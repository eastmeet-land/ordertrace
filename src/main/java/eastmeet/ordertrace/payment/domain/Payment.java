package eastmeet.ordertrace.payment.domain;

import eastmeet.ordertrace.global.domain.Currency;
import eastmeet.ordertrace.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", comment = "결제 ID")
    private Long id;

    @Column(name = "order_id", nullable = false, comment = "주문 ID")
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, comment = "결제 상태")
    private PaymentStatus status;

    @Column(name = "amount", nullable = false, comment = "결제 금액")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, comment = "통화")
    private Currency currency;

    @Column(name = "failure_reason", comment = "실패 사유")
    private String failureReason;

    @Column(name = "requested_at", comment = "결제 요청 일시")
    private LocalDateTime requestedAt;

    @Column(name = "completed_at", comment = "결제 완료 일시")
    private LocalDateTime completedAt;

    public Payment(Long orderId, BigDecimal amount, Currency currency) {
        Assert.notNull(orderId, "주문 ID는 필수입니다.");
        Assert.notNull(amount, "결제 금액은 필수입니다.");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "결제 금액은 0보다 커야 합니다.");
        Assert.notNull(currency, "통화는 필수입니다.");

        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
    }

    public void markProcessing() {
        validateStatus(PaymentStatus.REQUESTED, "결제 처리");
        this.status = PaymentStatus.PROCESSING;
    }

    public void markApproved() {
        validateStatus(PaymentStatus.PROCESSING, "결제 승인");
        this.status = PaymentStatus.APPROVED;
        this.completedAt = LocalDateTime.now();
    }

    public void markRejected(String reason) {
        validateStatus(PaymentStatus.PROCESSING, "결제 거절");
        this.status = PaymentStatus.REJECTED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
    }

    public void markRefunded() {
        validateStatus(PaymentStatus.APPROVED, "환불");
        this.status = PaymentStatus.REFUNDED;
        this.completedAt = LocalDateTime.now();
    }

    private void validateStatus(PaymentStatus expected, String action) {
        if (this.status != expected) {
            throw new IllegalStateException(
                String.format("%s 상태에서만 [%s]가 가능합니다. 현재 상태: %s",
                    expected.getDescription(), action, this.status.getDescription()));
        }
    }

}
