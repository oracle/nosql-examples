/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package nosql.cloud.table.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static nosql.cloud.table.config.Configuration.CRED_TYPE_PROP;
import static nosql.cloud.table.config.Configuration.REGION_PROP;
import static nosql.cloud.table.config.Configuration.COMPARTMENT_PROP;
import static nosql.cloud.table.config.Configuration.TABLE_PROP;
import static nosql.cloud.table.config.Configuration.DELETE_PROP;
import static nosql.cloud.table.config.Configuration.N_ROWS_PROP;
import static nosql.cloud.table.config.Configuration.READ_UNITS_PROP;
import static nosql.cloud.table.config.Configuration.WRITE_UNITS_PROP;
import static nosql.cloud.table.config.Configuration.STORAGE_GB_PROP;
import static nosql.cloud.table.config.Configuration.TTL_DAYS_PROP;
import static nosql.cloud.table.config.Configuration.CREDENTIALS_FILE_PROP;
import static nosql.cloud.table.config.Configuration.CREDENTIALS_PROFILE_PROP;
import static nosql.cloud.table.config.Configuration.TENANCY_PROP;
import static nosql.cloud.table.config.Configuration.USER_PROP;
import static nosql.cloud.table.config.Configuration.FINGERPRINT_PROP;
import static nosql.cloud.table.config.Configuration.PRIVATE_KEY_PROP;
import static nosql.cloud.table.config.Configuration.PASSPHRASE_PROP;

import static nosql.cloud.table.config.Config.CRED_TYPE_LIST;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nosql.cloud.table.ComplexTableTestBase;
import nosql.cloud.table.config.Configuration;
import nosql.cloud.table.config.Config;
import nosql.cloud.table.config.Configuration.CredentialsTypeEnum;
import nosql.cloud.table.config.ConfigUtils.InvalidConfigException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *  Tests for the configuration mechanism of the CreateLoadComplexTable
 *  example class.
 */
public class ComplexTableConfigurationTest extends ComplexTableTestBase {

    private static final Class THIS_CLASS =
            ComplexTableConfigurationTest.class;
    private static final String THIS_CLASS_FQN = THIS_CLASS.getName();
    private static final Logger logger = Logger.getLogger(THIS_CLASS_FQN);

    protected static final String EXPECTED_PRINCIPAL_CREDS_PROFILE = null;
    protected static final String EXPECTED_PRINCIPAL_CREDS_FILE = null;

    protected static final String EXPECTED_TOKEN_CREDS_PROFILE = null;
    protected static final String EXPECTED_TOKEN_CREDS_FILE = null;

    @Before
    public void setUp() {
        logger.finest("--- " + testName.getMethodName() + " ---");
    }

    @After
    public void teardown() {
        logger.finest("--- tearDown ---");
    }

    @Test
    public void testValidConfig() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        String expectedDelegationTokenPath = null;

        for (int i = 0; i < VALID_CONFIG_JSON_FLNMS.length; i++) {

            final String credType = CRED_TYPE_LIST.get(i);
            logger.fine(methodName + ": credentialsType = " + credType);
            final Map<String, String> expectedConfigMap = CONFIG_MAP.get(credType);

            final File configFd = new File(getTestDir(), VALID_CONFIG_JSON_FLNMS[i]);
            logger.fine(methodName + ": configFd = " + configFd);
            Configuration config = null;
            try {
                config = new Config(configFd);
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            }

            final String expectedCredentialsType = expectedConfigMap.get(CRED_TYPE_PROP);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedCredentialsType = " + expectedCredentialsType);
                logger.finest(methodName + ": credentialsType         = " + config.getCredentialsType());
            }
            assertEquals(expectedCredentialsType, config.getCredentialsType());

