package guru.springframework.reactivemongo.services;

import guru.springframework.reactivemongo.model.CustomerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<CustomerDTO> findFirstByCustomerName(String customerName);

    Flux<CustomerDTO> listCustomers();
    Mono<CustomerDTO> saveCustomer(Mono<CustomerDTO> CustomerDTO);

    Mono<CustomerDTO> saveCustomer(CustomerDTO CustomerDTO);
    Mono<CustomerDTO> getById(String CustomerId);

    Mono<CustomerDTO> updateCustomer(String CustomerId, CustomerDTO CustomerDTO);

    Mono<CustomerDTO> patchCustomer(String CustomerId, CustomerDTO CustomerDTO);

    Mono<Void> deleteCustomerById(String CustomerId);

}
