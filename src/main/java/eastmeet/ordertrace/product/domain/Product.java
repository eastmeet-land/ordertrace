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
    private Long id;

    @Column(nullable = false, comment = "제품명")
    private String name;

    @Column(comment = "제품 설명")
    private String description;

    @Column(nullable = false, comment = "가격")
    private BigDecimal price;

    @Column(nullable = false, comment = "재고 수량")
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

}
