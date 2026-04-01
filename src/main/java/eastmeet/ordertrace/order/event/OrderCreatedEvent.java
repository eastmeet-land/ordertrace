package eastmeet.ordertrace.order.event;

import eastmeet.ordertrace.global.domain.Currency;
import eastmeet.ordertrace.payment.port.PaymentScenario;
import java.math.BigDecimal;

public record OrderCreatedEvent(
    Long orderId,

    BigDecimal totalAmount,

    Currency currency,

    PaymentScenario scenario

) {

}
