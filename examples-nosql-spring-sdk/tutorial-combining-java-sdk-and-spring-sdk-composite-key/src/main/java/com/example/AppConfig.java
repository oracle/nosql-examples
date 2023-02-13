/*-
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates.  All rights reserved.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 *  https://oss.oracle.com/licenses/upl/
 */
package com.example;

import java.io.IOException;

import com.oracle.nosql.spring.data.config.NosqlDbConfig;
import com.oracle.nosql.spring.data.core.mapping.NosqlCapacityMode;
import com.oracle.nosql.spring.data.repository.config.EnableNosqlRepositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
/* Looks for configuration properties in file: application.properties */
@PropertySource(value = "classpath:application.properties")
@EnableNosqlRepositories
public class AppConfig
    extends AppConfigBase
    //extends AbstractNosqlConfiguration
{

    /* Get values specified in application.properties file or use
    specified default otherwise. */
    @Value("${test.config.defaultStorageGB:25}")
    private int defaultStorageGB;
    @Value(("${test.config.defaultCapacityMode:PROVISIONED}"))
    private String defaultCapacityMode;
    @Value("${test.config.defaultReadUnits:50}")
    private int defaultReadUnits;
    @Value("${test.config.defaultWriteUnits:50}")
    private int defaultWriteUnits;

    public static NosqlDbConfig nosqlDBConfig;
    static {
        staticSetup();

        if (onprem) {
            if (secure) {
                nosqlDBConfig = NosqlDbConfig.createProxyConfig(endpoint,
                    user, password.toCharArray());
            } else {
                nosqlDBConfig = NosqlDbConfig.createProxyConfig(endpoint);
            }
        } else {
            if (secure) {
                try {
                    /* cloud */
                    nosqlDBConfig = NosqlDbConfig.createCloudConfig(endpoint,
                        cloudConfig);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                /* cloud simulator */
                nosqlDBConfig = NosqlDbConfig.createCloudSimConfig(endpoint);
            }
        }

        nosqlDBConfig.setTableReqTimeout(DEFAULT_REQ_TIMEOUT);
        nosqlDBConfig.setTableReqPollInterval(DEFAULT_REQ_POLL_INTERVAL);

        // This is a simpler alternate configuration for development.
//        nosqlDBConfig= new NosqlDbConfig(
//            "http://localhost:8080",   // endpoint URL
//            new StoreAccessTokenProvider());    // AuthorizationProvider
    }


    @Bean
    public NosqlDbConfig nosqlDbConfig() {

        nosqlDBConfig.setDefaultCapacityMode(
            "ON_DEMAND".equals(this.defaultCapacityMode) ?
                NosqlCapacityMode.ON_DEMAND : NosqlCapacityMode.PROVISIONED);
        nosqlDBConfig.setDefaultStorageGB(this.defaultStorageGB);
        nosqlDBConfig.setDefaultReadUnits(this.defaultReadUnits);
        nosqlDBConfig.setDefaultWriteUnits(this.defaultWriteUnits);
        nosqlDBConfig.getNosqlHandleConfig().setDefaultCompartment(this.compid);

        return nosqlDBConfig;
    }
}