            if (CredentialsTypeEnum.CREDENTIALS_TYPE_FILE.toString().equals(credType)) {
                assertTrue(config.useCredentialsFile());
                assertFalse(config.useInstancePrincipal());
                assertFalse(config.useDelegationToken());

                expectedConfigMap.put(TENANCY_PROP, TENANCY_OCID);
                expectedConfigMap.put(USER_PROP, USER_OCID);
                expectedConfigMap.put(FINGERPRINT_PROP, FINGERPRINT);
                expectedConfigMap.put(PRIVATE_KEY_PROP, TEST_DIR + F_SEP + KEY_FILE);
                expectedConfigMap.put(PASSPHRASE_PROP , PASSPHRASE_STR);

            } else if (CredentialsTypeEnum.CREDENTIALS_TYPE_PRINCIPAL.toString().equals(credType)) {
                assertFalse(config.useCredentialsFile());
                assertTrue(config.useInstancePrincipal());
                assertFalse(config.useDelegationToken());
            } else if (CredentialsTypeEnum.CREDENTIALS_TYPE_TOKEN.toString().equals(credType)){
                assertFalse(config.useCredentialsFile());
                assertFalse(config.useInstancePrincipal());
                assertTrue(config.useDelegationToken());

                expectedDelegationTokenPath = System.getenv("OCI_obo_token_path");
            }

