package pl.prz.mnykolaichuk.inventoryservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.prz.mnykolaichuk.inventoryservice.dto.InventoryRequest;
import pl.prz.mnykolaichuk.inventoryservice.dto.InventoryResponse;
import pl.prz.mnykolaichuk.inventoryservice.model.Inventory;

import java.util.Arrays;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class InventoryServiceApplicationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private final static Network network = Network.newNetwork();

    @Container
    private static final MySQLContainer<?> mySQLContainerInventoryService;

    static {
        mySQLContainerInventoryService = new MySQLContainer<>(
                "mysql:8.0.32")
                .withDatabaseName("inventory-service")
                .withUsername("kolya5179596")
                .withPassword("Rjkz0985179596")
                .withNetwork(network);
    }
    @DynamicPropertySource
    static void mySQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainerInventoryService::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainerInventoryService::getUsername);
        registry.add("spring.datasource.password", mySQLContainerInventoryService::getPassword);
        registry.add("spring.datasource.driver-class-name", mySQLContainerInventoryService::getDriverClassName);
    }

    @Test
    void shouldPlaceInventory() throws Exception {
        String inventoryRequestListString =
                Arrays.stream(new InventoryRequest[]{
                        InventoryRequest.builder()
                                .skuCode("iphone_13")
                                .quantity(50).build(),
                        InventoryRequest.builder()
                                .skuCode("iphone_13_red")
                                .quantity(0).build(),
                        InventoryRequest.builder()
                                .skuCode("iphone_14")
                                .quantity(2).build(),
                        InventoryRequest.builder()
                                .skuCode("iphone_14_red")
                                .quantity(1).build()
                }).map(inventoryRequest -> {
                    try {
                        return objectMapper.writeValueAsString(inventoryRequest);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).toList().toString();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventoryRequestListString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("Inventory Placed Successfully"));

    }
    @Test
    @Transactional
    void shouldReturnIsInStock() throws Exception {

        Inventory inventory1 = Inventory.builder()
                .skuCode("iphone_13")
                .quantity(50)
                .build();
        Inventory inventory2 = Inventory.builder()
                .skuCode("iphone_13_red")
                .quantity(0)
                .build();

        /** Only if shouldPlaceInventory() wasn't start
        inventoryRepository.save(inventory1);
        inventoryRepository.save(inventory2);
         */
        mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory?skuCodeList=iphone_13")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        "[" +
                                objectMapper.writeValueAsString(
                                        InventoryResponse.builder()
                                                .skuCode(inventory1.getSkuCode())
                                                .isInStock(true).build())
                                + "]"
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory?skuCodeList=iphone_13_red")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        "[" +
                                objectMapper.writeValueAsString(
                                        InventoryResponse.builder()
                                                .skuCode(inventory2.getSkuCode())
                                                .isInStock(false).build())
                                + "]"
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory?skuCodeList=iphone_13, iphone_13_red")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        "[" +
                                objectMapper.writeValueAsString(
                                        InventoryResponse.builder()
                                                .skuCode(inventory1.getSkuCode())
                                                .isInStock(true).build())
                                + "," +
                                objectMapper.writeValueAsString(
                                        InventoryResponse.builder()
                                                .skuCode(inventory2.getSkuCode())
                                                .isInStock(false).build())
                                + "]"
                ));
    }
}
