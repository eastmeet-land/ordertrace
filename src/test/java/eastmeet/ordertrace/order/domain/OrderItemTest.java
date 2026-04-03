package eastmeet.ordertrace.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    private static final Long DEFAULT_PRODUCT_ID = 1L;
    private static final int DEFAULT_QUANTITY = 3;
    private static final BigDecimal DEFAULT_UNIT_PRICE = BigDecimal.valueOf(10_000);

    private OrderItem createOrderItem() {
        return new OrderItem(DEFAULT_PRODUCT_ID, DEFAULT_QUANTITY, DEFAULT_UNIT_PRICE);
    }

    @Nested
    @DisplayName("주문 상품 생성")
    class Create {

        @Test
        @DisplayName("정상적으로 주문 상품을 생성한다")
        void success() {
            OrderItem item = createOrderItem();

            assertThat(item.getProductId()).isEqualTo(DEFAULT_PRODUCT_ID);
            assertThat(item.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
            assertThat(item.getUnitPrice()).isEqualByComparingTo(DEFAULT_UNIT_PRICE);
            assertThat(item.getSubtotal()).isEqualByComparingTo(
                DEFAULT_UNIT_PRICE.multiply(BigDecimal.valueOf(DEFAULT_QUANTITY)));
        }

        @Test
        @DisplayName("소계는 단가 × 수량으로 계산된다")
        void subtotalCalculation() {
            BigDecimal unitPrice = BigDecimal.valueOf(2_990_000);
            int quantity = 5;

            OrderItem item = new OrderItem(DEFAULT_PRODUCT_ID, quantity, unitPrice);

            assertThat(item.getSubtotal()).isEqualByComparingTo(
                unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        @Test
        @DisplayName("상품 ID가 null이면 예외가 발생한다")
        void failWithNullProductId() {
            assertThatThrownBy(() -> new OrderItem(null, DEFAULT_QUANTITY, DEFAULT_UNIT_PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 ID는 필수");
        }

        @Test
        @DisplayName("수량이 null이면 예외가 발생한다")
        void failWithNullQuantity() {
            assertThatThrownBy(() -> new OrderItem(DEFAULT_PRODUCT_ID, null, DEFAULT_UNIT_PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 필수");
        }

        @Test
        @DisplayName("수량이 0이면 예외가 발생한다")
        void failWithZeroQuantity() {
            assertThatThrownBy(() -> new OrderItem(DEFAULT_PRODUCT_ID, 0, DEFAULT_UNIT_PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 1 이상");
        }

        @Test
        @DisplayName("수량이 음수이면 예외가 발생한다")
        void failWithNegativeQuantity() {
            assertThatThrownBy(() -> new OrderItem(DEFAULT_PRODUCT_ID, -1, DEFAULT_UNIT_PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 1 이상");
        }

        @Test
        @DisplayName("단가가 null이면 예외가 발생한다")
        void failWithNullUnitPrice() {
            assertThatThrownBy(() -> new OrderItem(DEFAULT_PRODUCT_ID, DEFAULT_QUANTITY, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("단가는 필수");
        }

        @Test
        @DisplayName("단가가 음수이면 예외가 발생한다")
        void failWithNegativeUnitPrice() {
            assertThatThrownBy(() -> new OrderItem(DEFAULT_PRODUCT_ID, DEFAULT_QUANTITY, BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("단가는 0 이상");
        }

        @Test
        @DisplayName("단가가 0이면 정상 생성된다 (무료 상품)")
        void successWithZeroUnitPrice() {
            OrderItem item = new OrderItem(DEFAULT_PRODUCT_ID, DEFAULT_QUANTITY, BigDecimal.ZERO);

            assertThat(item.getUnitPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
