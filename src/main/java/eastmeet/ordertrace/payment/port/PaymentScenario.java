package eastmeet.ordertrace.payment.port;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentScenario {
    SUCCESS("결제 성공"),
    TIMEOUT("결제 시간 초과"),
    INSUFFICIENT_BALANCE("잔액 부족"),
    GATEWAY_ERROR("PG사 연동 오류"),

    ;

    private final String description;

}
