package eastmeet.ordertrace.order.domain;

import eastmeet.ordertrace.global.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Nested
    @DisplayName("주문 생성")
    class Create {

        @Test
        @DisplayName("정상적으로 주문을 생성한다")
        void success() {
            Order order = new Order(1L, Currency.KRW);

            assertThat(order.getMemberId()).isEqualTo(1L);
            assertThat(order.getCurrency()).isEqualTo(Currency.KRW);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(order.getOrderItems()).isEmpty();
        }

        @Test
        @DisplayName("회원 ID가 null이면 예외가 발생한다")
        void failWithNullMemberId() {
            assertThatThrownBy(() -> new Order(null, Currency.KRW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회원 ID는 필수");
        }

        @Test
        @DisplayName("통화가 null이면 예외가 발생한다")
        void failWithNullCurrency() {
            assertThatThrownBy(() -> new Order(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("통화는 필수");
        }
    }

    @Nested
    @DisplayName("주문 상품 추가")
    class AddOrderItem {

        @Test
        @DisplayName("주문 상품을 추가하면 총 금액이 계산된다")
        void calculateTotalAmount() {
            Order order = new Order(1L, Currency.KRW);
            OrderItem item = new OrderItem(1L, 2, BigDecimal.valueOf(10000));

            order.addOrderItem(item);

            assertThat(order.getOrderItems()).hasSize(1);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        }

        @Test
        @DisplayName("여러 상품을 추가하면 총 금액이 합산된다")
        void calculateTotalAmountWithMultipleItems() {
            Order order = new Order(1L, Currency.KRW);
            OrderItem item1 = new OrderItem(1L, 2, BigDecimal.valueOf(10000));
            OrderItem item2 = new OrderItem(2L, 1, BigDecimal.valueOf(5000));

            order.addOrderItem(item1);
            order.addOrderItem(item2);

            assertThat(order.getOrderItems()).hasSize(2);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(25000));
        }

        @Test
        @DisplayName("주문 상품 목록은 수정할 수 없다")
        void orderItemsAreUnmodifiable() {
            Order order = new Order(1L, Currency.KRW);
            order.addOrderItem(new OrderItem(1L, 1, BigDecimal.valueOf(10000)));

            assertThatThrownBy(() -> order.getOrderItems().add(
                new OrderItem(2L, 1, BigDecimal.valueOf(5000))
            )).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("상태 전이 - 정상 흐름")
    class ValidStateTransition {

        @Test
        @DisplayName("CREATED → PAYMENT_PENDING")
        void createdToPaymentPending() {
            Order order = new Order(1L, Currency.KRW);

            order.markPaymentPending();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }

        @Test
        @DisplayName("PAYMENT_PENDING → CONFIRMED")
        void paymentPendingToConfirmed() {
            Order order = new Order(1L, Currency.KRW);
            order.markPaymentPending();

            order.markConfirmed();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("PAYMENT_PENDING → FAILED")
        void paymentPendingToFailed() {
            Order order = new Order(1L, Currency.KRW);
            order.markPaymentPending();

            order.markFailed();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
        }

        @Test
        @DisplayName("CONFIRMED → CANCELLED")
        void confirmedToCancelled() {
            Order order = new Order(1L, Currency.KRW);
            order.markPaymentPending();
            order.markConfirmed();

            order.cancel();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("상태 전이 - 잘못된 전이")
    class InvalidStateTransition {

        @Test
        @DisplayName("CREATED 상태에서 주문 확정은 불가능하다")
        void cannotConfirmFromCreated() {
            Order order = new Order(1L, Currency.KRW);

            assertThatThrownBy(order::markConfirmed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 대기")
                .hasMessageContaining("주문 확정");
        }

        @Test
        @DisplayName("CREATED 상태에서 주문 실패는 불가능하다")
        void cannotFailFromCreated() {
            Order order = new Order(1L, Currency.KRW);

            assertThatThrownBy(order::markFailed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 대기")
                .hasMessageContaining("주문 실패");
        }

        @Test
        @DisplayName("CREATED 상태에서 주문 취소는 불가능하다")
        void cannotCancelFromCreated() {
            Order order = new Order(1L, Currency.KRW);

            assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 확정")
                .hasMessageContaining("주문 취소");
        }

        @Test
        @DisplayName("CONFIRMED 상태에서 다시 확정은 불가능하다")
        void cannotConfirmFromConfirmed() {
            Order order = new Order(1L, Currency.KRW);
            order.markPaymentPending();
            order.markConfirmed();

            assertThatThrownBy(order::markConfirmed)
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("FAILED 상태에서 주문 확정은 불가능하다")
        void cannotConfirmFromFailed() {
            Order order = new Order(1L, Currency.KRW);
            order.markPaymentPending();
            order.markFailed();

            assertThatThrownBy(order::markConfirmed)
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("FAILED 상태에서 주문 취소는 불가능하다")
        void cannotCancelFromFailed() {
            Order order = new Order(1L, Currency.KRW);
            order.markPaymentPending();
            order.markFailed();

            assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("CANCELLED 상태에서 어떤 전이도 불가능하다")
        void cannotTransitionFromCancelled() {
            Order order = new Order(1L, Currency.KRW);
            order.markPaymentPending();
            order.markConfirmed();
            order.cancel();

            assertThatThrownBy(order::markPaymentPending).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(order::markConfirmed).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(order::markFailed).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(order::cancel).isInstanceOf(IllegalStateException.class);
        }
    }
}
