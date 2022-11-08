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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.oracle.bmc.ConfigFileReader;

import nosql.cloud.table.config.Configuration.CredentialsTypeEnum;
import nosql.cloud.table.config.ConfigUtils.InvalidConfigException;

import oracle.nosql.driver.JsonParseException;
import oracle.nosql.driver.values.JsonReader;
import oracle.nosql.driver.values.MapValue;

/**
 * Class that encapsulates the configurations needed for interacting with the
 * Oracle Database Cloud Service for purpose of creating and populating a
 * table with complex data.
 */
public class Config implements Configuration {

    private static final Class THIS_CLASS = Config.class;
    private static final String THIS_CLASS_FQN = THIS_CLASS.getName();
    private static final Logger logger = Logger.getLogger(THIS_CLASS_FQN);

    /* To support child tables, must now allow dot ('.') in tablenames. */
    protected final static String VALID_IDENTIFIER_CHAR_REGEX =
                                      "^[A-Za-z][A-Za-z0-9_.]*$";
    protected final static int MAX_TABLE_NAME_LENGTH = 256;

    protected MapValue configMap;

    protected boolean authByCredentialsFile = false;
    protected boolean authByInstancePrincipal = false;
    protected boolean authByDelegationToken = false;

    protected File privateKeyFd = null; /* From user credentials file. */
    protected char[] passphrase = null; /* From user credentials file. */
    protected File delegationTokenFileFd = null; /* From OCI_obo_token_path */

    public static final boolean DEFAULT_DELETE_EXISTING = true;
    public static final int DEFAULT_N_ROWS = 10;
    public static final int DEFAULT_READ_UNITS = 50;
    public static final int DEFAULT_WRITE_UNITS = 50;
    public static final int DEFAULT_STORAGE_GB = 25;
    public static final int DEFAULT_TTL_DAYS = 1;
    public static final String DEFAULT_CREDENTIALS_PROFILE = "DEFAULT";

    public final static List<String> CRED_TYPE_LIST = new ArrayList<>();
    static {
        CRED_TYPE_LIST.add(CredentialsTypeEnum.CREDENTIALS_TYPE_FILE.toString()); /* user credentials file */
        CRED_TYPE_LIST.add(CredentialsTypeEnum.CREDENTIALS_TYPE_PRINCIPAL.toString()); /* InstancePrincipal */
        CRED_TYPE_LIST.add(CredentialsTypeEnum.CREDENTIALS_TYPE_TOKEN.toString()); /* Delegation Token */
    }

    public Config(final File configFd) throws IOException {

        if (configFd == null) {
            throw new IllegalArgumentException("null file descriptor entered");
        }

        if (!configFd.exists()) {
            throw new IOException("file does not exist [" + configFd + "]");
        }

        final JsonReader jsonReader = new JsonReader(configFd, null);
        try {
            this.configMap = jsonReader.iterator().next();
        } catch (JsonParseException e) {
            throw new IllegalArgumentException(
                    "error parsing JSON config file [" +
                    configFd.getAbsolutePath() + "]: " + e.getMessage());
        }
        initValidateConfigMap();
        logger.fine("configuration = " + configMap);
    }

