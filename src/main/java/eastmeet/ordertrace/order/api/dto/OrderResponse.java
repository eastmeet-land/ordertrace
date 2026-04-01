package eastmeet.ordertrace.order.api.dto;

import eastmeet.ordertrace.order.domain.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    @Schema(description = "주문 ID", example = "1")
    Long id,

    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "주문 상태", example = "주문 생성")
    String status,

    @Schema(description = "총 주문 금액", example = "5980000")
    BigDecimal totalAmount,

    @Schema(description = "통화", example = "KRW")
    String currency,

    @Schema(description = "주문 상품 목록")
    List<OrderItemResponse> orderItems,

    @Schema(description = "주문 생성일시")
    LocalDateTime createdAt

) {

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
            .map(OrderItemResponse::from)
            .toList();

        return new OrderResponse(
            order.getId(),
            order.getMemberId(),
            order.getStatus().getDescription(),
            order.getTotalAmount(),
            order.getCurrency(),
            items,
            order.getCreatedAt()
        );
    }

}