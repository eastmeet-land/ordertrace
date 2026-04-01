package eastmeet.ordertrace.order.sevice;

import eastmeet.ordertrace.order.domain.Order;
import eastmeet.ordertrace.order.domain.OrderItem;
import eastmeet.ordertrace.order.repository.OrderRepository;
import eastmeet.ordertrace.product.domain.Product;
import eastmeet.ordertrace.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Transactional
    public Order createOrder(Long memberId, Long productId, Integer quantity) {
        Product product = productService.findById(productId);

        Order order = new Order(memberId, "KRW");

        OrderItem orderItem = new OrderItem(
            product.getId(),
            quantity,
            product.getPrice()
        );
        order.addOrderItem(orderItem);

        return orderRepository.save(order);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "주문을 찾을 수 없습니다. id: " + id));
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = findById(id);
        order.cancel();
    }

}
