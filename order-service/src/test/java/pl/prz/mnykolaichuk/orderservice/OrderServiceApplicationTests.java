package pl.prz.mnykolaichuk.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import org.hibernate.cfg.Configuration;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.prz.mnykolaichuk.orderservice.dto.OrderLineItemsDto;
import pl.prz.mnykolaichuk.orderservice.dto.OrderRequest;
import pl.prz.mnykolaichuk.orderservice.model.Order;
import pl.prz.mnykolaichuk.orderservice.model.OrderLineItems;
import pl.prz.mnykolaichuk.orderservice.repository.OrderRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class OrderServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private OrderRepository orderRepository;
	private static final int MYSQL_PORT = 3306;
	@Container
	private static final MySQLContainer<?> mySQLContainer;

	static {
		mySQLContainer = new MySQLContainer<>("mysql:8.0.32")
				.withDatabaseName("order-service")
				.withUsername("kolya5179596")
				.withPassword("Rjkz0985179596")
				.withExposedPorts(MYSQL_PORT);
	}

	@DynamicPropertySource
	static void mySQLProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mySQLContainer::getUsername);
		registry.add("spring.datasource.password", mySQLContainer::getPassword);
		registry.add("spring.datasource.driver-class-name", mySQLContainer::getDriverClassName);
	}
	@Test
	@Transactional
	void shouldPlaceOrder() throws Exception {
		Configuration config = new Configuration()
				.setProperty("hibernate.connection.driver_class", mySQLContainer.getDriverClassName())
				.setProperty("hibernate.connection.url", mySQLContainer.getJdbcUrl())
				.setProperty("hibernate.connection.username", mySQLContainer.getUsername())
				.setProperty("hibernate.connection.password", mySQLContainer.getPassword())
				.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
				.setProperty("hibernate.hbm2ddl.auto", "create-drop")
				.addAnnotatedClass(Order.class)
				.addAnnotatedClass(OrderLineItems.class);

		SessionFactory sessionFactory = config.buildSessionFactory();
		Session session = sessionFactory.openSession();

		OrderRequest orderRequest = OrderRequest.builder()
				.orderLineItemsDtoList(getOrderLineItemsDtoList())
				.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderRequest)))
				.andExpect(MockMvcResultMatchers.status().isCreated());

		Assertions.assertEquals(1, orderRepository.findAll().size());

		session.close();
	}

	private List<OrderLineItemsDto> getOrderLineItemsDtoList() {
		return List.of(OrderLineItemsDto.builder()
				.skuCode("iphone_13")
				.price(BigDecimal.valueOf(1200))
				.quantity(1)
				.build());
	}

}
