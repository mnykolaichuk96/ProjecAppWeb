/*** Version with inventory service and inventoryService DB in containers from docker compose
 *      OrderService app and OrderService db start from test containers
 *      In @Test try to communicate with InventoryService and can't. Can communicate from host, Postman
 *          but can't from @Test method
 *      mockMvc.perform(MockMvcRequestBuilders.post(...) don't work with localhost:8082 or localhost :containerPort
 *      under docker compose containers proxy container
 *          and I can't communicate with containers inside from container outside
 *          */
package pl.prz.mnykolaichuk.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
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
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.prz.mnykolaichuk.orderservice.dto.OrderLineItemsDto;
import pl.prz.mnykolaichuk.orderservice.dto.OrderRequest;
import pl.prz.mnykolaichuk.orderservice.repository.OrderRepository;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class OrderServiceApplicationTests {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Container
    private static final MySQLContainer<?> mySQLContainerOrderService;
    @Container
    public static DockerComposeContainer dockerComposeContainer =
            new DockerComposeContainer(new File("docker-compose.yml"))
                    .withExposedService("inventory-service-db", 3306,
                            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(15)))
                    .withExposedService("inventory-service", 8082,
                            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(20)));

    static {
        mySQLContainerOrderService = new MySQLContainer<>(
                "mysql:8.0.32")
                .withDatabaseName("order-service")
                .withUsername("kolya5179596")
                .withPassword("Rjkz0985179596");
    }
    @DynamicPropertySource
    static void mySQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainerOrderService::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainerOrderService::getUsername);
        registry.add("spring.datasource.password", mySQLContainerOrderService::getPassword);
        registry.add("spring.datasource.driver-class-name", mySQLContainerOrderService::getDriverClassName);
    }


    @Test
    @Transactional
    void shouldPlaceOrder() throws Exception {

        String jdbcUrl = "jdbc:mysql://localhost:3308/inventory-service";
        Connection conn = DriverManager.getConnection(jdbcUrl, "kolya5179596", "Rjkz0985179596");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO `inventory-service`.inventory(sku_code, quantity) VALUES ('iphone_13', 50);");
        stmt.executeUpdate("INSERT INTO `inventory-service`.inventory(sku_code, quantity) VALUES ('iphone_13_red', 0);");
        stmt.executeUpdate("INSERT INTO `inventory-service`.inventory(sku_code, quantity) VALUES ('iphone_14', 2);");
        stmt.executeUpdate("INSERT INTO `inventory-service`.inventory(sku_code, quantity) VALUES ('iphone_14_red', 1);");

        /***
         * Hardcode qsl because can't call post localhost:8082/api/inventory
         *
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

        mockMvc.perform(MockMvcRequestBuilders.post(
                dockerComposeContainer.getServiceHost("inventory-service", 8082) +
                        dockerComposeContainer.getServicePort("inventory-service", 8082) +
                        "/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventoryRequestListString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("Inventory Placed Successfully"));
        */

        OrderRequest orderRequestFirst = OrderRequest.builder()
                .orderLineItemsDtoList(List.of(
                        OrderLineItemsDto.builder()
                                .skuCode("iphone_13")
                                .price(BigDecimal.valueOf(1200))
                                .quantity(20)
                                .build(),
                        OrderLineItemsDto.builder()
                                .skuCode("iphone_14")
                                .price(BigDecimal.valueOf(1200))
                                .quantity(1)
                                .build()))
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestFirst)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("Order Placed Successfully"));
        Assertions.assertEquals(1, orderRepository.findAll().size());


        OrderRequest orderRequestSecond = OrderRequest.builder()
                .orderLineItemsDtoList(List.of(
                        OrderLineItemsDto.builder()
                                .skuCode("iphone_13")
                                .price(BigDecimal.valueOf(1200))
                                .quantity(20)
                                .build(),
                        OrderLineItemsDto.builder()
                                .skuCode("iphone_13_red")
                                .price(BigDecimal.valueOf(1200))
                                .quantity(1)
                                .build(),
                        OrderLineItemsDto.builder()
                                .skuCode("iphone_14")
                                .price(BigDecimal.valueOf(1200))
                                .quantity(4)
                                .build(),
                        OrderLineItemsDto.builder()
                                .skuCode("iphone_14_red")
                                .price(BigDecimal.valueOf(1200))
                                .quantity(2)
                                .build()))
                .build();

          try {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequestSecond)));
        } catch (jakarta.servlet.ServletException msg) {
            System.out.println("Test past");
        }



    }
}
