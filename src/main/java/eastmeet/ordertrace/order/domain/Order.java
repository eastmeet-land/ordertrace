package eastmeet.ordertrace.order.domain;

import eastmeet.ordertrace.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "주문 ID")
    private Long id;

    @Column(nullable = false, comment = "회원 ID")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "주문 상태")
    private OrderStatus status;

    @Column(nullable = false, comment = "총 주문 금액")
    private BigDecimal totalAmount;

    @Column(nullable = false, comment = "통화")
    private String currency;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order(Long memberId, String currency) {
        Assert.notNull(memberId, "회원 ID는 필수입니다.");
        Assert.hasText(currency, "통화는 필수입니다.");

        this.memberId = memberId;
        this.status = OrderStatus.CREATED;
        this.totalAmount = BigDecimal.ZERO;
        this.currency = currency;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.assignOrder(this);
        calculateTotalAmount();
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    private void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markPaymentPending() {
        validateStatus(OrderStatus.CREATED, "결제 대기로 변경");
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void markConfirmed() {
        validateStatus(OrderStatus.PAYMENT_PENDING, "주문 확정");
        this.status = OrderStatus.CONFIRMED;
    }

    public void markFailed() {
        validateStatus(OrderStatus.PAYMENT_PENDING, "주문 실패");
        this.status = OrderStatus.FAILED;
    }

    public void cancel() {
        validateStatus(OrderStatus.CONFIRMED, "주문 취소");
        this.status = OrderStatus.CANCELLED;
    }

    private void validateStatus(OrderStatus expected, String action) {
        if (this.status != expected) {
            throw new IllegalStateException(
                String.format("%s 상태에서만 [%s]가 가능합니다. 현재 상태: %s",
                    expected.getDescription(), action, this.status.getDescription()));
        }
    }

}
