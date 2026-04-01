package eastmeet.ordertrace.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    CREATED("주문 생성"),
    PAYMENT_PENDING("결제 대기"),
    CONFIRMED("주문 확정"),
    FAILED("주문 실패"),
    CANCELLED("주문 취소"),

    ;

    private final String description;

}
