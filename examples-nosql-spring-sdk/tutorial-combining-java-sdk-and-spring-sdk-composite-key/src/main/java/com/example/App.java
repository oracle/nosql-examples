package com.example;

import com.oracle.nosql.spring.data.NosqlDbFactory;
import oracle.nosql.driver.NoSQLHandle;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootApplication
// If Spring Data Framework doesn't automatically find the repository bean class
// uncomment the following line:
// @EnableNosqlRepositories(basePackageClasses = {CustomerRepository.class})
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
    public void run(String... args) {
        runCustomer();

        runMachine();
    }

    public void runCustomer() {
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

    public void runMachine() {
        NosqlDbFactory nosqlDbFactory = new NosqlDbFactory(
            AppConfig.nosqlDBConfig );
        NoSQLHandle client = nosqlDbFactory.getNosqlClient();
        MachineRepo machineRepo = new MachineRepoImpl(client, AppConfig.nosqlDBConfig );
        // Not automatically managed
        machineRepo.createTable();

        machineRepo.deleteAll();
        System.out.println("count: " + machineRepo.count());

        HashMap<String, String> settings = new HashMap<String, String>();
        settings.put("keyboard", "French");
        settings.put("size", "4");
        settings.put("color", "Grey");

        ArrayList<Transition> transitions = new ArrayList<Transition>();
        Transition trans = new Transition();
        trans.setSource("source");
        trans.setDestination("destination");
        trans.setAction("ONE");
        transitions.add(trans);
        trans = new Transition();
        trans.setSource("source");
        trans.setDestination("destination");
        trans.setAction("TWO");
        transitions.add(trans);
        trans = new Transition();
        trans.setSource("source");
        trans.setDestination("destination");
        trans.setAction("THREE");
        transitions.add(trans);

        List<MachineId> childs = new ArrayList<MachineId>();
        MachineId child = new MachineId();
        child.setName("child 0" );
        child.setVersion("version 0" );
        childs.add(child);
        child = new MachineId();
        child.setName("child 1" );
        child.setVersion("version 0" );
        childs.add(child);

        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 4; j++) {
                MachineId machineId = new MachineId();
                machineId.setName("name " + i);
                machineId.setVersion("version " + j);
                Machine machine = new Machine();
                machine.setMachineId(machineId);
                machine.setName("d " + i + " " + j);
                machine.setSetting (settings);
                machine.setTransitions (transitions);
                machine.setChilds (childs);                
                machineRepo.save(machine);
            }
        }

        MachineId machineId = new MachineId();
        machineId.setName("name 1");
        machineId.setVersion("version 1");

        Machine machine = machineRepo.findById(machineId);
        System.out.println("findById: " + machine.toString() );
        Assert.assertNotNull(machine);
        Assert.assertEquals("d 1 1", machine.getName());

        machineRepo.delete(machine);

        Machine machine2 = machineRepo.findById(machineId);
        Assert.assertNull(machine2);

        machineId.setName("name 3");
        machineRepo.deleteById(machineId);
        Machine machine3 = machineRepo.findById(machineId);
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
