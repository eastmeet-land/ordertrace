package eastmeet.ordertrace.payment.adapter;

import eastmeet.ordertrace.payment.port.PaymentProcessor;
import eastmeet.ordertrace.payment.port.PaymentRequest;
import eastmeet.ordertrace.payment.port.PaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockPaymentProcessor implements PaymentProcessor {

    public static final int TIMEOUT_MILLIS = 3000;

    @Override
    public PaymentResult process(PaymentRequest request) {
        log.info("결제 처리 시작 - orderId: {}, amount: {}, scenario: {}",
            request.orderId(), request.amount(), request.scenario());

        return switch (request.scenario()) {
            case TIMEOUT -> {
                try {
                    Thread.sleep(TIMEOUT_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.error("결제 타임아웃 - orderId: {}", request.orderId());
                yield PaymentResult.fail("결제 처리 시간 초과");
            }
            case INSUFFICIENT_BALANCE -> {
                log.error("잔액 부족 - orderId: {}", request.orderId());
                yield PaymentResult.fail("잔액이 부족합니다");
            }
            case GATEWAY_ERROR -> {
                log.error("PG사 오류 - orderId: {}", request.orderId());
                yield PaymentResult.fail("PG사 연동 오류");
            }
            case SUCCESS -> {
                log.info("결제 성공 - orderId: {}", request.orderId());
                yield PaymentResult.success();
            }
        };
    }

}
