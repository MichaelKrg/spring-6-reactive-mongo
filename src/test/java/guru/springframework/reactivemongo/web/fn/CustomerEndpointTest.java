package guru.springframework.reactivemongo.web.fn;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import guru.springframework.reactivemongo.domain.Customer;
import guru.springframework.reactivemongo.model.CustomerDTO;
import guru.springframework.reactivemongo.services.CustomerServiceImplTest;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@Testcontainers
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureWebTestClient
public class CustomerEndpointTest {
    @Container
    @ServiceConnection
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

   // @Autowired did not work with WebTestClient when using @SpringBootTest,
    // so we need to set up manually
    WebTestClient webTestClient;

    @Autowired
    ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
                .bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    void testPatchBadRequest() {
        CustomerDTO customerDTO = getSavedTestCustomer();
        customerDTO.setCustomerName("AA"); // intentionally short, should trigger error in validation

        webTestClient.mutateWith(mockOAuth2Login())
                .patch()
                .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerDTO.getId())
                .body(Mono.just(customerDTO), CustomerDTO.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testPatchIdNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .patch()
                .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, 999)
                .body(Mono.just(CustomerServiceImplTest.getTestCustomer()), CustomerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(6)
    void testPatchIdFound() {
        CustomerDTO customerDTO = getSavedTestCustomer();

        webTestClient.mutateWith(mockOAuth2Login())
                .patch()
                .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerDTO.getId())
                .body(Mono.just(customerDTO), CustomerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testDeleteNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .delete()
                .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(999)
    void testDeleteCustomer() {
        CustomerDTO customerDTO = getSavedTestCustomer();

        webTestClient.mutateWith(mockOAuth2Login())
                .delete()
                .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerDTO.getId())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @Order(5)
    void testUpdateCustomerBadRequest() {
        CustomerDTO testCustomer = getSavedTestCustomer();
        testCustomer.setCustomerName("");

        webTestClient.mutateWith(mockOAuth2Login())
                .put()
                .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, testCustomer)
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateCustomerNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .put()
                .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, 999)
                .body(Mono.just(CustomerServiceImplTest.getTestCustomer()), CustomerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(4)
    void testUpdateCustomer() {

        CustomerDTO customerDTO = getSavedTestCustomer();
        customerDTO.setCustomerName("New");

        webTestClient.mutateWith(mockOAuth2Login())
		.put()
                .uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerDTO.getId())
                .body(Mono.just(customerDTO), CustomerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testCreateCustomerBadData() {
        Customer testCustomer = CustomerServiceImplTest.getTestCustomer();
        testCustomer.setCustomerName("");

        webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(CustomerRouterConfig.CUSTOMER_PATH)
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(2)
    void testCreateCustomer() {
        CustomerDTO testDto = getSavedTestCustomer();

        webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(CustomerRouterConfig.CUSTOMER_PATH)
                .body(Mono.just(testDto), CustomerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location");
    }

    @Test
    void testGetByIdNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .get().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(1)
    void testGetById() {
        CustomerDTO customerDTO = getSavedTestCustomer();

        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, customerDTO.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(CustomerDTO.class);
    }

    @Test
    @Order(3)
    void testListCustomers() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .get().uri(CustomerRouterConfig.CUSTOMER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(List.class).value(value -> assertThat(value).hasSizeGreaterThan(1));
    }

    public CustomerDTO getSavedTestCustomer(){
        FluxExchangeResult<CustomerDTO> customerDTOFluxExchangeResult = webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(CustomerRouterConfig.CUSTOMER_PATH)
                .body(Mono.just(CustomerServiceImplTest.getTestCustomer()), CustomerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .returnResult(CustomerDTO.class);

        List<String> location = customerDTOFluxExchangeResult.getResponseHeaders().get("Location");

        return webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(CustomerRouterConfig.CUSTOMER_PATH)
                .exchange().returnResult(CustomerDTO.class).getResponseBody().blockFirst();
    }
    
}
