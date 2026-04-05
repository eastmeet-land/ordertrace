package eastmeet.ordertrace.product.service;

import static org.assertj.core.api.Assertions.assertThat;

import eastmeet.ordertrace.product.domain.Product;
import eastmeet.ordertrace.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest // JPA 관련 빈만 로드하는 슬라이스 테스트 (Repository, EntityManager 등)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 내장 DB 대신 실제 DB(application.yml) 사용
@Transactional(propagation = Propagation.NOT_SUPPORTED) // 테스트 레벨 트랜잭션 비활성화 — 멀티스레드에서 커밋된 데이터를 볼 수 있도록
@Import(StockConcurrencyTest.TestConfig.class) // TransactionTemplate 빈 수동 등록 (@DataJpaTest 슬라이스에 포함되지 않으므로)
@Slf4j
class StockConcurrencyTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    @AfterEach
    void cleanup() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("락 없이 동시 재고 차감 시 Lost Update 문제가 발생한다")
    void concurrentStockDecrease_withoutLock_causesLostUpdate() throws InterruptedException {
        // given - 재고 5개 상품
        int initialStock = 5;
        Long productId = transactionTemplate.execute(status -> {
            Product product = new Product("테스트 상품", "설명", BigDecimal.valueOf(1000), initialStock);
            return productRepository.save(product).getId();
        });

        int threadCount = 10;
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            // when - 10개 스레드가 동시에 재고 1씩 차감 시도
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        transactionTemplate.executeWithoutResult(status -> {
                            Product product = productRepository.findById(productId).orElseThrow();
                            product.decreaseStock(1);
                        });
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // 모든 스레드 동시 출발
            doneLatch.await();
            executor.shutdown();

            // then
            int finalStock = transactionTemplate.execute(status ->
                productRepository.findById(productId).orElseThrow().getStockQuantity()
            );

            log.info("=== 동시성 테스트 결과 ===");
            log.info("초기 재고: {}, 성공: {}, 실패: {}, 최종 재고: {}, 예상 최종 재고: {}",
                initialStock, successCount.get(), failCount.get(), finalStock, initialStock - successCount.get());

            // Lost Update: 성공 횟수만큼 재고가 줄어야 하는데 실제로는 다름
            assertThat(finalStock)
                .as("Lost Update 발생: 성공 %d회인데 재고가 기대값(%d)과 다름",
                    successCount.get(), initialStock - successCount.get())
                .isNotEqualTo(initialStock - successCount.get());
        }
    }

    @Test
    @DisplayName("비관적 락 적용 시 동시 재고 차감이 정확하게 처리")
    void concurrentStockDecrease_withPessimisticLock_isCorrect() throws InterruptedException {
        // given - 재고 5개 상품
        int initialStock = 5;
        Long productId = transactionTemplate.execute(status -> {
            Product product = new Product("테스트 상품", "설명", BigDecimal.valueOf(1000), initialStock);
            return productRepository.save(product).getId();
        });

        int threadCount = 10;
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            // when - 10개 스레드가 동시에 재고 1씩 차감 (비관적 락 사용)
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        transactionTemplate.executeWithoutResult(status -> {
                            List<Product> products = productRepository.findAllByIdForUpdate(List.of(productId));
                            products.getFirst().decreaseStock(1);
                        });
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // 모든 스레드 동시 출발
            doneLatch.await();
            executor.shutdown();

            // then
            Integer finalStock = transactionTemplate.execute(status ->
                productRepository.findById(productId).orElseThrow().getStockQuantity()
            );

            log.info("=== 비관적 락 동시성 테스트 결과 ===");
            log.info("초기 재고: {}, 성공: {}, 실패: {}, 최종 재고: {}",
                initialStock, successCount.get(), failCount.get(), finalStock);

            // 5개만 성공, 5개는 재고 부족으로 실패
            assertThat(successCount.get()).isEqualTo(initialStock);
            assertThat(failCount.get()).isEqualTo(threadCount - initialStock);
            assertThat(finalStock).isEqualTo(0);
        }
    }

    static class TestConfig {
        @Bean
        TransactionTemplate transactionTemplate(PlatformTransactionManager txManager) {
            return new TransactionTemplate(txManager);
        }
    }
}