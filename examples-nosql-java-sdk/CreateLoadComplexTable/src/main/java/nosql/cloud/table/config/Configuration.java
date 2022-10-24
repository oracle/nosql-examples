/*
 *  Copyright (C) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 *  This file was distributed by Oracle as part of a version of Oracle NoSQL
 *  Database made available at:
 *
 *  http://www.oracle.com/technetwork/database/database-technologies/nosqldb/downloads/index.html
 *
 *  Please see the LICENSE file included in the top-level directory of the
 *  appropriate version of Oracle NoSQL Database for a copy of the license and
 *  additional information.
 */

package nosql.cloud.table.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import oracle.nosql.driver.values.MapValue;

/**
 * Interface that specifies how implementing classes that parse a given JSON
 * configuration file should provide programmatic access to the contents of
 * the configuration.
 */
public interface Configuration {

    /* Names of properties in the configuration file. */
    String CRED_TYPE_PROP = "credentialsType";
    String REGION_PROP = "region";
    String COMPARTMENT_PROP = "compartment";
    String TABLE_PROP = "table";
    String DELETE_PROP = "delete";
    String N_ROWS_PROP = "nRows";
    String READ_UNITS_PROP = "readUnits";
    String WRITE_UNITS_PROP = "writeUnits";
    String STORAGE_GB_PROP = "storageGb";
    String TTL_DAYS_PROP = "ttlDays";

    String CREDENTIALS_FILE_PROP = "credentialsFile";
    String CREDENTIALS_PROFILE_PROP = "credentialsProfile";

    /* Names of properties in the credentials file. */
    String TENANCY_PROP = "tenancy";
    String USER_PROP = "user";
    String FINGERPRINT_PROP = "fingerprint";
    String PRIVATE_KEY_PROP = "key_file";
    String PASSPHRASE_PROP = "passphrase";

    String ENV_PROP_NAME_OCI_OBO_TOKEN_PATH = "OCI_obo_token_path";

    MapValue getConfigMap();

    String getCredentialsType();
    boolean useCredentialsFile();
    boolean useInstancePrincipal();
    boolean useDelegationToken();

    String getRegion();
    String getCompartment();
    String getTableName();
    boolean deleteRows();
    Integer getNRows();
    Integer getReadUnits();
    Integer getWriteUnits();
    Integer getStorageGb();
    Integer getTtlDays();

    String getCredentialsFile();
    String getCredentialsProfile();
    String getTenancy();
    String getUserOcid();
    String getFingerprint();
    File getPrivateKeyFd();
    char[] getPassphrase();

    File getDelegationTokenFd();

    public enum CredentialsTypeEnum {
        CREDENTIALS_TYPE_FILE {
            @Override
            public String toString() {
                return "file";
            }
        },
        CREDENTIALS_TYPE_PRINCIPAL {
            @Override
            public String toString() {
                return "principal";
            }
        },
        CREDENTIALS_TYPE_TOKEN {
            @Override
            public String toString() {
                return "token";
            }
        };
    }
}
