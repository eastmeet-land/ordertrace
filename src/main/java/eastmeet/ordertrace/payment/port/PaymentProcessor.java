package eastmeet.ordertrace.payment.port;

public interface PaymentProcessor {

    PaymentResult process(PaymentRequest request);

}
