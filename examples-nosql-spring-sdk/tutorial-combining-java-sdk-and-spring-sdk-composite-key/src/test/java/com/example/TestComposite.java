package com.example;

import com.oracle.nosql.spring.data.NosqlDbFactory;
import com.oracle.nosql.spring.data.repository.config.EnableNosqlRepositories;
import oracle.nosql.driver.NoSQLHandle;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@EnableNosqlRepositories(basePackageClasses = {CustomerRepository.class})
public class TestComposite {

    @Autowired
    CustomerRepository customerRepo;

    @Test
    public void testCustomer() {
        customerRepo.deleteAll();

        Customer s1 = new Customer();
        s1.firstName = "John";
        s1.lastName = "Doe";

        customerRepo.save(s1);
        System.out.println("\nsaved: " + s1); // customerId contains generated value

        Customer s2 = new Customer();
        s2.firstName = "John";
        s2.lastName = "Smith";

        customerRepo.save(s2);
        System.out.println("\nsaved: " + s2); // customerId contains generated value

        System.out.println("\nfindAll:");
        Iterable<Customer> customers = customerRepo.findAll();

        for (Customer s : customers) {
            System.out.println("  Customer: " + s);
        }

        System.out.println("\nfindByLastName: Smith");
        customers = customerRepo.findByLastName("Smith");

        for (Customer s : customers) {
            System.out.println("  Customer: " + s);
        }
    }

    @Test
    public void testComposite() {
        NosqlDbFactory nosqlDbFactory = new NosqlDbFactory(
            AppConfig.nosqlDBConfig );
        NoSQLHandle client = nosqlDbFactory.getNosqlClient();
        MachineRepo machineRepo = new MachineRepoImpl(client, AppConfig.nosqlDBConfig );
        // Not automatically managed
        machineRepo.createTable();

        machineRepo.deleteAll();
        System.out.println("count: " + machineRepo.count());

        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 4; j++) {
                MachineId machineId = new MachineId();
                machineId.setName("name " +i);
                machineId.setVersion("version " + j);
                Machine machine = new Machine();
                machine.setMachineId(machineId);
                machine.setName("d " + i + " " + j);

                machineRepo.save(machine);
            }
        }

        MachineId compKey = new MachineId();
        compKey.setName("name 1");
        compKey.setVersion("version 1");


        Machine machine = machineRepo.findById(compKey);
        System.out.println("findById: " + machine.toString() );
        Assert.assertNotNull(machine);
        Assert.assertEquals("d 1 1", machine.getName());

        machineRepo.delete(machine);


        Machine machine2 = machineRepo.findById(compKey);
        Assert.assertNull(machine2);


        compKey.setVersion("version 2");
        machineRepo.deleteById(compKey);
        Machine machine3 = machineRepo.findById(compKey);
        Assert.assertNull(machine3);


        Iterable<Machine> all = machineRepo.findAll();
        int manualCount = 0;
        for (Machine c : all) {
            manualCount++;
            System.out.println("findAll: " + c.toString() );
            Assert.assertTrue(c.getName().startsWith("d "));
        }
        Assert.assertEquals(9 - 2, manualCount);


        long repoCount = machineRepo.count();
        Assert.assertEquals(manualCount, repoCount);
        System.out.println("count: " + repoCount);


        Iterable<Machine> findByMachineIdNameRegexpIgnoreCaseQuery =
            machineRepo.findByMachineIdNameRegexpIgnoreCase("NaMe 2");

        for (Machine c : findByMachineIdNameRegexpIgnoreCaseQuery) {
            System.out.println("findByMachineIdNameRegexpIgnoreCaseQuery: " +
                c.toString() );
        }


        List<Sort.Order> sortList = new ArrayList<>();
        sortList.add(new Sort.Order(Sort.Direction.DESC, "version"));
        sortList.add(new Sort.Order(Sort.Direction.ASC, "creationDate"));
        Iterable<Machine> findAllSortQuery = machineRepo.findAll(Sort.by(sortList));

        for (Machine c : findAllSortQuery) {
            System.out.println("findAllSortQuery: " + c.toString() );
        }


        Pageable pageable = PageRequest.of(1, 3, Sort.by("name", "version"));
        Page<Machine> pageQuery = machineRepo.findAll(pageable);

        for (Machine c : pageQuery) {
            System.out.println("pageQuery: " + c.toString() );
        }


        pageable = PageRequest.of(1, 2,
            Sort.by(Sort.Direction.DESC, "version"));
        Page<Machine> pageByNameQuery =
            machineRepo.findAllByMachineIdName("name 2", pageable);

        for (Machine c : pageByNameQuery) {
            System.out.println("pageByNameQuery: " + c.toString() );
        }
    }
}
