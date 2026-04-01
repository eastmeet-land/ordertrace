package eastmeet.ordertrace.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "주문 상품 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, comment = "주문 ID")
    private Order order;

    @Column(nullable = false, comment = "상품 ID")
    private Long productId;

    @Column(nullable = false, comment = "수량")
    private Integer quantity;

    @Column(nullable = false, comment = "단가")
    private BigDecimal unitPrice;

    @Column(nullable = false, comment = "소계")
    private BigDecimal subtotal;

    public OrderItem(Long productId, Integer quantity, BigDecimal unitPrice) {
        Assert.notNull(productId, "상품 ID는 필수입니다.");
        Assert.notNull(quantity, "수량은 필수입니다.");
        Assert.isTrue(quantity > 0, "수량은 1 이상이어야 합니다.");
        Assert.notNull(unitPrice, "단가는 필수입니다.");
        Assert.isTrue(unitPrice.compareTo(BigDecimal.ZERO) >= 0, "단가는 0 이상이어야 합니다.");

        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    void assignOrder(Order order) {
        this.order = order;
    }
}