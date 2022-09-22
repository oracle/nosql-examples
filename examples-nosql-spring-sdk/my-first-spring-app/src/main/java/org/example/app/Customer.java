package org.example.app;

import com.oracle.nosql.spring.data.core.mapping.NosqlId;

public class Customer {
    @NosqlId(generated = true)
    long customerId;
    String firstName;
    String lastName;

    @Override
    public String toString() {
        return "Customer{" +
            "customerId=" + customerId +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            '}';
    }
}
