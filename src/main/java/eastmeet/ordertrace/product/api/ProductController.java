package eastmeet.ordertrace.product.api;

import eastmeet.ordertrace.product.api.dto.ProductResponse;
import eastmeet.ordertrace.product.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        List<ProductResponse> responses = productService.getAllProducts()
            .stream()
            .map(ProductResponse::from)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        ProductResponse response = ProductResponse.from(productService.getProductById(id));
        return ResponseEntity.ok(response);
    }

}
