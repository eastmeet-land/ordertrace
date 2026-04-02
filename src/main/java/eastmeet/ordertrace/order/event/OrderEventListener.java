package eastmeet.ordertrace.order.event;


import static eastmeet.ordertrace.global.config.KafkaConfig.PAYMENT_EVENTS_TOPIC;

import eastmeet.ordertrace.order.service.OrderService;
import eastmeet.ordertrace.payment.event.PaymentApprovedEvent;
import eastmeet.ordertrace.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = PAYMENT_EVENTS_TOPIC, groupId = "order-group")
public class OrderEventListener {

    private final OrderService orderService;

    @KafkaHandler
    public void handlePaymentApproved(PaymentApprovedEvent event) {
        log.info("결제 승인 이벤트 수신 - orderId: {}, thread: {}", event.orderId(), Thread.currentThread().getName());
        orderService.confirmOrder(event.orderId());
    }

    @KafkaHandler
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신 - orderId: {}, thread: {}", event.orderId(), Thread.currentThread().getName());
        orderService.failOrderAndRestoreStock(event.orderId());
    }

}
