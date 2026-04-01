package eastmeet.ordertrace.payment.event;

public record PaymentApprovedEvent (
    Long orderId,

    Long paymentId

){

}