            final String expectedRegion = expectedConfigMap.get(REGION_PROP);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedRegion = " + expectedRegion);
                logger.finest(methodName + ": region         = " + config.getRegion());
            }
            assertEquals(expectedRegion, config.getRegion());

            final String expectedCompartmentOcid = expectedConfigMap.get(COMPARTMENT_PROP);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedCompartmentOcid = " + expectedCompartmentOcid);
                logger.finest(methodName + ": compartmentOcid         = " + config.getCompartment());
            }
            assertEquals(expectedCompartmentOcid, config.getCompartment());

            final String expectedTableName = expectedConfigMap.get(TABLE_PROP);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedTableName = " + expectedTableName);
                logger.finest(methodName + ": tableName         = " + config.getTableName());
            }
            assertEquals(expectedTableName, config.getTableName());

            final boolean expectedDeleteRows = Boolean.valueOf(expectedConfigMap.get(DELETE_PROP));
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedDeleteRows = " + expectedDeleteRows);
                logger.finest(methodName + ": deleteRows         = " + config.deleteRows());
            }
            assertEquals(expectedDeleteRows, config.deleteRows());

            int expectedNRows = -1;
            try {
                expectedNRows = Integer.parseInt(expectedConfigMap.get(N_ROWS_PROP));
            } catch (NumberFormatException e) {
                fail(e.getMessage());
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedNRows = " + expectedNRows);
                logger.finest(methodName + ": nRows         = " + config.getNRows());
            }
            assertTrue(expectedNRows == config.getNRows());

            int expectedReadUnits = -1;
            try {
                expectedReadUnits = Integer.parseInt(expectedConfigMap.get(READ_UNITS_PROP));
            } catch (NumberFormatException e) {
                fail(e.getMessage());
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedReadUnits = " + expectedReadUnits);
                logger.finest(methodName + ": readUnits         = " + config.getReadUnits());
            }
            assertTrue(expectedReadUnits == config.getReadUnits());

            int expectedWriteUnits = -1;
            try {
                expectedWriteUnits = Integer.parseInt(expectedConfigMap.get(WRITE_UNITS_PROP));
            } catch (NumberFormatException e) {
                fail(e.getMessage());
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedWriteUnits = " + expectedWriteUnits);
                logger.finest(methodName + ": writeUnits         = " + config.getWriteUnits());
            }
            assertTrue(expectedWriteUnits == config.getWriteUnits());

            int expectedStorageGb = -1;
            try {
                expectedStorageGb = Integer.parseInt(expectedConfigMap.get(STORAGE_GB_PROP));
            } catch (NumberFormatException e) {
                fail(e.getMessage());
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedStorageGb = " + expectedStorageGb);
                logger.finest(methodName + ": storageGb         = " + config.getStorageGb());
            }
            assertTrue(expectedStorageGb == config.getStorageGb());

            int expectedTtlDays = -1;
            try {
                expectedTtlDays = Integer.parseInt(expectedConfigMap.get(TTL_DAYS_PROP));
            } catch (NumberFormatException e) {
                fail(e.getMessage());
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedTtlDays = " + expectedTtlDays);
                logger.finest(methodName + ": ttlDays         = " + config.getTtlDays());
            }
            assertTrue(expectedTtlDays == config.getTtlDays());

            final String expectedCredentialsProfile = expectedConfigMap.get(CREDENTIALS_PROFILE_PROP);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedCredentialsProfile = " + expectedCredentialsProfile);
                logger.finest(methodName + ": credentialsProfile         = " + config.getCredentialsProfile());
            }
            assertEquals(expectedCredentialsProfile, config.getCredentialsProfile());

            final String expectedCredentialsFile = expectedConfigMap.get(CREDENTIALS_FILE_PROP);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedCredentialsFile = " + expectedCredentialsFile);
                logger.finest(methodName + ": credentialsFile         = " + config.getCredentialsFile());
            }
            assertEquals(expectedCredentialsFile, config.getCredentialsFile());

            final String expectedTenancyOcid = expectedConfigMap.get(TENANCY_PROP);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedTenancyOcid = " + expectedTenancyOcid);
                logger.finest(methodName + ": tenancyOcid         = " + config.getTenancy());
            }
            assertEquals(expectedTenancyOcid, config.getTenancy());

            final String expectedUserOcid = expectedConfigMap.get(USER_PROP);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedUserOcid = " + expectedUserOcid);
                logger.finest(methodName + ": userOcid         = " + config.getUserOcid());
            }
            assertEquals(expectedUserOcid, config.getUserOcid());

            final String expectedFingerprint = expectedConfigMap.get(FINGERPRINT_PROP);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedFingerprint = " + expectedFingerprint);
                logger.finest(methodName + ": fingerprint         = " + config.getFingerprint());
            }
            assertEquals(expectedFingerprint, config.getFingerprint());

            final String expectedPrivateKeyFlnm = expectedConfigMap.get(PRIVATE_KEY_PROP);
            File privateKeyFd = config.getPrivateKeyFd();
            String privateKeyFlnm = (privateKeyFd == null ? null : privateKeyFd.toString());
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedPrivateKeyFlnm = " + expectedPrivateKeyFlnm);
                logger.finest(methodName + ": privateKeyFlnm         = " + privateKeyFlnm);
            }
            assertEquals(expectedPrivateKeyFlnm, privateKeyFlnm);

            final String expectedPassphraseStr = expectedConfigMap.get(PASSPHRASE_PROP);
            char[] passphrase = config.getPassphrase();
            String passphraseStr = (passphrase == null ? null : String.valueOf(passphrase));
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedPassphraseStr = " + expectedPassphraseStr);
                logger.finest(methodName + ": passphraseStr         = " + passphraseStr);
            }
            assertEquals(expectedPassphraseStr, passphraseStr);

            File delegationTokenFd = config.getDelegationTokenFd();
            String delegationTokenPath = (delegationTokenFd == null ? null : delegationTokenFd.toString());
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(methodName + ": expectedDelegationTokenPath = " + expectedDelegationTokenPath);
                logger.finest(methodName + ": delegationTokenPath         = " + delegationTokenPath);
            }
            assertEquals(expectedDelegationTokenPath, delegationTokenPath);
       }
    }

    /*
     * Tests that verify entries required by all credential types
     *  (file, principal, token).
     */
    @Test
    public void testMissingCredentialsType() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the value specified for the config
         * entry named "credentialsType" is missing from the
         * configuration, then an InvalidConfigException is
         * thrown by the configuration mechanism.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = CRED_TYPE_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;
        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingCredentialsType-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            try {
                final Configuration config = new Config(configFd);
                fail("expected InvalidConfigException due to missing '" +
                     fieldName + "' entry in the Configuration, but " +
                     "exception did not occur. A value was actually " +
                     "specified for that entry " +
                     "[" + config.getCredentialsType() + "]");
            } catch (InvalidConfigException e) {
                /* Swallow expected exception */
            }
        }
    }

    @Test
    public void testInvalidCredentialsType() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that when an invalid value is specified for the
         * entry named "credentialsType", then an InvalidConfigException
         * is thrown by the configuration mechanism.
         */
        final boolean injectError = true;
        final boolean excludeField = false;
        final String fieldName = CRED_TYPE_PROP; /* Change field with this name */
        final String strFieldVal = "BAD_TYPE"; /* Overwrite with this value */
        final int intFieldVal = 0;
        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configInvalidCredentialsType-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            try {
                final Configuration config = new Config(configFd);
                fail("expected InvalidConfigException due to invalid '" +
                     fieldName + "' entry in the Configuration, but " +
                     "exception did not occur. The value specified for " +
                     "that entry was actually valid " +
                     "[" + config.getCredentialsType() + "]");
            } catch (InvalidConfigException e) {
                /* Swallow expected exception */
            }
        }
    }

    @Test
    public void testMissingRegion() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the value specified for the config
         * entry named "region" is missing from the configuration,
         * then an InvalidConfigException is thrown by the
         * configuration mechanism.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = REGION_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;
        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingRegion-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            try {
                final Configuration config = new Config(configFd);
                fail("expected InvalidConfigException due to missing '" +
                      fieldName + "' entry in the Configuration, but " +
                      "exception did not occur. A value was actually " +
                      "specified for that entry " +
                      "[" + config.getRegion() + "]");
            } catch (InvalidConfigException e) {
                /* Swallow expected exception */
            }
        }
    }

    @Test
    public void testMissingCompartment() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the value specified for the config
         * entry named "compartment" is missing from the configuration,
         * then an InvalidConfigException is thrown by the
         * configuration mechanism.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = COMPARTMENT_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;
        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingCompartment-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            try {
                final Configuration config = new Config(configFd);
                fail("expected InvalidConfigException due to missing '" +
                        fieldName + "' entry in the Configuration, but " +
                        "exception did not occur. A value was actually " +
                        "specified for that entry " +
                        "[" + config.getCompartment() + "]");
            } catch (InvalidConfigException e) {
                /* Swallow expected exception */
            }
        }
    }


    @Test
    public void testMissingTable() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the value specified for the config
         * entry named "table" is missing from the configuration,
         * then an InvalidConfigException is thrown by the
         * configuration mechanism.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = TABLE_PROP;; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;
        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingTable-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            try {
                final Configuration config = new Config(configFd);
                fail("expected InvalidConfigException due to missing '" +
                        fieldName + "' entry in the Configuration, but " +
                        "exception did not occur. A value was actually " +
                        "specified for that entry " +
                        "[" + config.getCredentialsType() + "]");
            } catch (InvalidConfigException e) {
                /* Swallow expected exception */
            }
        }
    }

    @Test
    public void testInvalidTable() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the value specified for the config entry
         * named "table" is invalid, then an InvalidConfigException
         * is thrown by the configuration mechanism.
         */
        final boolean injectError = true;
        final boolean excludeField = false;
        final String fieldName = TABLE_PROP; /* Change field with this name */
        final String strFieldVal = "invalid-table-name"; /* Hyphens not allowed */
        final int intFieldVal = 0;
        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configInvalidTable-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            try {
                final Configuration config = new Config(configFd);
                fail("expected InvalidConfigException due to invalid '" +
                     fieldName + "' entry in the Configuration, but " +
                     "exception did not occur. The value specified for " +
                     "that entry was actually valid " +
                     "[" + config.getTableName() + "]");
            } catch (InvalidConfigException e) {
                /* Swallow expected exception */
            }
        }
    }

    @Test
    public void testMissingDeleteFlag() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that when the "delete" entry is not
         * specified in the configuration, then the configuration
         * returns the default value 'true' as the value of the
         * delete flag.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = DELETE_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;

        final boolean expectedVal = true;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingDeleteFlag-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final boolean actualVal = config.deleteRows();
            if (expectedVal != actualVal) {
                fail("when no '" + fieldName + "' entry is specified " +
                     " in the Configuration, the value expected [" +
                     expectedVal + "] was not the value returned " +
                     "from the configuration [" + actualVal + "]");
            }
        }
    }

    @Test
    public void testMissingNRows() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that when the "nRows" entry is not
         * specified in the configuration, then the configuration
         * returns the default value.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = N_ROWS_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;

        final int expectedVal = Config.DEFAULT_N_ROWS;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingNRows-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final int actualVal = config.getNRows();
            if (expectedVal != actualVal) {
                fail("when no '" + fieldName + "' entry is specified " +
                     " in the Configuration, the value expected [" +
                     expectedVal + "] was not the value returned " +
                     "from the configuration [" + actualVal + "]");
            }
        }
    }

    @Test
    public void testInvalidNRows() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the value specified for the config entry
         * named "nRows" is invalid, then the configuration
         * returns the default value.
         */
        final boolean injectError = true;
        final boolean excludeField = false;
        final String fieldName = N_ROWS_PROP; /* Change field with this name */
        final String strFieldVal = "invalid-n-rows";
        final int intFieldVal = 0;

        final int expectedVal = Config.DEFAULT_N_ROWS;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configInvalidNRows-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final int actualVal = config.getNRows();
            if (expectedVal != actualVal) {
                fail("when an invalid value is specified for '" + fieldName +
                     " in the Configuration, the default value expected [" +
                     expectedVal + "] was not the value returned " +
                     "from the configuration [" + actualVal + "]");
            }
        }
    }

    @Test
    public void testMissingReadUnits() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that when the "readUnits" entry is not
         * specified in the configuration, then the configuration
         * returns the default value.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = READ_UNITS_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;

        final int expectedVal = Config.DEFAULT_READ_UNITS;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingReadUnits-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final int actualVal = config.getReadUnits();
            if (expectedVal != actualVal) {
                fail("when no '" + fieldName + "' entry is specified " +
                     " in the Configuration, the value expected [" +
                     expectedVal + "] was not the value returned " +
                     "from the configuration [" + actualVal + "]");
            }
        }
    }

    @Test
    public void testInvalidReadUnits() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the value specified for the config entry
         * named "readUnits" is invalid, then the configuration
         * returns the default value.
         */
        final boolean injectError = true;
        final boolean excludeField = false;
        final String fieldName = READ_UNITS_PROP; /* Change field with this name */
        final String strFieldVal = "invalid-read-units";
        final int intFieldVal = 0;

        final int expectedVal = Config.DEFAULT_READ_UNITS;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configInvalidReadUnits-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final int actualVal = config.getReadUnits();
            if (expectedVal != actualVal) {
                fail("when an invalid value is specified for '" + fieldName +
                     " in the Configuration, the default value expected [" +
                     expectedVal + "] was not the value returned " +
                     "from the configuration [" + actualVal + "]");
            }
        }
    }

    @Test
    public void testMissingWriteUnits() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that when the "writeUnits" entry is not
         * specified in the configuration, then the configuration
         * returns the default value.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = WRITE_UNITS_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;

        final int expectedVal = Config.DEFAULT_WRITE_UNITS;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingWriteUnits-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final int actualVal = config.getWriteUnits();
            if (expectedVal != actualVal) {
                fail("when no '" + fieldName + "' entry is specified " +
                     " in the Configuration, the value expected [" +
                     expectedVal + "] was not the value returned " +
                     "from the configuration [" + actualVal + "]");
            }
        }
    }

    @Test
    public void testInvalidWriteUnits() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the value specified for the config entry
         * named "writeUnits" is invalid, then the configuration
         * returns the default value.
         */
        final boolean injectError = true;
        final boolean excludeField = false;
        final String fieldName = WRITE_UNITS_PROP; /* Change field with this name */
        final String strFieldVal = "invalid-write-units";
        final int intFieldVal = 0;

        final int expectedVal = Config.DEFAULT_WRITE_UNITS;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configInvalidWritUnits-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final int actualVal = config.getWriteUnits();
            if (expectedVal != actualVal) {
                fail("when an invalid value is specified for '" + fieldName +
                        " in the Configuration, the default value expected [" +
                        expectedVal + "] was not the value returned " +
                        "from the configuration [" + actualVal + "]");
            }
        }
    }

    @Test
    public void testMissingStorageGb() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that when the "storageGb" entry is not
         * specified in the configuration, then the configuration
         * returns the default value.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = STORAGE_GB_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;

        final int expectedVal = Config.DEFAULT_STORAGE_GB;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingStorageGb-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final int actualVal = config.getStorageGb();
            if (expectedVal != actualVal) {
                fail("when no '" + fieldName + "' entry is specified " +
                     " in the Configuration, the value expected [" +
                     expectedVal + "] was not the value returned " +
                     "from the configuration [" + actualVal + "]");
            }
        }
    }

    @Test
    public void testInvalidStorageGb() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the value specified for the config entry
         * named "storageGb" is invalid, then the configuration
         * returns the default value.
         */
        final boolean injectError = true;
        final boolean excludeField = false;
        final String fieldName = STORAGE_GB_PROP; /* Change field with this name */
        final String strFieldVal = "invalid-storage-gb-units";
        final int intFieldVal = 0;

        final int expectedVal = Config.DEFAULT_STORAGE_GB;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configInvalidStorageGb-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final int actualVal = config.getStorageGb();
            if (expectedVal != actualVal) {
                fail("when an invalid value is specified for '" + fieldName +
                        " in the Configuration, the default value expected [" +
                        expectedVal + "] was not the value returned " +
                        "from the configuration [" + actualVal + "]");
            }
        }
    }


    @Test
    public void testMissingTtlDays() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that when the "ttlDays" entry is not
         * specified in the configuration, then the configuration
         * returns the default value.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = TTL_DAYS_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;

        /* If ttlDays entry is missing, ConfigUtils.readInt returns 0 */
        final int expectedVal = 0;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingTtlDays-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final int actualVal = config.getTtlDays();
            if (expectedVal != actualVal) {
                fail("when no '" + fieldName + "' entry is specified " +
                        " in the Configuration, the value expected [" +
                        expectedVal + "] was not the value returned " +
                        "from the configuration [" + actualVal + "]");
            }
        }
    }

    @Test
    public void testInvalidTtlDays() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the value specified for the config entry
         * named "ttlDays" is invalid, then the configuration
         * returns the default value.
         */
        final boolean injectError = true;
        final boolean excludeField = false;
        final String fieldName = TTL_DAYS_PROP; /* Change field with this name */
        final String strFieldVal = "invalid-ttl-days-units";
        final int intFieldVal = 0;

        /*
         * If ttlDays entry is invalid (not an integer),
         * ConfigUtils.readInt returns 0.
         */
        final int expectedVal = 0;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configInvalidTtlDays-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            final Configuration config = new Config(configFd);
            final int actualVal = config.getTtlDays();
            if (expectedVal != actualVal) {
                fail("when an invalid value is specified for '" + fieldName +
                        " in the Configuration, the default value expected [" +
                        expectedVal + "] was not the value returned " +
                        "from the configuration [" + actualVal + "]");
            }
        }
    }

    @Test
    public void testMissingCredentialsFile() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that if the "credentialsType" is "file" and
         * the value specified for the config entry named "credentialsFile"
         * is missing from the configuration, then an InvalidConfigException
         * is thrown by the configuration mechanism. But if the
         * "credentialsType" is "principal" or "token", then no
         * exception is thrown.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = CREDENTIALS_FILE_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;
        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingCredentialsFile-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            Configuration config = null;
            if (credType.equals("file")) {
                try {
                    config = new Config(configFd);
                    fail("because credentialsType = " +
                         config.getCredentialsType() + ", expected " +
                         "InvalidConfigException due to missing '" +
                         fieldName + "' entry in the Configuration, but " +
                         "exception did not occur. A value was actually " +
                         "specified for that entry " +
                         "[" + config.getCredentialsFile() + "]");
                } catch (InvalidConfigException e) {
                    /* Swallow expected exception */
                }
            } else {
                config = new Config(configFd);
            }
        }
    }

    @Test
    public void testInvalidCredentialsFile() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that when an invalid value is specified for the
         * entry named "credentialsFile", then an IOException
         * is thrown by the configuration mechanism.
         */
        final boolean excludeField = false;
        final String fieldName = CREDENTIALS_FILE_PROP; /* Change field with this name */
        final int intFieldVal = 0;

        Configuration config = null;
        File configFd = null;
        boolean injectError = false;
        String strFieldVal = null;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configInvalidCredentialsFile-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            if (credType.equals("file")) {
                injectError = true;
                strFieldVal = "BAD_CREDENTIALS_FILE"; /* Overwrite with this value */
                configFd =
                        createJsonConfigFile(configFlnm,
                                injectError, excludeField,
                                credType, fieldName,
                                strFieldVal, intFieldVal);
                try {
                    config = new Config(configFd);
                    fail("because credentialsType = " +
                            config.getCredentialsType() + ", expected " +
                            "IOException due to invalid '" +
                            fieldName + "' entry in the Configuration, but " +
                            "exception did not occur. The value specified for " +
                            "that entry was actually valid " +
                            "[" + config.getCredentialsFile() + "]");
                } catch (IOException e) {
                    /* Swallow expected exception */
                }
            } else {
                injectError = false;
                strFieldVal = null;
                configFd =
                        createJsonConfigFile(configFlnm,
                                injectError, excludeField,
                                credType, fieldName,
                                strFieldVal, intFieldVal);
                config = new Config(configFd);
            }
        }
    }

    @Test
    public void testMissingCredentialsProfile() throws IOException {

        final String methodName = testName.getMethodName();
        logger.finest("--- " + methodName + " ---");
        /*
         * Verify that when the "credentialProfile" entry is not
         * specified in the configuration, then the configuration
         * return "DEFAULT" as the value of the profile.
         */
        final boolean injectError = true;
        final boolean excludeField = true;
        final String fieldName = CREDENTIALS_PROFILE_PROP; /* Change field with this name */
        final String strFieldVal = null;
        final int intFieldVal = 0;

        Configuration config = null;
        String expectedVal = null;
        String actualVal = null;

        for (String credType : CRED_TYPE_LIST) {
            final String configFlnm = "configMissingCredentialsProfile-" + credType + ".json";
            logger.fine(methodName + ": configFlnm = " + configFlnm);
            final File configFd =
                    createJsonConfigFile(configFlnm,
                            injectError, excludeField,
                            credType, fieldName,
                            strFieldVal, intFieldVal);
            if (credType.equals("file")) {
                expectedVal = Config.DEFAULT_CREDENTIALS_PROFILE;
                config = new Config(configFd);
                actualVal = config.getCredentialsProfile();
                if (!expectedVal.equals(actualVal)) {
                    fail("credentialsType = " + config.getCredentialsType() +
                            ", and no '" + fieldName + "' entry is specified " +
                            " in the Configuration, the value expected [" +
                            expectedVal + "] was not the value returned " +
                            "from the configuration [" + actualVal + "]");
                }
            } else {
                config = new Config(configFd);
            }
        }
    }


}
