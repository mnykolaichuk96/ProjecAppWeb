package pl.prz.mnykolaichuk.productwebservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.prz.mnykolaichuk.productwebservice.model.Product;

// JPA dla mongoDB
public interface ProductRepository extends MongoRepository<Product, String> {
}
