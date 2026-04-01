package eastmeet.ordertrace.order.repository;

import eastmeet.ordertrace.order.domain.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"orderItems"})
    Optional<Order> findById(Long id);


}
