package eastmeet.ordertrace.product.domain;

import eastmeet.ordertrace.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", comment = "상품 ID")
    private Long id;

    @Column(name = "name", nullable = false, comment = "상품명")
    private String name;

    @Column(name = "description", comment = "상품 설명")
    private String description;

    @Column(name = "price", nullable = false, comment = "가격")
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false, comment = "재고 수량")
    private Integer stockQuantity;

    public Product(String name, String description, BigDecimal price, Integer stockQuantity) {
        Assert.hasText(name, "상품명은 필수입니다.");

        Assert.notNull(price, "가격은 필수입니다.");
        Assert.isTrue(price.compareTo(BigDecimal.ZERO) >= 0, "가격은 0 이상이어야 합니다.");

        Assert.notNull(stockQuantity, "재고 수량은 필수입니다.");
        Assert.isTrue(stockQuantity >= 0, "재고 수량은 0 이상이어야 합니다.");

        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public void decreaseStock(Integer quantity) {
        Assert.notNull(quantity, "수량은 필수입니다.");
        Assert.isTrue(quantity > 0, "수량은 1 이상이어야 합니다.");
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException(
                "재고가 부족합니다. 현재 재고: " + this.stockQuantity + ", 요청 수량: " + quantity);
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(Integer quantity) {
        Assert.notNull(quantity, "수량은 필수입니다.");
        Assert.isTrue(quantity > 0, "수량은 1 이상이어야 합니다.");
        this.stockQuantity += quantity;
    }

}
