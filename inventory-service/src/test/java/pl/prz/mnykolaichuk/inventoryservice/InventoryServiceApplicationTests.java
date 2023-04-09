package pl.prz.mnykolaichuk.inventoryservice;

import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
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
import pl.prz.mnykolaichuk.inventoryservice.model.Inventory;
import pl.prz.mnykolaichuk.inventoryservice.repository.InventoryRepository;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class InventoryServiceApplicationTests {
	@Autowired
	private InventoryRepository inventoryRepository;
	@Autowired
	private MockMvc mockMvc;
	private static final int MYSQL_PORT = 3306;
	@Container
	private static final MySQLContainer<?> mySQLContainer;

	static {
		mySQLContainer = new MySQLContainer<>("mysql:8.0.32")
				.withDatabaseName("inventory-service")
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
	void shouldReturnIsInStock() throws Exception {
		Configuration config = new Configuration()
				.setProperty("hibernate.connection.driver_class", mySQLContainer.getDriverClassName())
				.setProperty("hibernate.connection.url", mySQLContainer.getJdbcUrl())
				.setProperty("hibernate.connection.username", mySQLContainer.getUsername())
				.setProperty("hibernate.connection.password", mySQLContainer.getPassword())
				.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
				.setProperty("hibernate.hbm2ddl.auto", "create-drop")
				.addAnnotatedClass(Inventory.class);

		SessionFactory sessionFactory = config.buildSessionFactory();
		Session session = sessionFactory.openSession();

		Inventory inventory1 = Inventory.builder()
				.skuCode("iphone_13")
				.quantity(100)
				.build();
		Inventory inventory2 = Inventory.builder()
				.skuCode("iphone_13_red")
				.quantity(0)
				.build();
		inventoryRepository.save(inventory1);
		inventoryRepository.save(inventory2);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/iphone_13")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/iphone_13_red")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("true"));

		session.close();
	}

}
