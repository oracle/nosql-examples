package io.helidon.examples.quickstart.se;

import java.util.List;

public interface GreetRepo {
    //void createTable();

    Greet save(Greet greet);
    Greet findById(Integer id);
    List<Greet> findAll() ;

}
