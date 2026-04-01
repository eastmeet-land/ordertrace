package eastmeet.ordertrace.order.event;


import eastmeet.ordertrace.order.service.OrderService;
import eastmeet.ordertrace.payment.event.PaymentApprovedEvent;
import eastmeet.ordertrace.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderService orderService;

    @EventListener
    public void handlePaymentApproved(PaymentApprovedEvent event) {
        log.info("결제 승인 이벤트 수신 - orderId: {}, thread: {}", event.orderId(), Thread.currentThread().getName());
        orderService.confirmOrder(event.orderId());
    }

    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신 - orderId: {}, thread: {}", event.orderId(), Thread.currentThread().getName());
        orderService.failOrder(event.orderId());
    }

}
