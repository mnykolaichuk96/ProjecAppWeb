package pl.prz.mnykolaichuk.productwebservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.prz.mnykolaichuk.productwebservice.dto.ProductRequest;
import pl.prz.mnykolaichuk.productwebservice.dto.ProductResponse;
import pl.prz.mnykolaichuk.productwebservice.model.Product;
import pl.prz.mnykolaichuk.productwebservice.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor    //для constructor injection полів щоб не писати конструктор вручну
@Slf4j                      // for logs
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(ProductRequest productRequest){
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();                                       //stworzy obiekt product zawierajacy przekazane pola

        productRepository.save(product);
        log.info("Product {} is saved", product.getId());   //lombok tworzy mozliwosc uzycia {} jako f"str" w python
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream().map(this::mapToProductResponse).toList();
        // map wykonuje przekazaną funkcje na wszystkich el
        // :: wywolanie funkcji do przekazania, jak w python str.toUpperCase
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }


}
