package org.example.app;

import com.oracle.nosql.spring.data.config.AbstractNosqlConfiguration;
import com.oracle.nosql.spring.data.config.NosqlDbConfig;
import com.oracle.nosql.spring.data.repository.config.EnableNosqlRepositories;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.Region;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;

@Configuration
@EnableNosqlRepositories
public class AppConfig extends AbstractNosqlConfiguration {

    @Value("${nosql.servicetype}")
    private String SERVICE_TYPE;
    @Value("${nosql.endpoint}")
    private String NOSQL_ENDPOINT;
    @Value("${nosql.port}")
    private String NOSQL_PORT;
    @Value("${nosql.namespace}")
    private String NOSQL_NAMESPACE;

    @Value("${nosql.ociregion}")
    private String OCI_REGION;
    @Value("${nosql.ocicompid}")
    private String OCI_COMPID;

    @Bean
    public NosqlDbConfig nosqlDbConfig() throws java.io.IOException{
        if (SERVICE_TYPE.equals("useInstancePrincipal")){
            SignatureProvider provider = SignatureProvider.createWithInstancePrincipal();
            NoSQLHandleConfig config = new NoSQLHandleConfig(OCI_REGION, provider);
            config.setDefaultCompartment(OCI_COMPID);
            return new NosqlDbConfig(config);
        } else if (SERVICE_TYPE.equals("useResourcePrincipal")){
            SignatureProvider provider = SignatureProvider.createWithResourcePrincipal();
            NoSQLHandleConfig config = new NoSQLHandleConfig(OCI_REGION, provider);
            config.setDefaultCompartment(OCI_COMPID);
            return new NosqlDbConfig(config);
        } else if (SERVICE_TYPE.equals("useUserPrincipal")){
            SignatureProvider provider = new SignatureProvider();
            NoSQLHandleConfig config = new NoSQLHandleConfig(OCI_REGION, provider);
            config.setDefaultCompartment(OCI_COMPID);
            return new NosqlDbConfig(config);
        } else if (SERVICE_TYPE.equals("onprem")) {
            NoSQLHandleConfig config = new NoSQLHandleConfig("http://" + NOSQL_ENDPOINT + ":" + NOSQL_PORT);
            config.setDefaultNamespace(NOSQL_NAMESPACE);
            config.setAuthorizationProvider(new StoreAccessTokenProvider());
            return new NosqlDbConfig( config );
        } else if (SERVICE_TYPE.equals("useDelegationToken")){
            SignatureProvider provider = SignatureProvider.createWithInstancePrincipal(System.getenv("OCI_obo_token"));
            NoSQLHandleConfig config = new NoSQLHandleConfig(OCI_REGION, provider);
            config.setDefaultCompartment(OCI_COMPID);
            return new NosqlDbConfig(config);
        } else {
            System.err.println("Unknown service: " + SERVICE_TYPE);
            return null;
        }
    }
}
