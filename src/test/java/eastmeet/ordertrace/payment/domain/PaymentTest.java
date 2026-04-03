package eastmeet.ordertrace.payment.domain;

import eastmeet.ordertrace.global.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private static final Long DEFAULT_ORDER_ID = 1L;
    private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(10_000);
    private static final Currency DEFAULT_CURRENCY = Currency.KRW;
    private static final String DEFAULT_FAILURE_REASON = "잔액 부족";

    private Payment createPayment() {
        return new Payment(DEFAULT_ORDER_ID, DEFAULT_AMOUNT, DEFAULT_CURRENCY);
    }

    private Payment createPaymentWithStatus(PaymentStatus targetStatus) {
        Payment payment = createPayment();
        switch (targetStatus) {
            case PROCESSING -> payment.markProcessing();
            case APPROVED -> { payment.markProcessing(); payment.markApproved(); }
            case REJECTED -> { payment.markProcessing(); payment.markRejected(DEFAULT_FAILURE_REASON); }
            case REFUNDED -> { payment.markProcessing(); payment.markApproved(); payment.markRefunded(); }
            default -> {} // REQUESTED
        }
        return payment;
    }

    @Nested
    @DisplayName("결제 생성")
    class Create {

        @Test
        @DisplayName("정상적으로 결제를 생성한다")
        void success() {
            Payment payment = createPayment();

            assertThat(payment.getOrderId()).isEqualTo(DEFAULT_ORDER_ID);
            assertThat(payment.getAmount()).isEqualByComparingTo(DEFAULT_AMOUNT);
            assertThat(payment.getCurrency()).isEqualTo(DEFAULT_CURRENCY);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
            assertThat(payment.getRequestedAt()).isNotNull();
        }

        @Test
        @DisplayName("주문 ID가 null이면 예외가 발생한다")
        void failWithNullOrderId() {
            assertThatThrownBy(() -> new Payment(null, DEFAULT_AMOUNT, DEFAULT_CURRENCY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 ID는 필수");
        }

        @Test
        @DisplayName("금액이 0이면 예외가 발생한다")
        void failWithZeroAmount() {
            assertThatThrownBy(() -> new Payment(DEFAULT_ORDER_ID, BigDecimal.ZERO, DEFAULT_CURRENCY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액은 0보다 커야");
        }

        @Test
        @DisplayName("금액이 음수이면 예외가 발생한다")
        void failWithNegativeAmount() {
            assertThatThrownBy(() -> new Payment(DEFAULT_ORDER_ID, BigDecimal.valueOf(-1), DEFAULT_CURRENCY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액은 0보다 커야");
        }
    }

    @Nested
    @DisplayName("상태 전이 - 정상 흐름")
    class ValidStateTransition {

        @Test
        @DisplayName("REQUESTED → PROCESSING")
        void requestedToProcessing() {
            Payment payment = createPayment();

            payment.markProcessing();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        }

        @Test
        @DisplayName("PROCESSING → APPROVED")
        void processingToApproved() {
            Payment payment = createPaymentWithStatus(PaymentStatus.PROCESSING);

            payment.markApproved();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("PROCESSING → REJECTED")
        void processingToRejected() {
            Payment payment = createPaymentWithStatus(PaymentStatus.PROCESSING);

            payment.markRejected(DEFAULT_FAILURE_REASON);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
            assertThat(payment.getFailureReason()).isEqualTo(DEFAULT_FAILURE_REASON);
            assertThat(payment.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("APPROVED → REFUNDED")
        void approvedToRefunded() {
            Payment payment = createPaymentWithStatus(PaymentStatus.APPROVED);

            payment.markRefunded();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }
    }

    @Nested
    @DisplayName("상태 전이 - 잘못된 전이")
    class InvalidStateTransition {

        @Test
        @DisplayName("REQUESTED 상태에서 승인은 불가능하다")
        void cannotApproveFromRequested() {
            Payment payment = createPayment();

            assertThatThrownBy(payment::markApproved)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 처리 중")
                .hasMessageContaining("결제 승인")
                .hasMessageContaining("결제요청");
        }

        @Test
        @DisplayName("REQUESTED 상태에서 거절은 불가능하다")
        void cannotRejectFromRequested() {
            Payment payment = createPayment();

            assertThatThrownBy(() -> payment.markRejected(DEFAULT_FAILURE_REASON))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 처리 중")
                .hasMessageContaining("결제 거절")
                .hasMessageContaining("결제요청");
        }

        @Test
        @DisplayName("APPROVED 상태에서 다시 승인은 불가능하다")
        void cannotApproveFromApproved() {
            Payment payment = createPaymentWithStatus(PaymentStatus.APPROVED);

            assertThatThrownBy(payment::markApproved)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 처리 중")
                .hasMessageContaining("결제 승인");
        }

        @Test
        @DisplayName("REJECTED 상태에서 환불은 불가능하다")
        void cannotRefundFromRejected() {
            Payment payment = createPaymentWithStatus(PaymentStatus.REJECTED);

            assertThatThrownBy(payment::markRefunded)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 승인")
                .hasMessageContaining("환불")
                .hasMessageContaining("결제 거절");
        }

        @Test
        @DisplayName("REFUNDED 상태에서 어떤 전이도 불가능하다")
        void cannotTransitionFromRefunded() {
            Payment payment = createPaymentWithStatus(PaymentStatus.REFUNDED);

            assertThatThrownBy(payment::markProcessing)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("환불 완료");
            assertThatThrownBy(payment::markApproved)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("환불 완료");
            assertThatThrownBy(() -> payment.markRejected(DEFAULT_FAILURE_REASON))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("환불 완료");
            assertThatThrownBy(payment::markRefunded)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("환불 완료");
        }
    }
}
