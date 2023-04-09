package pl.prz.mnykolaichuk.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private String orderNumber;
    private List<OrderLineItemsDto> orderLineItemsDtoList;
}
