package eastmeet.ordertrace.payment.service;

import eastmeet.ordertrace.global.domain.Currency;
import eastmeet.ordertrace.payment.domain.Payment;
import eastmeet.ordertrace.payment.port.PaymentProcessor;
import eastmeet.ordertrace.payment.port.PaymentRequest;
import eastmeet.ordertrace.payment.port.PaymentResult;
import eastmeet.ordertrace.payment.port.PaymentScenario;
import eastmeet.ordertrace.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProcessor paymentProcessor;

    @Transactional
    public Payment processPayment(Long orderId, BigDecimal amount, Currency currency, PaymentScenario scenario) {
        Payment payment = new Payment(orderId, amount, currency);
        paymentRepository.save(payment);

        payment.markProcessing();

        PaymentResult result = paymentProcessor.process(
            new PaymentRequest(orderId, amount, currency, scenario)
        );

        if (result.isSuccess()) {
            payment.markApproved();
            log.info("결제 승인 완료 - orderId: {}, paymentId: {}", orderId, payment.getId());
        } else {
            payment.markRejected(result.failureReason());
            log.error("결제 거절 - orderId: {}, reason: {}", orderId, result.failureReason());
        }

        return payment;
    }

    @Transactional
    public void refund(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException(
                "결제를 찾을 수 없습니다. orderId: " + orderId));
        payment.markRefunded();
        log.info("환불 처리 완료 - orderId: {}, paymentId: {}", orderId, payment.getId());
    }

    public Payment findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException(
                "결제를 찾을 수 없습니다. orderId: " + orderId));
    }

}
