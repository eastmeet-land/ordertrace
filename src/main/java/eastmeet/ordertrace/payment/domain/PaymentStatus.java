package eastmeet.ordertrace.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    REQUESTED("결제요청"),
    PROCESSING("결제 처리 중"),
    APPROVED("결제 승인"),
    REJECTED("결제 거절"),
    REFUNDED("환불 완료"),

    ;

    private final String description;


}
