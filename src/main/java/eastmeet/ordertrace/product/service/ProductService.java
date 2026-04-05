package eastmeet.ordertrace.product.service;

import eastmeet.ordertrace.product.domain.Product;
import eastmeet.ordertrace.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("상품을 찾을 수 없습니다. id: " + id)
            );
    }

    public List<Product> getAllProductsByIdsForUpdate(List<Long> ids) {
        return productRepository.findAllByIdForUpdate(ids);
    }

}
