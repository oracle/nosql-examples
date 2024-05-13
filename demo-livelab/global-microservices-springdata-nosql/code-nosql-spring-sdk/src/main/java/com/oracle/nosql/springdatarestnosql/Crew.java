package com.oracle.nosql.springdatarestnosql;

import java.util.List;

class Crew {
  public List<String> names;
  public String job;

  Crew() {}

  Crew(List<String> names,  String job){
    this.names = names;
    this.job = job;
  }

}
