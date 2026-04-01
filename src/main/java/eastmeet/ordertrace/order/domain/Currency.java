package eastmeet.ordertrace.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {

    KRW("원화"),
    USD("달러"),
    JPY("엔화");

    private final String description;
}