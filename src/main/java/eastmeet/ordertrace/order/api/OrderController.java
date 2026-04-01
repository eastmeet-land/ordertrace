package eastmeet.ordertrace.order.api;

import eastmeet.ordertrace.order.api.dto.OrderCreateResponse;
import eastmeet.ordertrace.order.api.dto.OrderRequest;
import eastmeet.ordertrace.order.api.dto.OrderResponse;
import eastmeet.ordertrace.order.domain.Order;
import eastmeet.ordertrace.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        Order order = orderService.createOrder(
            request.memberId(),
            request.productId(),
            request.quantity(),
            request.currency(),
            request.scenario().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderCreateResponse.from(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderWithItemsByOrderId(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

}
