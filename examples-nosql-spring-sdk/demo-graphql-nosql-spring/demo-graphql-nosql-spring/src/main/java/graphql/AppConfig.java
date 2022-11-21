package graphql;

import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.Region;
import oracle.nosql.driver.iam.SignatureProvider;
import com.oracle.nosql.spring.data.config.AbstractNosqlConfiguration;
import com.oracle.nosql.spring.data.config.NosqlDbConfig;
import com.oracle.nosql.spring.data.repository.config.EnableNosqlRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;
import org.springframework.beans.factory.annotation.Value;

    @Configuration
    @EnableNosqlRepositories
    public class AppConfig extends AbstractNosqlConfiguration {  
        @Value("${nosql.ociregion}")
        private String OCI_REGION;
        @Value("${nosql.ocid_comp}")
        private String OCI_COMPID;
        @Bean
        public NosqlDbConfig nosqlDbConfig() 
            throws java.io.IOException {
              
            /* Config for cloud service using instance principal. */                         
            SignatureProvider provider = SignatureProvider.createWithInstancePrincipal();
   
            /* Use the same region your instance VM runs in. */ 
            //NoSQLHandleConfig config = new NoSQLHandleConfig(Region.US_ASHBURN_1, provider);
            NoSQLHandleConfig config = new NoSQLHandleConfig(OCI_REGION, provider);

            config.setDefaultCompartment(OCI_COMPID);
            System.out.println (OCI_REGION);
            System.out.println (OCI_COMPID);
            return new NosqlDbConfig(config);
        }
    }
