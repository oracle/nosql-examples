/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package nosql.cloud.table;

import static nosql.cloud.table.config.Config.CRED_TYPE_LIST;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import nosql.cloud.table.config.Config;
import nosql.cloud.table.config.Configuration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Base class for complex table creation, populate, and load
 */
public class ComplexTableTestBase {

    private static final Class THIS_CLASS = ComplexTableTestBase.class;
    private static final String THIS_CLASS_FQN = THIS_CLASS.getName();
    private static final Logger logger = Logger.getLogger(THIS_CLASS_FQN);

    protected static final String F_SEP = System.getProperty("file.separator");
    protected static final String L_SEP = System.getProperty("line.separator");

    protected static final String OPEN_BRACE = "{";
    protected static final String D_QUOTE = "\"";
    protected static final String D_QUOTE_COMMA = ",";
    protected static final String COLON = " : ";
    protected static final String COMMA = ",";
    protected static final String INDENT_4 = "    ";
    protected static final String INDENT_8 = INDENT_4 + INDENT_4;
    protected static final String EMPTY_STRING = "";
    protected static final String CLOSE_BRACE = "}";

    protected static final String TEST_DIR =
        System.getProperty("java.io.tmpdir") +
        System.getProperty("user.name") + F_SEP + "ComplexTableTestDir";

    protected static final String CREDENTIALS_FILE = "config.complex-table-file";
    protected static final String COMPARTMENT_OCID =
            "ocid1.compartment.oc1..abcdefghijklmnopqrstuvwxyz";
    protected static final String TENANCY_OCID =
            "ocid1.tenancy.oc1..abcdefghijklmnopqrstuvwxyz";
    protected static final String USER_OCID =
            "ocid1.user.oc1..abcdefghijklmnopqrstuvwxyz";
    protected static final String FINGERPRINT =
            "19:80:2a:cc:41:dc:9d:7a:3f:74:06:e5:d2:15:cb:f0";
    protected static final String KEY_FILE = "oci_api_key_private.pem";
    protected static final String PASSPHRASE_STR = "example_passphrase_for_key_file";

    protected static final Map<String, String> FILE_CREDS_CONFIG_MAP =
                                                 new LinkedHashMap<>();
    protected static final Map<String, String> PRINCIPAL_CREDS_CONFIG_MAP =
                                                 new LinkedHashMap<>();
    protected static final Map<String, String> TOKEN_CREDS_CONFIG_MAP =
                                                 new LinkedHashMap<>();
    protected static final Map<String, Map<String, String>> CONFIG_MAP =
                                                 new LinkedHashMap<>();
    static {
        FILE_CREDS_CONFIG_MAP.put(Configuration.CRED_TYPE_PROP, CRED_TYPE_LIST.get(0));
        FILE_CREDS_CONFIG_MAP.put(Configuration.REGION_PROP, "us-ashburn-1");
        FILE_CREDS_CONFIG_MAP.put(Configuration.COMPARTMENT_PROP, COMPARTMENT_OCID);
        FILE_CREDS_CONFIG_MAP.put(Configuration.TABLE_PROP , "compex_table_file");
        FILE_CREDS_CONFIG_MAP.put(Configuration.DELETE_PROP, "true");
        FILE_CREDS_CONFIG_MAP.put(Configuration.N_ROWS_PROP, "37");
        FILE_CREDS_CONFIG_MAP.put(Configuration.READ_UNITS_PROP, "41");
        FILE_CREDS_CONFIG_MAP.put(Configuration.WRITE_UNITS_PROP, "53");
        FILE_CREDS_CONFIG_MAP.put(Configuration.STORAGE_GB_PROP, "23");
        FILE_CREDS_CONFIG_MAP.put(Configuration.TTL_DAYS_PROP, "3");
        FILE_CREDS_CONFIG_MAP.put(Configuration.CREDENTIALS_PROFILE_PROP,
                                  Config.DEFAULT_CREDENTIALS_PROFILE);
        FILE_CREDS_CONFIG_MAP.put(Configuration.CREDENTIALS_FILE_PROP,
                                  TEST_DIR + F_SEP + CREDENTIALS_FILE);

        CONFIG_MAP.put(CRED_TYPE_LIST.get(0), FILE_CREDS_CONFIG_MAP);

        PRINCIPAL_CREDS_CONFIG_MAP.put(Configuration.CRED_TYPE_PROP, CRED_TYPE_LIST.get(1));
        PRINCIPAL_CREDS_CONFIG_MAP.put(Configuration.REGION_PROP, "us-sanjose-1");
        PRINCIPAL_CREDS_CONFIG_MAP.put(Configuration.COMPARTMENT_PROP, COMPARTMENT_OCID);
        PRINCIPAL_CREDS_CONFIG_MAP.put(Configuration.TABLE_PROP , "compex_table_principal");
        PRINCIPAL_CREDS_CONFIG_MAP.put(Configuration.DELETE_PROP, "false");
        PRINCIPAL_CREDS_CONFIG_MAP.put(Configuration.N_ROWS_PROP, "39");
        PRINCIPAL_CREDS_CONFIG_MAP.put(Configuration.READ_UNITS_PROP, "43");
        PRINCIPAL_CREDS_CONFIG_MAP.put(Configuration.WRITE_UNITS_PROP, "51");
        PRINCIPAL_CREDS_CONFIG_MAP.put(Configuration.STORAGE_GB_PROP, "17");
        PRINCIPAL_CREDS_CONFIG_MAP.put(Configuration.TTL_DAYS_PROP, "5");

        CONFIG_MAP.put(CRED_TYPE_LIST.get(1), PRINCIPAL_CREDS_CONFIG_MAP);

        TOKEN_CREDS_CONFIG_MAP.put(Configuration.CRED_TYPE_PROP, CRED_TYPE_LIST.get(2));
        TOKEN_CREDS_CONFIG_MAP.put(Configuration.REGION_PROP, "us-phoenix-1");
        TOKEN_CREDS_CONFIG_MAP.put(Configuration.COMPARTMENT_PROP, COMPARTMENT_OCID);
        TOKEN_CREDS_CONFIG_MAP.put(Configuration.TABLE_PROP , "compex_table_token");
        TOKEN_CREDS_CONFIG_MAP.put(Configuration.DELETE_PROP, "true");
        TOKEN_CREDS_CONFIG_MAP.put(Configuration.N_ROWS_PROP, "31");
        TOKEN_CREDS_CONFIG_MAP.put(Configuration.READ_UNITS_PROP, "41");
        TOKEN_CREDS_CONFIG_MAP.put(Configuration.WRITE_UNITS_PROP, "59");
        TOKEN_CREDS_CONFIG_MAP.put(Configuration.STORAGE_GB_PROP, "11");
        TOKEN_CREDS_CONFIG_MAP.put(Configuration.TTL_DAYS_PROP, "7");

        CONFIG_MAP.put(CRED_TYPE_LIST.get(2), TOKEN_CREDS_CONFIG_MAP);
    }

