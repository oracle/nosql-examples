package org.example.app;

import com.oracle.nosql.spring.data.repository.NosqlRepository;

public interface CustomerRepository
    extends NosqlRepository<Customer, Long>
{
    Iterable<Customer> findByLastName(String lastname);
}
