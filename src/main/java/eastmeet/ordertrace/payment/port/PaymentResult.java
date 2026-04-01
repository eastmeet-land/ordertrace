package eastmeet.ordertrace.payment.port;

public record PaymentResult(
    Boolean isSuccess,

    String failureReason

) {
    public static PaymentResult success() {
        return new PaymentResult(true, null);
    }

    public static PaymentResult fail(String reason) {
        return new PaymentResult(false, reason);
    }
}