    /**
     * Initializes the configMap with each validated config item.
     */
    protected void initValidateConfigMap() throws IOException {

        /* credentialsType - required */
        String sysPropName = Configuration.CRED_TYPE_PROP;
        final String credType = ConfigUtils.readString(configMap, sysPropName);
        String sysPropVal = System.getProperty(sysPropName);
        if (sysPropVal != null) {
            configMap.put(sysPropName, sysPropVal);
        } else if (credType != null) {
            configMap.put(sysPropName, credType);
        } else {
            ConfigUtils.requireValue(sysPropName);
        }
        initValidateCredentialInfo();

        /* region - required */
        sysPropName = Configuration.REGION_PROP;
        final String region = ConfigUtils.readString(configMap, sysPropName);
        sysPropVal = System.getProperty(sysPropName);
        if (sysPropVal != null) {
            configMap.put(sysPropName, sysPropVal);
        } else if (region != null) {
            configMap.put(sysPropName, region);
        } else {
            ConfigUtils.requireValue(sysPropName);
        }

        /* compartment - required */
        sysPropName = Configuration.COMPARTMENT_PROP;
        final String compartment =
                ConfigUtils.readString(configMap, sysPropName);
        sysPropVal = System.getProperty(sysPropName);
        if (sysPropVal != null) {
            configMap.put(sysPropName, sysPropVal);
        } else if (compartment != null) {
            configMap.put(sysPropName, compartment);
        } else {
            ConfigUtils.requireValue(sysPropName);
        }

        /* table - required */
        sysPropName = Configuration.TABLE_PROP;
        final String tableName =
                ConfigUtils.readString(configMap, sysPropName);
        sysPropVal = System.getProperty(sysPropName);
        if (sysPropVal != null) {
            configMap.put(sysPropName, sysPropVal);
        } else if (tableName != null) {
            configMap.put(sysPropName, tableName);
        } else {
            ConfigUtils.requireValue(sysPropName);
        }
        validateIdentifier(ConfigUtils.readString(configMap, sysPropName),
                           MAX_TABLE_NAME_LENGTH, sysPropName);

        /* Delete existing rows - optional */
        sysPropName = Configuration.DELETE_PROP;
        boolean deleteExisting =
                ConfigUtils.readBoolean(configMap, sysPropName,
                                        DEFAULT_DELETE_EXISTING);
        sysPropVal = System.getProperty(sysPropName);
        if (sysPropVal != null) {
            /* parseBoolean returns true only if sysPropVal is "true". */
            deleteExisting = Boolean.parseBoolean(sysPropVal);
        }
        configMap.put(sysPropName, deleteExisting);

        /* nRows to create and populate - optional */
        sysPropName = Configuration.N_ROWS_PROP;
        int sysPropInt = DEFAULT_N_ROWS;
        int nRows = ConfigUtils.readInt(configMap, sysPropName);
        sysPropVal = System.getProperty(sysPropName);
        if (sysPropVal != null) {/* Command line specified override. */
            try{
                sysPropInt = Integer.parseInt(sysPropVal);
                if (sysPropInt < 0) {
                    final String errMsg = "invalid config entry [" +
                            sysPropName + "]: " +
                            "value specified is negative " +
                            "[" + sysPropInt + "]";
                    throw new InvalidConfigException(errMsg);
                }
            } catch (NumberFormatException e) {
                final String errMsg = "invalid config entry [" +
                        sysPropName + "]: " +
                        "value specified is not a number " +
                        "[" + sysPropVal + "]";
                throw new InvalidConfigException(errMsg);
            }
            configMap.put(sysPropName, sysPropInt);
        } else if (nRows < 0) {/* config specified negative */
            final String errMsg = "invalid config entry [" +
                    sysPropName + "]: " +
                    "value specified is negative " +
                    "[" + nRows + "]";
            throw new InvalidConfigException(errMsg);
        } else if (nRows > 0) {/* valid config value */
            configMap.put(sysPropName, nRows);
        } else {/* Nothing specified. Use default. */
            configMap.put(sysPropName, sysPropInt);
        }

        /* readUnits - optional */
        sysPropName = Configuration.READ_UNITS_PROP;
        sysPropInt = DEFAULT_READ_UNITS;
        int readUnits = ConfigUtils.readInt(configMap, sysPropName);
        sysPropVal = System.getProperty(sysPropName);
        if (sysPropVal != null) {/* Command line specified override. */
            try{
                sysPropInt = Integer.parseInt(sysPropVal);
                if (sysPropInt < 0) {
                    final String errMsg = "invalid config entry [" +
                            sysPropName + "]: " +
                            "value specified is negative " +
                            "[" + sysPropInt + "]";
                    throw new InvalidConfigException(errMsg);
                }
            } catch (NumberFormatException e) {
                final String errMsg = "invalid config entry [" +
                        sysPropName + "]: " +
                        "value specified is not a number " +
                        "[" + sysPropInt + "]";
                throw new InvalidConfigException(errMsg);
            }
            configMap.put(sysPropName, sysPropInt);
        } else if (readUnits < 0) {/* config specified negative */
            final String errMsg = "invalid config entry [" +
                    sysPropName + "]: " +
                    "value specified is negative " +
                    "[" + readUnits + "]";
            throw new InvalidConfigException(errMsg);
        } else if (readUnits > 0) {/* valid config value */
            configMap.put(sysPropName, readUnits);
        } else {/* Nothing specified. Use default. */
            configMap.put(sysPropName, sysPropInt);
        }

        /* writeUnits - optional */
        sysPropName = Configuration.WRITE_UNITS_PROP;
        sysPropInt = DEFAULT_WRITE_UNITS;
        int writeUnits = ConfigUtils.readInt(configMap, sysPropName);
        sysPropVal = System.getProperty(sysPropName);
        if (sysPropVal != null) {/* Command line specified override. */
            try{
                sysPropInt = Integer.parseInt(sysPropVal);
                if (sysPropInt < 0) {
                    final String errMsg = "invalid config entry [" +
                            sysPropName + "]: " +
                            "value specified is negative " +
                            "[" + sysPropInt + "]";
                    throw new InvalidConfigException(errMsg);
                }
            } catch (NumberFormatException e) {
                final String errMsg = "invalid config entry [" +
                        sysPropName + "]: " +
                        "value specified is not a number " +
                        "[" + sysPropVal + "]";
                throw new InvalidConfigException(errMsg);
            }
            configMap.put(sysPropName, sysPropInt);
        } else if (writeUnits < 0) {/* config specified negative */
            final String errMsg = "invalid config entry [" +
                    sysPropName + "]: " +
                    "value specified is negative " +
                    "[" + writeUnits + "]";
            throw new InvalidConfigException(errMsg);
        } else if (writeUnits > 0) {/* valid config value */
            configMap.put(sysPropName, writeUnits);
        } else {/* Nothing specified. Use default. */
            configMap.put(sysPropName, sysPropInt);
        }

        /* storageGb - optional */
        sysPropName = Configuration.STORAGE_GB_PROP;
        sysPropInt = DEFAULT_STORAGE_GB;
        int storageGb = ConfigUtils.readInt(configMap, sysPropName);
        sysPropVal = System.getProperty(sysPropName);
        if (sysPropVal != null) {/* Command line specified override. */
            try{
                sysPropInt = Integer.parseInt(sysPropVal);
                if (sysPropInt < 0) {
                    final String errMsg = "invalid config entry [" +
                            sysPropName + "]: " +
                            "value specified is negative " +
                            "[" + sysPropInt + "]";
                    throw new InvalidConfigException(errMsg);
                }
            } catch (NumberFormatException e) {
                final String errMsg = "invalid config entry [" +
                        sysPropName + "]: " +
                        "value specified is not a number " +
                        "[" + sysPropVal + "]";
                throw new InvalidConfigException(errMsg);
            }
            configMap.put(sysPropName, sysPropInt);
        } else if (storageGb < 0) {/* config specified negative */
            final String errMsg = "invalid config entry [" +
                    sysPropName + "]: " +
                    "value specified is negative " +
                    "[" + storageGb + "]";
            throw new InvalidConfigException(errMsg);
        } else if (storageGb > 0) {/* valid config value */
            configMap.put(sysPropName, storageGb);
        } else {/* Nothing specified. Use default. */
            configMap.put(sysPropName, sysPropInt);
        }

        /* ttlDays - optional */
        sysPropName = Configuration.TTL_DAYS_PROP;
        sysPropInt = DEFAULT_TTL_DAYS;
        int ttlDays = ConfigUtils.readInt(configMap, sysPropName);
        sysPropVal = System.getProperty(sysPropName);
        if (sysPropVal != null) {/* Command line specified override. */
            try{
                sysPropInt = Integer.parseInt(sysPropVal);
                if (sysPropInt < 0) {
                    final String errMsg = "invalid config entry [" +
                            sysPropName + "]: " +
                            "value specified is negative " +
                            "[" + sysPropInt + "]";
                    throw new InvalidConfigException(errMsg);
                }
            } catch (NumberFormatException e) {
                final String errMsg = "invalid config entry [" +
                        sysPropName + "]: " +
                        "value specified is not a number " +
                        "[" + sysPropVal + "]";
                throw new InvalidConfigException(errMsg);
            }
            configMap.put(sysPropName, sysPropInt);
        } else if (ttlDays < 0) {/* config specified negative */
            final String errMsg = "invalid config entry [" +
                    sysPropName + "]: " +
                    "value specified is negative " +
                    "[" + ttlDays + "]";
            throw new InvalidConfigException(errMsg);
        } else if (ttlDays >= 0) {/* valid config value */
            configMap.put(sysPropName, ttlDays);
        } else {/* Nothing specified. Use default. */
            configMap.put(sysPropName, sysPropInt);
        }

        if (useCredentialsFile()) {
            /* credentialsProfile - optional */
            sysPropName = Configuration.CREDENTIALS_PROFILE_PROP;
            String sysPropStr = DEFAULT_CREDENTIALS_PROFILE;
            final String credentialsProfile =
                    ConfigUtils.readString(configMap, sysPropName);
            sysPropVal = System.getProperty(sysPropName);
            if (sysPropVal != null) {/* Command line specified override. */
                configMap.put(sysPropName, sysPropVal);
            } else if (credentialsProfile != null) {/* valid config value */
                configMap.put(sysPropName, credentialsProfile);
            } else {/* Nothing specified. Use default. */
                configMap.put(sysPropName, sysPropStr);
            }

            /* credentialsFile - required when authType is cred file */
            sysPropName = Configuration.CREDENTIALS_FILE_PROP;
            final String credentialsFilePath =
                    ConfigUtils.readString(configMap, sysPropName);
            sysPropVal = System.getProperty(sysPropName);
            if (sysPropVal != null) {/* Command line specified override. */
                configMap.put(sysPropName, sysPropVal);
            } else if (credentialsFilePath != null) {/* valid config value */
                configMap.put(sysPropName, credentialsFilePath);
            } else {
                ConfigUtils.requireValue(sysPropName);
            }

            /* Verify the credentials file exists. */
            final File credentialsFileFd = new File(credentialsFilePath);
            if (!credentialsFileFd.exists()) {
                throw new IOException("credentials file does not exist [" +
                        credentialsFileFd + "]");
            }

            /* Retrieve the contents of the credentials file. */
            final ConfigFileReader.ConfigFile credentialsFile =
                    ConfigFileReader.parse(credentialsFileFd.getAbsolutePath(),
                                           credentialsProfile);

            /* tenancy - required when authType is cred file */
            sysPropName = Configuration.TENANCY_PROP;
            final String tenancy = credentialsFile.get(sysPropName);
            sysPropVal = System.getProperty(sysPropName);
            if (sysPropVal != null) {
                configMap.put(sysPropName, sysPropVal);
            } else if (tenancy != null) {
                configMap.put(sysPropName, tenancy);
            } else {
                ConfigUtils.requireValue(sysPropName);
            }

            /* user - required when authType is cred file */
            sysPropName = Configuration.USER_PROP;
            final String userOcid = credentialsFile.get(sysPropName);
            sysPropVal = System.getProperty(sysPropName);
            if (sysPropVal != null) {
                configMap.put(sysPropName, sysPropVal);
            } else if (userOcid != null) {
                configMap.put(sysPropName, userOcid);
            } else {
                ConfigUtils.requireValue(sysPropName);
            }

            /* fingerprint - required when authType is cred file */
            sysPropName = Configuration.FINGERPRINT_PROP;
            final String fingerprint = credentialsFile.get(sysPropName);
            sysPropVal = System.getProperty(sysPropName);
            if (sysPropVal != null) {
                configMap.put(sysPropName, sysPropVal);
            } else if (fingerprint != null) {
                configMap.put(sysPropName, fingerprint);
            } else {
                ConfigUtils.requireValue(sysPropName);
            }

            /* key_file - required when authType is cred file */
            sysPropName = Configuration.PRIVATE_KEY_PROP;
            final String privateKeyFile = credentialsFile.get(sysPropName);
            sysPropVal = System.getProperty(sysPropName);
            if (sysPropVal != null) {
                configMap.put(sysPropName, sysPropVal);
            } else if (privateKeyFile != null) {
                configMap.put(sysPropName, privateKeyFile);
            } else {
                ConfigUtils.requireValue(sysPropName);
            }

            /* Verify the private key file exists. */
            privateKeyFd = new File(privateKeyFile);
            if (!privateKeyFd.exists()) {
                throw new IOException("private key file specified does not " +
                        "exist [" + privateKeyFd + "]");
            }

            /*
             * passphrase. Required only if a passphrase was set on the
             * key in the private key file. Treated here as not required.
             */
            sysPropName = Configuration.PASSPHRASE_PROP;
            String passphraseStr = credentialsFile.get(sysPropName);
            sysPropVal = System.getProperty(sysPropName);
            if (sysPropVal != null) {
                configMap.put(sysPropName, sysPropVal);
            } else if (passphraseStr != null) {
                configMap.put(sysPropName, passphraseStr);
            }

            /* Convert passphrase to char array. */
            passphraseStr = ConfigUtils.readString(configMap, sysPropName);
            if (passphraseStr != null) {
                passphrase = passphraseStr.toCharArray();
            }
        }

        if (useDelegationToken()) {
            sysPropName = ENV_PROP_NAME_OCI_OBO_TOKEN_PATH;
            final String tokenFilePath = System.getenv(sysPropName);

            /* Verify the file with the delegation token exists. */
            if (tokenFilePath == null) {
                throw new IOException("delegation token file not found, " +
                        "because enviromnment variable '" +
                        ENV_PROP_NAME_OCI_OBO_TOKEN_PATH + "' does not " +
                        "exist.");
            }
            delegationTokenFileFd = new File(tokenFilePath);
            if (!delegationTokenFileFd.exists()) {
                throw new IOException("delegation token file does not " +
                        "exist [" + delegationTokenFileFd + "]");
            }
        }
    }

