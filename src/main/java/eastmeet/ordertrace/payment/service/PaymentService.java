package eastmeet.ordertrace.payment.service;

import eastmeet.ordertrace.global.domain.Currency;
import eastmeet.ordertrace.global.slack.SlackAlert;
import eastmeet.ordertrace.global.slack.SlackAlertService;
import eastmeet.ordertrace.payment.domain.Payment;
import eastmeet.ordertrace.payment.event.PaymentApprovedEvent;
import eastmeet.ordertrace.payment.event.PaymentFailedEvent;
import eastmeet.ordertrace.payment.port.PaymentProcessor;
import eastmeet.ordertrace.payment.port.PaymentRequest;
import eastmeet.ordertrace.payment.port.PaymentResult;
import eastmeet.ordertrace.payment.port.PaymentScenario;
import eastmeet.ordertrace.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProcessor paymentProcessor;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final SlackAlertService slackAlertService;

    public void processPayment(Long orderId, BigDecimal amount, Currency currency, PaymentScenario scenario) {
        // 1. 결제 요청 저장(트랜잭션 1)
        Long paymentId = transactionTemplate.execute(status -> {
            Payment saved = paymentRepository.save(new Payment(orderId, amount, currency));
            saved.markProcessing();
            return saved.getId();
        });

        // 2. 외부 연동 (트랜잭션 없음 - DB 커넥션 미점유)
        PaymentResult result = paymentProcessor.process(
            new PaymentRequest(orderId, amount, currency, scenario)
        );

        // 3. 결제 결과 반영 (트랜잭션 2)
        transactionTemplate.executeWithoutResult(status -> {
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(
                    () -> new EntityNotFoundException("결제를 찾을 수 없습니다. paymentId: " + paymentId)
                );

            if (result.isSuccess()) {
                payment.markApproved();
                eventPublisher.publishEvent(new PaymentApprovedEvent(orderId, payment.getId()));
                log.info("결제 승인 완료 - orderId: {}, paymentId: {}", orderId, payment.getId());
            } else {
                payment.markRejected(result.failureReason());
                eventPublisher.publishEvent(new PaymentFailedEvent(orderId, payment.getId(), result.failureReason()));
                log.error("결제 거절 - orderId: {}, reason: {}", orderId, result.failureReason());
                slackAlertService.send(new SlackAlert(
                    "🚨",
                    "결제 실패 알림",
                    String.format("*주문 ID:* %d\n*실패 사유:* %s", orderId, result.failureReason()),
                    String.valueOf(orderId)
                ));
            }
        });
    }

    @Transactional
    public void refund(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(
                () -> new EntityNotFoundException("결제를 찾을 수 없습니다. orderId: " + orderId)
            );

        payment.markRefunded();
        log.info("환불 처리 완료 - orderId: {}, paymentId: {}", orderId, payment.getId());
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(
                () -> new EntityNotFoundException("결제를 찾을 수 없습니다. orderId: " + orderId)
            );
    }

}
