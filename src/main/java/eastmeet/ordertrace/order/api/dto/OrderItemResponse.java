package eastmeet.ordertrace.order.api.dto;

import eastmeet.ordertrace.order.domain.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record OrderItemResponse(
    @Schema(description = "주문 상품 ID", example = "1")
    Long id,

    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "수량", example = "2")
    Integer quantity,

    @Schema(description = "단가", example = "2990000")
    BigDecimal unitPrice,

    @Schema(description = "소계", example = "5980000")
    BigDecimal subtotal

) {

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
            orderItem.getId(),
            orderItem.getProductId(),
            orderItem.getQuantity(),
            orderItem.getUnitPrice(),
            orderItem.getSubtotal()
        );
    }
}