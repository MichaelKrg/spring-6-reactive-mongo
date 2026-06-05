package guru.springframework.reactivemongo.services;

import org.springframework.stereotype.Service;

import guru.springframework.reactivemongo.domain.Customer;
import guru.springframework.reactivemongo.mappers.CustomerMapper;
import guru.springframework.reactivemongo.model.CustomerDTO;
import guru.springframework.reactivemongo.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;

    @Override
    public Mono<Void> deleteCustomerById(String customerId) {
        return customerRepository.deleteById(customerId);
    }

    @Override
    public Mono<CustomerDTO> findFirstByCustomerName(String customerName) {
        return customerRepository.findFirstByCustomerName(customerName)
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDTO> getById(String customerId) {
        return customerRepository.findById(customerId)
             .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Flux<CustomerDTO> listCustomers() {
        return customerRepository.findAll()
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDTO> patchCustomer(String customerId, CustomerDTO customerDTO) {
        return customerRepository.findById(customerId)
        .map(existingCustomer -> {
            if (customerDTO.getCustomerName() != null) {
                existingCustomer.setCustomerName(customerDTO.getCustomerName());
            }
            return existingCustomer;
        })
        .flatMap(customerRepository::save)
        .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDTO> saveCustomer(Mono<CustomerDTO> customerDTO) {
        return customerDTO.map(customerMapper::customerDtoToCustomer)
                .flatMap(customerRepository::save)
                .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDTO> saveCustomer(CustomerDTO customerDTO) {
        return customerRepository.save(customerMapper.customerDtoToCustomer(customerDTO))
                .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDTO> updateCustomer(String customerId, CustomerDTO customerDTO) {
        return customerRepository.findById(customerId)
                .map(existingCustomer -> {
                    existingCustomer.setCustomerName(customerDTO.getCustomerName());
                    return existingCustomer;
                })
                .flatMap(customerRepository::save)
                .map(customerMapper::customerToCustomerDto);
    }

}
