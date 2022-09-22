package org.example.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class App implements CommandLineRunner
{
    @Autowired
    private CustomerRepository repo;

    public static void main( String[] args )
    {
        ConfigurableApplicationContext
            ctx = SpringApplication.run(App.class, args);
        SpringApplication.exit(ctx, () -> 0);
        ctx.close();
        System.exit(0);
    }

    @Override
    public void run(String... args) throws Exception {

        repo.deleteAll();

        Customer s1 = new Customer();
        s1.firstName = "John";
        s1.lastName = "Doe";

        repo.save(s1);
        System.out.println("\nsaved: " + s1); // customerId contains generated value

        Customer s2 = new Customer();
        s2.firstName = "John";
        s2.lastName = "Smith";

        repo.save(s2);
        System.out.println("\nsaved: " + s2); // customerId contains generated value

        System.out.println("\nfindAll:");
        Iterable<Customer> customers = repo.findAll();

        for (Customer s : customers) {
            System.out.println("  Customer: " + s);
        }

        System.out.println("\nfindByLastName: Smith");
        customers = repo.findByLastName("Smith");

        for (Customer s : customers) {
            System.out.println("  Customer: " + s);
        }
    }
}