    protected static final String[] VALID_CONFIG_JSON_FLNMS = {
            "valid_config_credentials_type_" + CRED_TYPE_LIST.get(0),
            "valid_config_credentials_type_" + CRED_TYPE_LIST.get(1),
            "valid_config_credentials_type_" + CRED_TYPE_LIST.get(2)
    };

    protected static final String[] KEY_FILE_CONTENTS =
    {
        "-----BEGIN RSA PRIVATE KEY-----",
        "MIIEowIBAAKCAQEAm9IHPcDko5QefButdbM9+mGo1TbxpROFWkFf6TZBykJMMx6+",
        "4PT4DO9rpAB8/tPA9LLKn7KeXXSciGUzutdr2jVPsr1HvkkqaiqXlD9MjmxensEx",
        "cSIuzvde0bAw0eB7AuGnG0bwruhNrckuamsD7q0O162I+AaD8h22",
        "-----END RSA PRIVATE KEY-----"
    };

    protected static final String[] CREDENTIALS_FILE_DEFAULT =
    {
        "[DEFAULT]",
        "tenancy=" + TENANCY_OCID,
        "user=" + USER_OCID,
        "fingerprint=" + FINGERPRINT,
        "key_file=" + TEST_DIR + F_SEP + KEY_FILE,
        "passphrase=" + PASSPHRASE_STR
    };

