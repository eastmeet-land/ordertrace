package eastmeet.ordertrace.product.repository;

import eastmeet.ordertrace.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
