package eastmeet.ordertrace.order.domain;

import eastmeet.ordertrace.global.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Long DEFAULT_MEMBER_ID = 1L;
    private static final Currency DEFAULT_CURRENCY = Currency.KRW;
    private static final Long DEFAULT_PRODUCT_ID = 1L;
    private static final int DEFAULT_QUANTITY = 2;
    private static final BigDecimal DEFAULT_UNIT_PRICE = BigDecimal.valueOf(10_000);

    private Order createOrder() {
        return new Order(DEFAULT_MEMBER_ID, DEFAULT_CURRENCY);
    }

    private OrderItem createOrderItem() {
        return new OrderItem(DEFAULT_PRODUCT_ID, DEFAULT_QUANTITY, DEFAULT_UNIT_PRICE);
    }

    private OrderItem createOrderItem(Long productId, int quantity, BigDecimal unitPrice) {
        return new OrderItem(productId, quantity, unitPrice);
    }

    private Order createOrderWithStatus(OrderStatus targetStatus) {
        Order order = createOrder();
        switch (targetStatus) {
            case PAYMENT_PENDING -> order.markPaymentPending();
            case CONFIRMED -> { order.markPaymentPending(); order.markConfirmed(); }
            case FAILED -> { order.markPaymentPending(); order.markFailed(); }
            case CANCELLED -> { order.markPaymentPending(); order.markConfirmed(); order.cancel(); }
            default -> {} // CREATED
        }
        return order;
    }

    @Nested
    @DisplayName("주문 생성")
    class Create {

        @Test
        @DisplayName("정상적으로 주문을 생성한다")
        void success() {
            Order order = createOrder();

            assertThat(order.getMemberId()).isEqualTo(DEFAULT_MEMBER_ID);
            assertThat(order.getCurrency()).isEqualTo(DEFAULT_CURRENCY);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(order.getOrderItems()).isEmpty();
        }

        @Test
        @DisplayName("회원 ID가 null이면 예외가 발생한다")
        void failWithNullMemberId() {
            assertThatThrownBy(() -> new Order(null, DEFAULT_CURRENCY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회원 ID는 필수");
        }

        @Test
        @DisplayName("통화가 null이면 예외가 발생한다")
        void failWithNullCurrency() {
            assertThatThrownBy(() -> new Order(DEFAULT_MEMBER_ID, null))
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
            Order order = createOrder();
            OrderItem item = createOrderItem();

            order.addOrderItem(item);

            assertThat(order.getOrderItems()).hasSize(1);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(
                DEFAULT_UNIT_PRICE.multiply(BigDecimal.valueOf(DEFAULT_QUANTITY)));
        }

        @Test
        @DisplayName("여러 상품을 추가하면 총 금액이 합산된다")
        void calculateTotalAmountWithMultipleItems() {
            Order order = createOrder();
            BigDecimal secondItemPrice = BigDecimal.valueOf(5_000);
            int secondItemQuantity = 1;

            order.addOrderItem(createOrderItem());
            order.addOrderItem(createOrderItem(2L, secondItemQuantity, secondItemPrice));

            BigDecimal expectedTotal = DEFAULT_UNIT_PRICE.multiply(BigDecimal.valueOf(DEFAULT_QUANTITY))
                .add(secondItemPrice.multiply(BigDecimal.valueOf(secondItemQuantity)));

            assertThat(order.getOrderItems()).hasSize(2);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(expectedTotal);
        }

        @Test
        @DisplayName("주문 상품 목록은 수정할 수 없다")
        void orderItemsAreUnmodifiable() {
            Order order = createOrder();
            order.addOrderItem(createOrderItem());

            assertThatThrownBy(() -> order.getOrderItems().add(createOrderItem()))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("상태 전이 - 정상 흐름")
    class ValidStateTransition {

        @Test
        @DisplayName("CREATED → PAYMENT_PENDING")
        void createdToPaymentPending() {
            Order order = createOrder();

            order.markPaymentPending();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }

        @Test
        @DisplayName("PAYMENT_PENDING → CONFIRMED")
        void paymentPendingToConfirmed() {
            Order order = createOrderWithStatus(OrderStatus.PAYMENT_PENDING);

            order.markConfirmed();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("PAYMENT_PENDING → FAILED")
        void paymentPendingToFailed() {
            Order order = createOrderWithStatus(OrderStatus.PAYMENT_PENDING);

            order.markFailed();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
        }

        @Test
        @DisplayName("CONFIRMED → CANCELLED")
        void confirmedToCancelled() {
            Order order = createOrderWithStatus(OrderStatus.CONFIRMED);

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
            Order order = createOrder();

            assertThatThrownBy(order::markConfirmed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 대기")
                .hasMessageContaining("주문 확정")
                .hasMessageContaining("주문 생성");
        }

        @Test
        @DisplayName("CREATED 상태에서 주문 실패는 불가능하다")
        void cannotFailFromCreated() {
            Order order = createOrder();

            assertThatThrownBy(order::markFailed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 대기")
                .hasMessageContaining("주문 실패")
                .hasMessageContaining("주문 생성");
        }

        @Test
        @DisplayName("CREATED 상태에서 주문 취소는 불가능하다")
        void cannotCancelFromCreated() {
            Order order = createOrder();

            assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 확정")
                .hasMessageContaining("주문 취소")
                .hasMessageContaining("주문 생성");
        }

        @Test
        @DisplayName("CONFIRMED 상태에서 다시 확정은 불가능하다")
        void cannotConfirmFromConfirmed() {
            Order order = createOrderWithStatus(OrderStatus.CONFIRMED);

            assertThatThrownBy(order::markConfirmed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 대기")
                .hasMessageContaining("주문 확정");
        }

        @Test
        @DisplayName("FAILED 상태에서 주문 확정은 불가능하다")
        void cannotConfirmFromFailed() {
            Order order = createOrderWithStatus(OrderStatus.FAILED);

            assertThatThrownBy(order::markConfirmed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 대기")
                .hasMessageContaining("주문 확정")
                .hasMessageContaining("주문 실패");
        }

        @Test
        @DisplayName("FAILED 상태에서 주문 취소는 불가능하다")
        void cannotCancelFromFailed() {
            Order order = createOrderWithStatus(OrderStatus.FAILED);

            assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 확정")
                .hasMessageContaining("주문 취소")
                .hasMessageContaining("주문 실패");
        }

        @Test
        @DisplayName("CANCELLED 상태에서 어떤 전이도 불가능하다")
        void cannotTransitionFromCancelled() {
            Order order = createOrderWithStatus(OrderStatus.CANCELLED);

            assertThatThrownBy(order::markPaymentPending)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 취소");
            assertThatThrownBy(order::markConfirmed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 취소");
            assertThatThrownBy(order::markFailed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 취소");
            assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 취소");
        }
    }
}
