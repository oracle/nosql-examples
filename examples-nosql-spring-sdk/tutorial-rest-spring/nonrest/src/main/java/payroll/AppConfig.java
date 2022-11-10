package payroll;

import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.Region;
import oracle.nosql.driver.iam.SignatureProvider;
import com.oracle.nosql.spring.data.config.AbstractNosqlConfiguration;
import com.oracle.nosql.spring.data.config.NosqlDbConfig;
import com.oracle.nosql.spring.data.repository.config.EnableNosqlRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;
    @Configuration
    @EnableNosqlRepositories(basePackageClasses = EmployeeRepository.class)
    public class AppConfig extends AbstractNosqlConfiguration {  
        @Bean
        public NosqlDbConfig nosqlDbConfig() 
            throws java.io.IOException {

            /* Config for cloud service using instance principal. */                         
            SignatureProvider provider = SignatureProvider.createWithInstancePrincipal();
   
            /* Use the same region your instance VM runs in. */ 
            NoSQLHandleConfig config = new NoSQLHandleConfig(Region.US_ASHBURN_1, provider);

            config.setDefaultCompartment("ocid1.compartment.oc1..aaaaaaaa4mlehopmvdluv2wjcdp4tnh2ypjz3nhhpahb4ss7yvxaa3be3diq");
            return new NosqlDbConfig(config);
        }
    }
