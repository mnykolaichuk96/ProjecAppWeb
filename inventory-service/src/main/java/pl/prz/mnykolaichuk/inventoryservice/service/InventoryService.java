package pl.prz.mnykolaichuk.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.prz.mnykolaichuk.inventoryservice.dto.InventoryRequest;
import pl.prz.mnykolaichuk.inventoryservice.dto.InventoryResponse;
import pl.prz.mnykolaichuk.inventoryservice.model.Inventory;
import pl.prz.mnykolaichuk.inventoryservice.repository.InventoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCodeList) {
        return inventoryRepository.findBySkuCodeIn(skuCodeList).stream()
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQuantity() > 0)
                                .build()
                ).toList();
    }

    public void placeInventory(List<InventoryRequest> inventoryRequestList) {
        for (InventoryRequest inventoryRequest: inventoryRequestList) {
            inventoryRepository.save(Inventory.builder()
                    .skuCode(inventoryRequest.getSkuCode())
                    .quantity(inventoryRequest.getQuantity()).build());
        }

    }
}
