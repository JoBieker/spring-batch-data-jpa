package nrw.bieker.demo.springbatchdatajpa.repositories.customers;

import nrw.bieker.demo.springbatchdatajpa.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByCustomerId(String customerId);
    Optional<Customer> findByCompanyName(String companyName);
}
