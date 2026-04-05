package eastmeet.ordertrace.product.repository;

import eastmeet.ordertrace.product.domain.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id In :ids order by p.id")
    List<Product> findAllByIdForUpdate(@Param("ids") List<Long> ids);

}
