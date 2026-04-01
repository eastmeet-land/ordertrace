package eastmeet.ordertrace.order.event;

public record OrderCancelledEvent(
    Long orderId
) {}