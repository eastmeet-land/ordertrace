package eastmeet.ordertrace.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Nested
    @DisplayName("재고 차감")
    class DecreaseStock {

        @Test
        @DisplayName("정상적으로 재고를 차감한다")
        void success() {
            Product product = new Product("맥북", "설명", BigDecimal.valueOf(2990000), 10);

            product.decreaseStock(3);

            assertThat(product.getStockQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("재고를 전부 차감할 수 있다")
        void decreaseAllStock() {
            Product product = new Product("맥북", "설명", BigDecimal.valueOf(2990000), 5);

            product.decreaseStock(5);

            assertThat(product.getStockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("재고보다 많이 차감하면 예외가 발생한다")
        void failWhenInsufficientStock() {
            Product product = new Product("맥북", "설명", BigDecimal.valueOf(2990000), 3);

            assertThatThrownBy(() -> product.decreaseStock(5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고가 부족합니다");
        }

        @Test
        @DisplayName("수량이 null이면 예외가 발생한다")
        void failWithNullQuantity() {
            Product product = new Product("맥북", "설명", BigDecimal.valueOf(2990000), 10);

            assertThatThrownBy(() -> product.decreaseStock(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 필수");
        }

        @Test
        @DisplayName("수량이 0이면 예외가 발생한다")
        void failWithZeroQuantity() {
            Product product = new Product("맥북", "설명", BigDecimal.valueOf(2990000), 10);

            assertThatThrownBy(() -> product.decreaseStock(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 1 이상");
        }
    }

    @Nested
    @DisplayName("재고 복원")
    class IncreaseStock {

        @Test
        @DisplayName("정상적으로 재고를 복원한다")
        void success() {
            Product product = new Product("맥북", "설명", BigDecimal.valueOf(2990000), 7);

            product.increaseStock(3);

            assertThat(product.getStockQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("수량이 null이면 예외가 발생한다")
        void failWithNullQuantity() {
            Product product = new Product("맥북", "설명", BigDecimal.valueOf(2990000), 10);

            assertThatThrownBy(() -> product.increaseStock(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 필수");
        }

        @Test
        @DisplayName("수량이 0이면 예외가 발생한다")
        void failWithZeroQuantity() {
            Product product = new Product("맥북", "설명", BigDecimal.valueOf(2990000), 10);

            assertThatThrownBy(() -> product.increaseStock(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 1 이상");
        }
    }

    @Nested
    @DisplayName("재고 차감 → 복원 시나리오")
    class DecreaseAndRestore {

        @Test
        @DisplayName("차감 후 복원하면 원래 재고로 돌아간다")
        void restoreAfterDecrease() {
            Product product = new Product("맥북", "설명", BigDecimal.valueOf(2990000), 10);

            product.decreaseStock(3);
            assertThat(product.getStockQuantity()).isEqualTo(7);

            product.increaseStock(3);
            assertThat(product.getStockQuantity()).isEqualTo(10);
        }
    }
}
