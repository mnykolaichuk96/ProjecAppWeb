package pl.prz.mnykolaichuk.productwebservice;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.prz.mnykolaichuk.productwebservice.dto.ProductRequest;
import pl.prz.mnykolaichuk.productwebservice.dto.ProductResponse;
import pl.prz.mnykolaichuk.productwebservice.repository.ProductRepository;

import java.math.BigDecimal;

@SpringBootTest
//junit5 zrozumi ze bedzie wykorzystany testcontainer do testow
@Testcontainers
@AutoConfigureMockMvc
class ProductWebServiceApplicationTests {
	// junit understand that  this is container
	@Container

	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.4");

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ProductRepository productRepository;

	@DynamicPropertySource      // doda propierties dynamicznie do kontenera
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}
	/*
	 * Test uruchomi mongodb kontainer sciagajac image: mongodb:6.0.4
	 * Pobierze replikaset url i doda ten url do spring.data.mongodb.uri
	 * */
	@Test
	void shouldCreateProduct() throws Exception {
		String productRequestString = objectMapper.writeValueAsString(getProductRequest());
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(productRequestString))
				.andExpect(MockMvcResultMatchers.status().isCreated());

		Assertions.assertEquals(1, productRepository.findAll().size());
//        import org.assertj.core.api.Assertions;
//        Assertions.assertThat(productRepository.findAll().size()).isEqualTo(1);
	}

	@Test
	void shouldGetAllProduct() throws Exception {
		productRepository.save(getProductResponse().mapToProduct());
		Assertions.assertEquals(1, productRepository.findAll().size());
		mockMvc.perform(MockMvcRequestBuilders.get("/api/product")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(
						"[" + objectMapper.writeValueAsString(getProductResponse()) + "]"));
	}



	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name("iPhone 13")
				.description("iPhone 13")
				.price(BigDecimal.valueOf(1200))
				.build();
	}

	private ProductResponse getProductResponse() {
		return ProductResponse.builder()
				.id("1")
				.name("iPhone 13")
				.description("iPhone 13")
				.price(BigDecimal.valueOf(1200))
				.build();
	}

}
