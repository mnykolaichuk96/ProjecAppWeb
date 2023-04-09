package pl.prz.mnykolaichuk.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.prz.mnykolaichuk.orderservice.dto.OrderLineItemsDto;
import pl.prz.mnykolaichuk.orderservice.dto.OrderRequest;
import pl.prz.mnykolaichuk.orderservice.model.Order;
import pl.prz.mnykolaichuk.orderservice.model.OrderLineItems;
import pl.prz.mnykolaichuk.orderservice.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    public void placeOrder(OrderRequest orderRequest){
        List<OrderLineItems> orderLineItems =  orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto).toList();

        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .orderLineItemsList(orderLineItems)
                .build();

        orderRepository.save(order);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        return OrderLineItems.builder()
                .id(orderLineItemsDto.getId())
                .skuCode(orderLineItemsDto.getSkuCode())
                .price(orderLineItemsDto.getPrice())
                .quantity(orderLineItemsDto.getQuantity())
                .build();
    }

}
