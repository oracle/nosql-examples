package com.example;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface MachineRepo {

    void createTable();

    Machine save(Machine machine);
    void deleteById(MachineId machineId);
    void delete(Machine machine);
    void deleteAll();
    Machine findById(MachineId machineId);
    long count();
    Iterable<Machine> findAll();
    Iterable<Machine> findAll(Sort sort);
    Page<Machine> findAll(Pageable pageable);

    Iterable<Machine> findByMachineIdNameRegexpIgnoreCase(String name);
    Page<Machine> findAllByMachineIdName(String name, Pageable pageable);
}
