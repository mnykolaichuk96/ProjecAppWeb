package pl.prz.mnykolaichuk.productwebservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Document(value = "product")    // mapowanie tabeli product
@AllArgsConstructor             // generowanie konstruktorów z parametrami
@NoArgsConstructor              // generowanie konstruktorów bez parametrów
@Builder                        //builder dodaje mozliwosc tworzenia obiektu przez wywolanie
                                // .builder i przekazanie wartosci pol jako parametrow:
                                // .builder.name(productRequest.getNane()) gdzie prReq -- dao class
@Data                           // generowanie getters setters toString hashCode
public class Product {
    @Id
    private String id;
    @Field(name = "name")
    private String name;
    @Field(name = "description")
    private String description;
    @Field(name = "price")
    private BigDecimal price;
}