    private void initValidateCredentialInfo() {
        final String credTypeName = Configuration.CRED_TYPE_PROP;
        final String credTypeVal =
                ConfigUtils.readString(configMap, credTypeName);
        if (credTypeVal == null) {
            throw new InvalidConfigException(
                        "no value specified for '" + credTypeName + "' " +
                        "config entry. Value specified was " + credTypeVal +
                        ", but must be one of: " + CRED_TYPE_LIST);
        }
        final String credTypeLower = credTypeVal.trim().toLowerCase();
        if (!CRED_TYPE_LIST.contains(credTypeLower)) {
            throw new InvalidConfigException(
                        "invalid value specified for '" + credTypeName + "' " +
                        "config entry. Value specified was " + credTypeLower +
                        ", but must be one of: " + CRED_TYPE_LIST);
        }
        if (CredentialsTypeEnum.CREDENTIALS_TYPE_FILE.toString().equals(credTypeLower)) {
            authByCredentialsFile = true;
            authByInstancePrincipal = false;
            authByDelegationToken = false;
        }
        if (CredentialsTypeEnum.CREDENTIALS_TYPE_PRINCIPAL.toString().equals(credTypeLower)) {
            authByCredentialsFile = false;
            authByInstancePrincipal = true;
            authByDelegationToken = false;
        }
        if (CredentialsTypeEnum.CREDENTIALS_TYPE_TOKEN.toString().equals(credTypeLower)) {
            authByCredentialsFile = false;
            authByInstancePrincipal = false;
            authByDelegationToken = true;
        }
    }

