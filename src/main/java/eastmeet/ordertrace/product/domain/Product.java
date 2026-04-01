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

}
