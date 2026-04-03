package eastmeet.ordertrace.payment.domain;

import eastmeet.ordertrace.global.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @Nested
    @DisplayName("결제 생성")
    class Create {

        @Test
        @DisplayName("정상적으로 결제를 생성한다")
        void success() {
            Payment payment = new Payment(1L, BigDecimal.valueOf(10000), Currency.KRW);

            assertThat(payment.getOrderId()).isEqualTo(1L);
            assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
            assertThat(payment.getCurrency()).isEqualTo(Currency.KRW);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
            assertThat(payment.getRequestedAt()).isNotNull();
        }

        @Test
        @DisplayName("주문 ID가 null이면 예외가 발생한다")
        void failWithNullOrderId() {
            assertThatThrownBy(() -> new Payment(null, BigDecimal.valueOf(10000), Currency.KRW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 ID는 필수");
        }

        @Test
        @DisplayName("금액이 0이면 예외가 발생한다")
        void failWithZeroAmount() {
            assertThatThrownBy(() -> new Payment(1L, BigDecimal.ZERO, Currency.KRW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액은 0보다 커야");
        }

        @Test
        @DisplayName("금액이 음수이면 예외가 발생한다")
        void failWithNegativeAmount() {
            assertThatThrownBy(() -> new Payment(1L, BigDecimal.valueOf(-1), Currency.KRW))
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
            Payment payment = new Payment(1L, BigDecimal.valueOf(10000), Currency.KRW);

            payment.markProcessing();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        }

        @Test
        @DisplayName("PROCESSING → APPROVED")
        void processingToApproved() {
            Payment payment = new Payment(1L, BigDecimal.valueOf(10000), Currency.KRW);
            payment.markProcessing();

            payment.markApproved();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("PROCESSING → REJECTED")
        void processingToRejected() {
            Payment payment = new Payment(1L, BigDecimal.valueOf(10000), Currency.KRW);
            payment.markProcessing();

            payment.markRejected("잔액 부족");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
            assertThat(payment.getFailureReason()).isEqualTo("잔액 부족");
            assertThat(payment.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("APPROVED → REFUNDED")
        void approvedToRefunded() {
            Payment payment = new Payment(1L, BigDecimal.valueOf(10000), Currency.KRW);
            payment.markProcessing();
            payment.markApproved();

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
            Payment payment = new Payment(1L, BigDecimal.valueOf(10000), Currency.KRW);

            assertThatThrownBy(payment::markApproved)
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("REQUESTED 상태에서 거절은 불가능하다")
        void cannotRejectFromRequested() {
            Payment payment = new Payment(1L, BigDecimal.valueOf(10000), Currency.KRW);

            assertThatThrownBy(() -> payment.markRejected("사유"))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("APPROVED 상태에서 다시 승인은 불가능하다")
        void cannotApproveFromApproved() {
            Payment payment = new Payment(1L, BigDecimal.valueOf(10000), Currency.KRW);
            payment.markProcessing();
            payment.markApproved();

            assertThatThrownBy(payment::markApproved)
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("REJECTED 상태에서 환불은 불가능하다")
        void cannotRefundFromRejected() {
            Payment payment = new Payment(1L, BigDecimal.valueOf(10000), Currency.KRW);
            payment.markProcessing();
            payment.markRejected("사유");

            assertThatThrownBy(payment::markRefunded)
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("REFUNDED 상태에서 어떤 전이도 불가능하다")
        void cannotTransitionFromRefunded() {
            Payment payment = new Payment(1L, BigDecimal.valueOf(10000), Currency.KRW);
            payment.markProcessing();
            payment.markApproved();
            payment.markRefunded();

            assertThatThrownBy(payment::markProcessing).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(payment::markApproved).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> payment.markRejected("사유")).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(payment::markRefunded).isInstanceOf(IllegalStateException.class);
        }
    }
}
