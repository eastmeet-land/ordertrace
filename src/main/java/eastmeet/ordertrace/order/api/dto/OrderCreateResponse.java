package eastmeet.ordertrace.order.api.dto;

import eastmeet.ordertrace.order.domain.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record OrderCreateResponse(
    @Schema(description = "주문 ID", example = "1")
    Long id,

    @Schema(description = "주문 상태", example = "주문 생성")
    String status,

    @Schema(description = "주문 생성일시")
    LocalDateTime createdAt

) {

    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
            order.getId(),
            order.getStatus().getDescription(),
            order.getCreatedAt()
        );
    }

}