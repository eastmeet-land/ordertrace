package eastmeet.ordertrace.payment.port;

import eastmeet.ordertrace.global.domain.Currency;
import java.math.BigDecimal;

public record PaymentRequest(
    Long orderId,

    BigDecimal amount,

    Currency currency,

    PaymentScenario scenario

) {

}
