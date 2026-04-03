package eastmeet.ordertrace.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    @Nested
    @DisplayName("주문 상품 생성")
    class Create {

        @Test
        @DisplayName("정상적으로 주문 상품을 생성한다")
        void success() {
            OrderItem item = new OrderItem(1L, 3, BigDecimal.valueOf(10000));

            assertThat(item.getProductId()).isEqualTo(1L);
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));
            assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        }

        @Test
        @DisplayName("소계는 단가 × 수량으로 계산된다")
        void subtotalCalculation() {
            OrderItem item = new OrderItem(1L, 5, BigDecimal.valueOf(2990000));

            assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(14950000));
        }

        @Test
        @DisplayName("상품 ID가 null이면 예외가 발생한다")
        void failWithNullProductId() {
            assertThatThrownBy(() -> new OrderItem(null, 1, BigDecimal.valueOf(10000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 ID는 필수");
        }

        @Test
        @DisplayName("수량이 null이면 예외가 발생한다")
        void failWithNullQuantity() {
            assertThatThrownBy(() -> new OrderItem(1L, null, BigDecimal.valueOf(10000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 필수");
        }

        @Test
        @DisplayName("수량이 0이면 예외가 발생한다")
        void failWithZeroQuantity() {
            assertThatThrownBy(() -> new OrderItem(1L, 0, BigDecimal.valueOf(10000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 1 이상");
        }

        @Test
        @DisplayName("수량이 음수이면 예외가 발생한다")
        void failWithNegativeQuantity() {
            assertThatThrownBy(() -> new OrderItem(1L, -1, BigDecimal.valueOf(10000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 1 이상");
        }

        @Test
        @DisplayName("단가가 null이면 예외가 발생한다")
        void failWithNullUnitPrice() {
            assertThatThrownBy(() -> new OrderItem(1L, 1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("단가는 필수");
        }

        @Test
        @DisplayName("단가가 음수이면 예외가 발생한다")
        void failWithNegativeUnitPrice() {
            assertThatThrownBy(() -> new OrderItem(1L, 1, BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("단가는 0 이상");
        }

        @Test
        @DisplayName("단가가 0이면 정상 생성된다 (무료 상품)")
        void successWithZeroUnitPrice() {
            OrderItem item = new OrderItem(1L, 1, BigDecimal.ZERO);

            assertThat(item.getUnitPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