    protected static final String OCI_OBO_TOKEN_FLNM = "delegation_token";
    protected static final String OCI_OBO_TOKEN =
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "jIiwic3ViIjoib2NpZDEudXNlci5vYzEuLmFhYWFhYWFhNnQyM2hvNmZoZnRmdGdjdj" +
        "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
        "Gd0X25hbWUiOiJhbGxfc3ZjIiwiZGdycHMiOiJbXCJvY2lkMS5keW5hbWljZ3JvdXAu" +
        "ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc" +
        "veGMyYjVjY3FqYW9ndnFcIl0iLCJ0Z3RzIjoiW1wiYWxsX3N2Y1wiXSIsIm1mYV92ZX" +
        "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD" +
        "WVvaXI0Y3BydWdlbHN1dWRoaTJkbjJpczZsaGozdG1naWt6bmxwNGI3dGx1Nmgycmtp" +
        "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
        "0Z3RfbmFtZXMiOiJbXCJhbGxfc3ZjXCJdIiwiYXVkIjoib2NpIiwicHN0eXBlIjoibm" +
        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
        "jcsImp0aSI6ImZlZjEzNmFjLTk1NTUtNDYzOS1iYzY4LWUwMmNjOTNmNWNmNSIsInRl" +
        "ggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg" +
        "6eWlkc2hkYnJmbW1lZmx2NGtrZW1hanJvejJ0aHZjYTRrYmEiLCJuYW1lLWNoYWluIj" +
        "HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH" +
        "lx8ZtIVP7fXKkIw9ZTIHghTADnP6hrfe4Fc-CuOh6-4tEbXQrkhcxMgjX6HRm7RZIqq" +
        "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii" +
        "4EY1JxIBK44e0Q_jq8Jl8amcySRgOZ3ZM_pmOxP60Z23yRLZtSGvqnuFNiWtKNQGr_Y" +
        "JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ" +
        "ClfhsVlyX-ufA";

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void testSetup() throws IOException {

        logger.finest("--- testSetup ---");

        final File testDirFd = getTestDir();

        /* Create valid JSON config files and their contents. */
        for (int i = 0; i < CRED_TYPE_LIST.size(); i++) {
            final File jsonConfigFd =
                    createJsonConfigFile(VALID_CONFIG_JSON_FLNMS[i],
                                         CRED_TYPE_LIST.get(i));
            logger.fine("create & populate valid json config file " +
                    "[" + jsonConfigFd + "]");
        }

        /* Credentials files containing user OCID, fingerprint, etc. */
        final File fileCredsFd = createFile(testDirFd, CREDENTIALS_FILE);
        logger.fine("create and populate credentials file [" +
                     fileCredsFd + "]");
        Files.write(
            fileCredsFd.toPath(), Arrays.asList(CREDENTIALS_FILE_DEFAULT));

        /* Mock the key_file entry in each credentials file. */
        logger.fine("create and populate private key_file [" +
                     TEST_DIR + F_SEP + KEY_FILE + "]");
        final File keyFileFd =
            createFile(testDirFd, KEY_FILE);
        Files.write(
            keyFileFd.toPath(), Arrays.asList(KEY_FILE_CONTENTS));

        /* Mock the OCI_obo_token_path file. */
        logger.fine("create and populate oci obo token file [" +
                TEST_DIR + F_SEP + OCI_OBO_TOKEN_FLNM + "]");
        final File ociOboTokenFd =
                createFile(testDirFd, OCI_OBO_TOKEN_FLNM);
        Files.write(ociOboTokenFd.toPath(), OCI_OBO_TOKEN.getBytes());
    }

    @AfterClass
    public static void testTeardown() {
        logger.finest("--- testTeardown ---");
//TODO: Un-comment this call when completely done
//        clearTestDirectory();
    }

    /**
     * @return directory for test staging.
     */
    public static File getTestDir() {

        logger.finest("--- getTestDir ---");
        final Path testDirPath = Paths.get(TEST_DIR);
        if (!Files.exists(testDirPath)) {
            try {
                Files.createDirectories(testDirPath);
            } catch (IOException e) {
                fail("error creating test directory [" + TEST_DIR + "]: " + e);
            }
        }
        return testDirPath.toFile();
    }

    /**
     * Remove all files and directories from the test instance directory.
     */
    public static void clearTestDirectory() {

        logger.finest("--- clearTestDirectory ---");
        File testDir = getTestDir();
        if (!testDir.exists()) {
            return;
        }
        clearDirectory(getTestDir());
    }

