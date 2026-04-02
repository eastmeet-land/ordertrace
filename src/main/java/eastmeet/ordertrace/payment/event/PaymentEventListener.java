package eastmeet.ordertrace.payment.event;

import static eastmeet.ordertrace.global.config.KafkaConfig.ORDER_EVENTS_TOPIC;

import eastmeet.ordertrace.order.event.OrderCancelledEvent;
import eastmeet.ordertrace.order.event.OrderCreatedEvent;
import eastmeet.ordertrace.payment.port.PaymentScenario;
import eastmeet.ordertrace.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = ORDER_EVENTS_TOPIC, groupId = "payment-group")
public class PaymentEventListener {

    private final PaymentService paymentService;

    @KafkaHandler
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신 - orderId: {}, thread: {}", event.orderId(), Thread.currentThread().getName());

        PaymentScenario scenario;
        try {
            scenario = PaymentScenario.valueOf(event.scenario());
        } catch (IllegalArgumentException e) {
            log.error("알 수 없는 결제 시나리오 '{}', 결제 처리 중단 - orderId: {}",
                event.scenario(), event.orderId());
            return;
        }

        paymentService.processPayment(
            event.orderId(),
            event.totalAmount(),
            event.currency(),
            scenario
        );
    }

    @KafkaHandler
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("주문 취소 이벤트 수신 - orderId: {}, thread: {}", event.orderId(), Thread.currentThread().getName());
        paymentService.refund(event.orderId());
    }

}