package payroll;

import com.oracle.nosql.spring.data.repository.NosqlRepository;


interface EmployeeRepository extends NosqlRepository<Employee, Long> {

}