    /* Clears out the contents of the directory, recursively */
    private static void clearDirectory(File dir) {
        logger.finest("--- clearDirectory ---");
        if (dir.listFiles() == null) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                clearDirectory(file);
            }
            boolean deleteDone = file.delete();
            assert deleteDone : "Couldn't delete " + file;
        }
    }

    /*
     * Creates a file in test dir if not exists, or clear the content of the
     * file if exists.
     */
    static File createFile(final File pathFd, final String fileName) {
        File file = new File(pathFd, fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                new FileOutputStream(file).close();
            }
            return file;
        } catch (IOException e) {
            fail("Failed to create a file: " + file);
        }
        return null;
    }

    /**
     * If injectError is false, then create a 'valid' config.
     *
     * If injectError is true, then badFieldName must be non-null. And,
     *
     *   - credType must be non-null. And,
     *     o if badFieldVal is null, then exclude the line(s) associated
     *       with the given field name from the file.
     *     o if badFieldVal is non-null, then overwrite the preset value
     *       of the line(s) associated with the given field name.
     */
    protected static File createJsonConfigFile(final String configFilename,
                                               final boolean injectError,
                                               final boolean deleteField,
                                               final String credType,
                                               final String badFieldName,
                                               final String badStrFieldVal,
                                               final int badIntFieldVal)
                                                   throws IOException {
        logger.finest("--- createJsonConfigFile ---");

        if (credType == null) {
            throw new NullPointerException("credType is null");
        }

        if (injectError && badFieldName == null) {
            throw new NullPointerException("createJsonConfigFile: " +
                "'badFieldName' argument cannot be null when 'true' input " +
                "for 'injectError' argument");
        }

        final File testDirFd = getTestDir();

        /* JSON config file. */
        final File configFd = createFile(testDirFd, configFilename);
        final List<String> configJson =
            createConfigurationJson(injectError, deleteField,
                                    credType, badFieldName,
                                    badStrFieldVal, badIntFieldVal);
        Files.write(configFd.toPath(), configJson);
        logger.fine("json config file created: [" + configFd + "]");
        return configFd;
    }

    protected static File createJsonConfigFile(final String configFilename,
                                               final String credType)
                                                      throws IOException {
        return createJsonConfigFile(configFilename,
                                   false,
                                   false,
                                   credType,
                                   null,
                                   null,
                                   0);
    }

    /*
     * If injectError is false, then generate a valid config file with no
     * errors. Otherwise, if injectError is true, then generate a config
     * that is valid in all ways except for one; where the type of error that
     * is injected into the file is determined in the following way:
     *
     * If excludeField is true, then the line corresponding to the
     * badFieldName is NOT written to the file, and badStrFieldVal and
     * badIntFieldVal are ignored.
     *
     * If excludeField is false, then the line corresponding to the
     * badFieldName will be overwritten with an invalid value. The
     * invalid value that is used to overwrite the bad field is dependent
     * on the value of the badStrFieldVal. If badStrFieldVal is null, then
     * the value of badIntFieldVal will be used; where the value in
     * the overwritten line will be an int with no encapsulating quotes.
     *
     * On the other hand, if If badStrFieldVal is non-null, then the
     * value of badStrFieldVal will be used as the value of the
     * overwritten. This option is used to verify how the config mechanism
     * handles the situation where the contents of the field string value
     * may be invalid.
     *
     * Note that if injectError is true, then badFieldVal must be non-null;
     * which is checked and flagged in the method that invokes this method
     * (createJsonConfigFile).
     */
    private static List<String> createConfigurationJson(
                                      final boolean injectError,
                                      final boolean excludeField,
                                      final String credType,
                                      final String badFieldName,
                                      final String badStrFieldVal,
                                      final int badIntFieldVal)
                                                       throws IOException {
        final List<String> retList = new ArrayList<>();
        retList.add(OPEN_BRACE);
        final Map<String, String> entriesMap = CONFIG_MAP.get(credType);

        final Set<Map.Entry<String, String>> entriesSet = entriesMap.entrySet();
        int lineCntr = 0;
        for (Map.Entry<String, String> nameValueEntry : entriesSet) {

            final String entryName = nameValueEntry.getKey();
            String entryVal = nameValueEntry.getValue();

            String valStr = D_QUOTE + entryVal + D_QUOTE;
            String entryValLowerCase = entryVal.toLowerCase();

            /* Encapsulate in quotes unless it's boolean. */
            if ("true".equals(entryValLowerCase) ||
                "false".equals(entryValLowerCase)) {
                valStr = entryVal;
            }
            if (injectError) {
                if (badFieldName.equals(entryName)) {
                    if (excludeField) {
                        lineCntr++; /* avoid double commas */
                        /*
                         * If deleting the last line, then need to
                         * remove the comma from the previous line;
                         * which becomes the new last line.
                         */
                        if (lineCntr == entriesSet.size()) {
                            /* Get previous line with comma. */
                            final int lastIndx = retList.size() - 1;
                            final String prevLine = retList.get(lastIndx);

                            /* Remove previous line with comma. */
                            retList.remove(lastIndx);

                            /* Strip off comma from previous line. */
                            final int begIndx = 0;
                            final int endIndx = prevLine.length() - 1;
                            final String prevLineNoComma =
                                    prevLine.substring(begIndx, endIndx);

                            /* Add new previous line but w/o comma. */
                            retList.add(prevLineNoComma);
                        }
                        continue; /* skip/delete current line */
                    }
                    /*
                     * Reaching this point means that instead of
                     * excluding the current line, a 'bad' value
                     * will be injected in the current line. In
                     * that case, if badStrFieldVal is null, then
                     * the bad int value should be injected;
                     * otherwise, the bad string should be injected.
                     */
                    if (badStrFieldVal == null) {
                        valStr = String.valueOf(badIntFieldVal);
                    } else {
                        valStr = D_QUOTE + badStrFieldVal + D_QUOTE;
                    }
                }
            }
            String configLine =
                    INDENT_4 + D_QUOTE + entryName + D_QUOTE + COLON + valStr;
            if (lineCntr < (entriesSet.size() - 1)) {
                configLine = configLine + COMMA;
            }
            retList.add(configLine);
            lineCntr++;
        }
        retList.add(CLOSE_BRACE);
        return retList;
    }
}
