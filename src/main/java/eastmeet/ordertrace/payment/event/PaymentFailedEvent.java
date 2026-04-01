package eastmeet.ordertrace.payment.event;

public record PaymentFailedEvent(
    Long orderId,

    Long paymentId,

    String reason

) {

}
