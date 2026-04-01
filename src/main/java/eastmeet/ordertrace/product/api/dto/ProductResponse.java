package eastmeet.ordertrace.product.api.dto;

import eastmeet.ordertrace.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "상품 응답")
public record ProductResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "상품명", example = "맥북 프로 14")
    String name,

    @Schema(description = "상품 설명", example = "Apple M3 Pro 칩")
    String description,

    @Schema(description = "가격", example = "2990000")
    BigDecimal price,

    @Schema(description = "재고 수량", example = "10")
    Integer stockQuantity
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity()
        );
    }
}
