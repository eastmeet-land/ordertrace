package eastmeet.ordertrace.order.service;

import static eastmeet.ordertrace.global.config.KafkaConfig.ORDER_EVENTS_TOPIC;

import eastmeet.ordertrace.global.domain.Currency;
import eastmeet.ordertrace.global.event.EventPublisher;
import eastmeet.ordertrace.order.api.dto.OrderItemRequest;
import eastmeet.ordertrace.order.domain.Order;
import eastmeet.ordertrace.order.domain.OrderItem;
import eastmeet.ordertrace.order.domain.OrderStatus;
import eastmeet.ordertrace.order.event.OrderCancelledEvent;
import eastmeet.ordertrace.order.event.OrderCreatedEvent;
import eastmeet.ordertrace.order.repository.OrderRepository;
import eastmeet.ordertrace.product.domain.Product;
import eastmeet.ordertrace.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final EventPublisher eventPublisher;

    @Transactional
    public Order createOrder(Long memberId, List<OrderItemRequest> items, Currency currency, String scenario) {
        List<Long> productIds = items.stream()
            .map(OrderItemRequest::productId)
            .toList();

        Map<Long, Product> productMap = productService.findAllByIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        if (productMap.size() != productIds.size()) {
            throw new EntityNotFoundException("존재하지 않는 상품이 포함되어 있습니다.");
        }

        Order order = new Order(memberId, currency);

        items.forEach(item -> {
            Product product = productMap.get(item.productId());
            product.decreaseStock(item.quantity());

            OrderItem orderItem = new OrderItem(
                product.getId(),
                item.quantity(),
                product.getPrice()
            );
            order.addOrderItem(orderItem);
        });

        orderRepository.save(order);
        order.markPaymentPending();

        // 트랜잭션 커밋 후 Kafka 이벤트 발행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(
                    ORDER_EVENTS_TOPIC,
                    String.valueOf(order.getId()),
                    new OrderCreatedEvent(
                        order.getId(),
                        order.getTotalAmount(),
                        order.getCurrency(),
                        scenario
                    )
                );
            }
        });

        log.info("주문 생성 및 결제 요청 - orderId: {}, 상품 수: {}", order.getId(), items.size());
        return order;
    }

    public Order getOrderWithItemsByOrderId(Long orderId) {
        return orderRepository.findWithItemsById(orderId)
            .orElseThrow(
                () -> new EntityNotFoundException("주문을 찾을 수 없습니다. id: " + orderId)
            );
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(
                () -> new EntityNotFoundException("주문을 찾을 수 없습니다. id: " + orderId)
            );
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = getOrderById(id);
        order.cancel();

        // 트랜잭션 커밋 후 Kafka 이벤트 발행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(
                    ORDER_EVENTS_TOPIC,
                    String.valueOf(order.getId()),
                    new OrderCancelledEvent(order.getId())
                );
            }
        });

        log.info("주문 취소 - orderId: {}", id);
    }

    @Transactional
    public void confirmOrder(Long id) {
        Order order = getOrderById(id);
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.info("이미 확정된 주문 - orderId: {}", id);
            return;
        }
        order.markConfirmed();
        log.info("주문 확정 - orderId: {}", id);
    }

    @Transactional
    public void failOrderAndRestoreStock(Long id) {
        Order order = getOrderById(id);
        if (order.getStatus() == OrderStatus.FAILED) {
            log.info("이미 실패 처리된 주문 - orderId: {}", id);
            return;
        }
        order.markFailed();
        log.info("주문 실패 - orderId: {}", id);
        this.restoreStock(id);
    }

    @Transactional
    public void restoreStock(Long orderId) {
        Order order = getOrderWithItemsByOrderId(orderId);

        List<Long> productIds = order.getOrderItems().stream()
            .map(OrderItem::getProductId)
            .toList();

        Map<Long, Product> productMap = productService.findAllByIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        order.getOrderItems().forEach(item -> {
            Product product = productMap.get(item.getProductId());
            product.increaseStock(item.getQuantity());
        });

        log.info("재고 복원 완료 - orderId: {}", orderId);
    }

}
