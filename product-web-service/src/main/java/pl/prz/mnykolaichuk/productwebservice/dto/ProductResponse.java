package pl.prz.mnykolaichuk.productwebservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.prz.mnykolaichuk.productwebservice.model.Product;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;

    public Product mapToProduct() {
        return Product.builder()
                .id(this.getId())
                .price(this.getPrice())
                .description(this.getDescription())
                .name(this.getName())
                .build();
    }
}
