package eastmeet.ordertrace.order.api.dto;

import eastmeet.ordertrace.global.domain.Currency;
import eastmeet.ordertrace.payment.port.PaymentScenario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "주문 생성 요청")
public record OrderRequest(
    @Schema(description = "회원 ID", example = "1")
    @NotNull(message = "회원 ID는 필수입니다.")
    @Positive(message = "회원 ID는 양수여야 합니다.")
    Long memberId,

    @Schema(description = "주문 상품 목록")
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItemRequest> items,

    @Schema(description = "통화", example = "KRW")
    @NotNull(message = "통화는 필수입니다.")
    Currency currency,

    @Schema(description = "결제 시나리오", example = "SUCCESS")
    @NotNull(message = "결제 시나리오는 필수입니다.")
    PaymentScenario scenario
) {}