package guru.springframework.reactivemongo.repositories;

import guru.springframework.reactivemongo.domain.Customer;
import reactor.core.publisher.Mono;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Created by jt, Spring Framework Guru.
 */
public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {
    Mono<Customer> findFirstByCustomerName(String customerName);

}