    protected void validateIdentifier(final String identifierValue,
                                      final int identifierMaxLen,
                                      final String identifierType) {

        if (!identifierValue.matches(VALID_IDENTIFIER_CHAR_REGEX)) {
            throw new InvalidConfigException(
                "invalid value specified for '" + identifierType + "' " +
                "config entry. Value specified was " + identifierValue +
                ", but must contain only alphanumeric characters, " +
                "plus the character \"_\"");
        }
        if (!Character.isLetter(identifierValue.charAt(0)) ||
            (identifierValue.charAt(0) == '_')) {
            throw new InvalidConfigException(
                "invalid value specified for '" + identifierType + "' " +
                "config entry. Value specified was " + identifierValue +
                ", but must start with alphanumeric character");
        }
        if (identifierValue.length() > identifierMaxLen) {
            throw new InvalidConfigException(
                "invalid value specified for '" + identifierType + "' " +
                "config entry. Value specified was " + identifierValue +
                ", with length equal to " + identifierValue.length() +
                " characters, but must have no more than " + identifierMaxLen +
                "characters");
        }
    }
    
    public MapValue getConfigMap() {
        return configMap;
    }

    public String getCredentialsType() {
        return ConfigUtils.readString(configMap, Configuration.CRED_TYPE_PROP);
    }

