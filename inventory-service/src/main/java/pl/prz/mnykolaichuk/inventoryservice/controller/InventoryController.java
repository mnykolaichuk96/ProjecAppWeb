package pl.prz.mnykolaichuk.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.prz.mnykolaichuk.inventoryservice.dto.InventoryRequest;
import pl.prz.mnykolaichuk.inventoryservice.dto.InventoryResponse;
import pl.prz.mnykolaichuk.inventoryservice.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCodeList) {
        return inventoryService.isInStock(skuCodeList);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeInventory(@RequestBody List<InventoryRequest> inventoryRequestList) {
        inventoryService.placeInventory(inventoryRequestList);
        return "Inventory Placed Successfully";
    }
}
