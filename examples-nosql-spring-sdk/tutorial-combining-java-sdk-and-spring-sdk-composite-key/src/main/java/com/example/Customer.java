package com.example;

import java.util.Date;
import com.oracle.nosql.spring.data.core.mapping.NosqlId;
import com.oracle.nosql.spring.data.core.mapping.NosqlTable;

@NosqlTable(storageGB = 1, writeUnits = 10, readUnits = 10)
 public class Customer {
        @NosqlId(generated = true)
        long customerId;
        String firstName;
        String lastName;
        Date createdAt;

        @Override
        public String toString() {
            return "Customer{" +
               "customerId=" + customerId +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", createdAt='" + createdAt + '\'' +
               '}';
        }
}
