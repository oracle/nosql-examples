package org.example.app;

import com.oracle.nosql.spring.data.config.AbstractNosqlConfiguration;
import com.oracle.nosql.spring.data.config.NosqlDbConfig;
import com.oracle.nosql.spring.data.repository.config.EnableNosqlRepositories;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;


import oracle.nosql.driver.kv.StoreAccessTokenProvider;

@Configuration
@EnableNosqlRepositories
public class AppConfig extends AbstractNosqlConfiguration {

    @Value("${nosql.endpoint}")
    private String NOSQL_ENDPOINT;
    @Value("${nosql.port}")
    private String NOSQL_PORT;

    @Bean
    public NosqlDbConfig nosqlDbConfig() {
        return new NosqlDbConfig(
            "http://" + NOSQL_ENDPOINT + ":" + NOSQL_PORT,                   // endpoint URL
            new StoreAccessTokenProvider());    // AuthorizationProvider
    }
}
