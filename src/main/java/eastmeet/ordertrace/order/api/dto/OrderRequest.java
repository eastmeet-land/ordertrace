package eastmeet.ordertrace.order.api.dto;

import eastmeet.ordertrace.global.domain.Currency;
import eastmeet.ordertrace.payment.port.PaymentScenario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 생성 요청")
public record OrderRequest(
    @Schema(description = "회원 ID", example = "1")
    @NotNull(message = "회원 ID는 필수입니다.")
    Long memberId,

    @Schema(description = "상품 ID", example = "1")
    @NotNull(message = "상품 ID는 필수입니다.")
    Long productId,

    @Schema(description = "주문 수량", example = "2")
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    Integer quantity,

    @Schema(description = "통화", example = "KRW")
    @NotNull(message = "통화는 필수입니다.")
    Currency currency,

    @Schema(description = "결제 시나리오", example = "SUCCESS")
    @NotNull(message = "결제 시나리오는 필수입니다.")
    PaymentScenario scenario

) {

}
