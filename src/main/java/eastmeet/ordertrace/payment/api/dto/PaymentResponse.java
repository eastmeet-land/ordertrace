package eastmeet.ordertrace.payment.api.dto;

import eastmeet.ordertrace.payment.domain.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "결제 응답")
public record PaymentResponse(
    @Schema(description = "결제 ID", example = "1")
    Long id,

    @Schema(description = "주문 ID", example = "1")
    Long orderId,

    @Schema(description = "결제 상태", example = "결제 승인")
    String status,

    @Schema(description = "결제 금액", example = "5980000")
    BigDecimal amount,

    @Schema(description = "통화", example = "KRW")
    String currency,

    @Schema(description = "실패 사유")
    String failureReason,

    @Schema(description = "결제 요청 일시")
    LocalDateTime requestedAt,

    @Schema(description = "결제 완료 일시")
    LocalDateTime completedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getOrderId(),
            payment.getStatus().getDescription(),
            payment.getAmount(),
            payment.getCurrency().name(),
            payment.getFailureReason(),
            payment.getRequestedAt(),
            payment.getCompletedAt()
        );
    }
}
