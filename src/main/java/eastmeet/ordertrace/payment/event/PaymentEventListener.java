package eastmeet.ordertrace.payment.event;

import eastmeet.ordertrace.order.event.OrderCancelledEvent;
import eastmeet.ordertrace.order.event.OrderCreatedEvent;
import eastmeet.ordertrace.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신 - orderId: {}", event.orderId());
        paymentService.processPayment(
            event.orderId(),
            event.totalAmount(),
            event.currency(),
            event.scenario()
        );
    }

    @EventListener
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("주문 취소 이벤트 수신 - orderId: {}", event.orderId());
        paymentService.refund(event.orderId());
    }

}