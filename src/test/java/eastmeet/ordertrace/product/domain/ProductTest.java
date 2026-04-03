package eastmeet.ordertrace.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static final String DEFAULT_NAME = "맥북 프로 14";
    private static final String DEFAULT_DESCRIPTION = "Apple M3 Pro 칩";
    private static final BigDecimal DEFAULT_PRICE = BigDecimal.valueOf(2_990_000);
    private static final int DEFAULT_STOCK = 10;

    private Product createProduct() {
        return new Product(DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_PRICE, DEFAULT_STOCK);
    }

    private Product createProductWithStock(int stock) {
        return new Product(DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_PRICE, stock);
    }

    @Nested
    @DisplayName("재고 차감")
    class DecreaseStock {

        @Test
        @DisplayName("정상적으로 재고를 차감한다")
        void success() {
            Product product = createProduct();

            product.decreaseStock(3);

            assertThat(product.getStockQuantity()).isEqualTo(DEFAULT_STOCK - 3);
        }

        @Test
        @DisplayName("재고를 전부 차감할 수 있다")
        void decreaseAllStock() {
            Product product = createProductWithStock(5);

            product.decreaseStock(5);

            assertThat(product.getStockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("재고보다 많이 차감하면 예외가 발생한다")
        void failWhenInsufficientStock() {
            Product product = createProductWithStock(3);

            assertThatThrownBy(() -> product.decreaseStock(5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고가 부족합니다");
        }

        @Test
        @DisplayName("수량이 null이면 예외가 발생한다")
        void failWithNullQuantity() {
            Product product = createProduct();

            assertThatThrownBy(() -> product.decreaseStock(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 필수");
        }

        @Test
        @DisplayName("수량이 0이면 예외가 발생한다")
        void failWithZeroQuantity() {
            Product product = createProduct();

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
            Product product = createProductWithStock(7);

            product.increaseStock(3);

            assertThat(product.getStockQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("수량이 null이면 예외가 발생한다")
        void failWithNullQuantity() {
            Product product = createProduct();

            assertThatThrownBy(() -> product.increaseStock(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 필수");
        }

        @Test
        @DisplayName("수량이 0이면 예외가 발생한다")
        void failWithZeroQuantity() {
            Product product = createProduct();

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
            Product product = createProduct();

            product.decreaseStock(3);
            assertThat(product.getStockQuantity()).isEqualTo(DEFAULT_STOCK - 3);

            product.increaseStock(3);
            assertThat(product.getStockQuantity()).isEqualTo(DEFAULT_STOCK);
        }
    }
}
