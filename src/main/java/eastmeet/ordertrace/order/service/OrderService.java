package eastmeet.ordertrace.order.service;

import eastmeet.ordertrace.global.domain.Currency;
import eastmeet.ordertrace.order.domain.Order;
import eastmeet.ordertrace.order.domain.OrderItem;
import eastmeet.ordertrace.order.event.OrderCancelledEvent;
import eastmeet.ordertrace.order.event.OrderCreatedEvent;
import eastmeet.ordertrace.order.repository.OrderRepository;
import eastmeet.ordertrace.product.domain.Product;
import eastmeet.ordertrace.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order createOrder(Long memberId, Long productId, Integer quantity, Currency currency, String scenario) {
        Product product = productService.findById(productId);
        product.decreaseStock(quantity);

        Order order = new Order(memberId, currency);

        OrderItem orderItem = new OrderItem(
            product.getId(),
            quantity,
            product.getPrice()
        );

        order.addOrderItem(orderItem);
        orderRepository.save(order);

        order.markPaymentPending();

        eventPublisher.publishEvent(new OrderCreatedEvent(
            order.getId(),
            order.getTotalAmount(),
            order.getCurrency(),
            scenario
        ));

        log.info("주문 생성 및 결제 요청 - orderId: {}", order.getId());
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

        eventPublisher.publishEvent(new OrderCancelledEvent(order.getId()));
        log.info("주문 취소 - orderId: {}", id);
    }

    @Transactional
    public void confirmOrder(Long id) {
        Order order = getOrderById(id);
        order.markConfirmed();
        log.info("주문 확정 - orderId: {}", id);
    }

    @Transactional
    public void failOrder(Long id) {
        Order order = getOrderById(id);
        order.markFailed();
        log.info("주문 실패 - orderId: {}", id);
    }

    @Transactional
    public void restoreStock(Long orderId) {
        Order order = getOrderWithItemsByOrderId(orderId);
        order.getOrderItems().forEach(item -> {
            Product product = productService.findById(item.getProductId());
            product.increaseStock(item.getQuantity());
        });
        log.info("재고 복원 완료 - orderId: {}", orderId);
    }

}