    public boolean useCredentialsFile() {
        return authByCredentialsFile;
    }

    public boolean useInstancePrincipal() {
        return authByInstancePrincipal;
    }

    public boolean useDelegationToken() {
        return authByDelegationToken;
    }

    public String getRegion() {
        return ConfigUtils.readString(configMap, Configuration.REGION_PROP);
    }

    public String getCompartment() {
        return ConfigUtils.readString(configMap, Configuration.COMPARTMENT_PROP);
    }

    public String getTableName() {
        return ConfigUtils.readString(configMap, Configuration.TABLE_PROP);
    }

    public boolean deleteRows() {
        return ConfigUtils.readBoolean(configMap, Configuration.DELETE_PROP);
    }

    public Integer getNRows() {
        return ConfigUtils.readInt(configMap, Configuration.N_ROWS_PROP);
    }

    public Integer getReadUnits() {
        return ConfigUtils.readInt(configMap, Configuration.READ_UNITS_PROP);
    }

    public Integer getWriteUnits() {
        return ConfigUtils.readInt(configMap, Configuration.WRITE_UNITS_PROP);
    }

    public Integer getStorageGb() {
        return ConfigUtils.readInt(configMap, Configuration.STORAGE_GB_PROP);
    }

    public Integer getTtlDays() {
        return ConfigUtils.readInt(configMap, Configuration.TTL_DAYS_PROP);
    }

    public String getCredentialsFile() {
        return ConfigUtils.readString(
                   configMap, Configuration.CREDENTIALS_FILE_PROP);
    }

    public String getCredentialsProfile() {
        return ConfigUtils.readString(
                   configMap, Configuration.CREDENTIALS_PROFILE_PROP);
    }

    public String getTenancy() {
        return ConfigUtils.readString(configMap, Configuration.TENANCY_PROP);
    }

    public String getUserOcid() {
        return ConfigUtils.readString(configMap, Configuration.USER_PROP);
    }

    public String getFingerprint() {
        return ConfigUtils.readString(configMap, Configuration.FINGERPRINT_PROP);
    }

    public File getPrivateKeyFd() {
        return privateKeyFd;
    }

    public char[] getPassphrase() {
        return passphrase;
    }

    public File getDelegationTokenFd() {
        return delegationTokenFileFd;
    }

    @Override
    public String toString() {
        return configMap.toString();
    }
}

