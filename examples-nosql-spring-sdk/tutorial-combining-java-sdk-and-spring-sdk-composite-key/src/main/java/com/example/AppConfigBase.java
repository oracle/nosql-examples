/*-
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates.  All rights reserved.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 *  https://oss.oracle.com/licenses/upl/
 */
package com.example;

import com.oracle.nosql.spring.data.config.AbstractNosqlConfiguration;

public class AppConfigBase extends AbstractNosqlConfiguration {
    protected static final String ENDPOINT = "test.endpoint";
    protected static final String SERVER_TYPE = "test.serverType";
    protected static final String ST_ONPREM = "onprem";
    protected static final String ST_ONPREM_SECURE = "onprem-secure";
    protected static final String ST_CLOUDSIM = "cloudsim";
    protected static final String ST_CLOUD = "cloud";
    protected static final String USER = "test.user";
    protected static final String TRUST_STORE = "test.trust";
    protected static final String TRUST_STORE_PASSWORD = "test.trust.password";
    protected static final String PASSWORD = "test.password";
    protected static final String CLOUD_CONFIG = "test.config";
    protected static final String CLOUD_COMPID = "test.compid";
    protected static final int DEFAULT_REQ_TIMEOUT = 15000;
    protected static final int DEFAULT_REQ_POLL_INTERVAL = 500;

    protected static String serverType;
    protected static String endpoint;
    protected static boolean onprem;
    protected static boolean secure;
    protected static String user;       // only if onprem && secure
    protected static String password;   // only if onprem && secure
    protected static String trustStore; // only if onprem && secure
    protected static String trustStorePassword; // only if onprem && secure
    protected static String cloudConfig; // only for cloud
    protected static String compid; // only for cloud


    public static void staticSetup() {
        serverType = System.getProperty(SERVER_TYPE);
        endpoint = System.getProperty(ENDPOINT);

        if ( (serverType == null || serverType.isEmpty()) &&
            (endpoint == null || endpoint.isEmpty()) ) {
            serverType = "onprem";
            endpoint = "http://localhost:8080";
        }

        if ( serverType == null || endpoint == null) {
            throw new IllegalArgumentException(
            "Test requires " + ENDPOINT + " and " + SERVER_TYPE +
                " system properties. Ex: -D" + ENDPOINT +
                "=http://localhost:8080 -D" + SERVER_TYPE + "=[" + ST_ONPREM +
                "|" + ST_ONPREM_SECURE + "|" + ST_CLOUDSIM + "]");
        }

        switch (serverType) {
            case ST_ONPREM:
                onprem = true;
                break;

            case ST_ONPREM_SECURE:
                onprem = true;
                secure = true;
                user = System.getProperty(USER);
                password = System.getProperty(PASSWORD);
                trustStore = System.getProperty(TRUST_STORE);
                trustStorePassword = System.getProperty(TRUST_STORE_PASSWORD);
                if (user == null || password == null || trustStore == null) {
                    throw new IllegalArgumentException(
                        "Secure configuration requires user, password, and " +
                            " trustStore system properties.");
                }
                /* the trust store containing SSL cert for the proxy */
                System.setProperty("javax.net.ssl.trustStore", trustStore);
                if (trustStorePassword != null) {
                    System.setProperty("javax.net.ssl.trustStorePassword",
                        trustStorePassword);
                }
                break;

            case ST_CLOUDSIM:
                break;

            case ST_CLOUD:
                secure = true;
                cloudConfig = System.getProperty(CLOUD_CONFIG);
                compid = System.getProperty(CLOUD_COMPID);
                break;

            default:
                throw new IllegalArgumentException("Unknown serverType " +
                    "option: " + serverType);
        }
    }
}
