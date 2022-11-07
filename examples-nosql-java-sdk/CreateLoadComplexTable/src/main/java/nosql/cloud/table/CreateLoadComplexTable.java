package nosql.cloud.table;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import nosql.cloud.table.config.Configuration;
import nosql.cloud.table.config.Config;

import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.Region;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.ops.DeleteRequest;
import oracle.nosql.driver.ops.DeleteResult;
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.ops.PutResult;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.QueryResult;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableResult;
import oracle.nosql.driver.util.TimestampUtil;
import oracle.nosql.driver.values.ArrayValue;
import oracle.nosql.driver.values.MapValue;

/**
 * Class that creates an example table in the Oracle NoSQL Database Cloud
 * Service, then uses the Table API to populate the table with sample records.
 * The table that is created consists of all types (primitive and complex)
 * supported by the Oracle NoSQL Database Cloud Service.
 */
public final class CreateLoadComplexTable {

    private static final Class THIS_CLASS = CreateLoadComplexTable.class;
    private static final String THIS_CLASS_FQN = THIS_CLASS.getName();
    private static final Logger logger = Logger.getLogger(THIS_CLASS_FQN);

    private static final SecureRandom generator = new SecureRandom();

    private final Configuration config;
    private final NoSQLHandle ociNoSqlHndl;

    private String regionName = "us-sanjose-1"; /* default */
    private String compartment = "";
    private String tableName = "";
    private boolean deleteExisting = Config.DEFAULT_DELETE_EXISTING;
    private long nOps = Config.DEFAULT_N_ROWS;
    private int readUnits = Config.DEFAULT_READ_UNITS;
    private int writeUnits = Config.DEFAULT_WRITE_UNITS;
    private int storageGb = Config.DEFAULT_STORAGE_GB;
    private int ttlDays = Config.DEFAULT_TTL_DAYS;

    private long nRowsAdded;

    public static void main(final String[] args) {
        try {
            final CreateLoadComplexTable loadData =
                new CreateLoadComplexTable(args);
            loadData.run();
            System.exit(0);
        } catch (Throwable e) {
            e.printStackTrace();
            logger.severe("Failed to create and populate the " +
                          "requested table");
            System.exit(1);
        }
    }

    private CreateLoadComplexTable(final String[] argv) throws IOException {

        String configFile = null;
        final int nArgs = argv.length;
        int argc = 0;

        if (nArgs == 0) {
            usage(null);
        }

        while (argc < nArgs) {

            final String thisArg = argv[argc++];

            if ("-config".equals(thisArg)) {
                if (argc < nArgs) {
                    configFile = argv[argc++];
                } else {
                    usage("-config argument requires an argument");
                }
            } else if ("-help".equals(thisArg)) {
                usage(null, 0);
            } else {
                usage("Unknown argument: " + thisArg);
            }
        }
        if (configFile == null) {
            usage("missing required argument [-config <path to config file>]");
        }

        final File fileFd = new File(configFile);
        if (!fileFd.exists()) {
            throw new IOException("config file does not exist [" +
                    fileFd + "]");
        }

        this.config = new Config(fileFd);

        this.regionName = config.getRegion();
        this.compartment = config.getCompartment();
        this.tableName = config.getTableName();
        this.deleteExisting = config.deleteRows();
        this.nOps = config.getNRows();
        this.readUnits = config.getReadUnits();
        this.writeUnits = config.getWriteUnits();
        this.storageGb = config.getStorageGb();
        this.ttlDays = config.getTtlDays();

        this.nRowsAdded = nOps;

        if (logger.isLoggable(Level.INFO)) {
            logger.info("COMPARTMENT: " + compartment);
            logger.info("TABLE: " + tableName);
        }

        SignatureProvider authProvider = null;
        if (config.useCredentialsFile()) {
            authProvider = new SignatureProvider(config.getTenancy(),
                                                 config.getUserOcid(),
                                                 config.getFingerprint(),
                                                 config.getPrivateKeyFd(),
                                                 config.getPassphrase());
        } else if (config.useDelegationToken()) {
            authProvider =
                    SignatureProvider.createWithInstancePrincipalForDelegation(
                            config.getDelegationTokenFd());
        } else { /* instance principal auth. */
            authProvider = SignatureProvider.createWithInstancePrincipal();
        }
        final NoSQLHandleConfig config =
            new NoSQLHandleConfig(Region.fromRegionId(regionName), authProvider);
        ociNoSqlHndl = NoSQLHandleFactory.createNoSQLHandle(config);
        createTable();
    }

    private void usage(final String message) {
        usage(message, 1);
    }

    private void usage(final String message, final int exitCode) {
        if (message != null) {
            System.out.println("\n" + message + "\n");
        }
        System.out.println("usage: " +
                "java -Djava.util.logging.config.file=" +
                "./src/main/resources/logging/java-util-logging.properties " +
                "-Dlog4j.configuration=" +
                "file:./src/main/resources/logging/log4j2-complextable.properties " +
                "-jar ./lib/complextable-<version> " +
                "-config <path to json formatted config file>");
        System.exit(exitCode);
    }

    private void run() {
        if (deleteExisting) {
            deleteExistingData();
        }
        doLoad();
    }

    private void createTable() {

        /* Wait no more than 2 minutes for table creation. */
        final int waitMs = 2 * 60 * 1000;
        /* Check for table existence every 2 seconds. */
        final int delayMs = 2 * 1000;

        /* Table creation statement. */
        String statement =
            "CREATE TABLE IF NOT EXISTS " + tableName +
            " (" +
               "ID INTEGER," +
               "AINT INTEGER," +
               "ALON LONG," +
               "ADOU DOUBLE," +
               "ANUM NUMBER," +
               "AUUID STRING," +
               "ATIM_P0 TIMESTAMP(0)," +
               "ATIM_P3 TIMESTAMP(3)," +
               "ATIM_P6 TIMESTAMP(6)," +
               "ATIM_P9 TIMESTAMP(9)," +
               "AENU ENUM(S,M,L,XL,XXL,XXXL)," +
               "ABOO BOOLEAN," +
               "ABIN BINARY," +
               "AFBIN BINARY(16)," +
               "ARRY ARRAY (INTEGER)," +
               "AMAP MAP (DOUBLE)," +
               "AREC RECORD(" +
                            "BLON LONG," +
                            "BTIM_P6 TIMESTAMP(6)," +
                            "BNUM NUMBER," +
                            "BSTR STRING," +
                            "BRRY ARRAY(DOUBLE))," +
               "AJSON JSON," +
                   "PRIMARY KEY (SHARD(AINT), ALON, ADOU, ID)" +
              ")";

        if (ttlDays > 0) {
            statement = statement + " USING TTL " + ttlDays + " days";
        }

        logger.fine(statement);

        final TableRequest tblRqst = new TableRequest();
        tblRqst.setCompartment(compartment).setStatement(statement);

        final TableLimits tblLimits =
            new TableLimits(readUnits, writeUnits, storageGb);

        tblRqst.setTableLimits(tblLimits);

        final TableResult tblResult = ociNoSqlHndl.tableRequest(tblRqst);
        tblResult.waitForCompletion(ociNoSqlHndl, waitMs, delayMs);

        if (tblResult.getTableState() != TableResult.State.ACTIVE) {
            final String msg =
                "TIMEOUT: Failed to create table in OCI NoSQL [table=" +
                tableName + "]";
            throw new RuntimeException(msg);
        }
    }

    private void doLoad() {

        final List<MapValue> rows = generateData(nOps);
        for (MapValue row : rows) {
            addRow(row);
        }
        displayRow();
        final long nRowsTotal = nRowsInTable();
        if (nOps > nRowsAdded) {
            logger.info(
                nOps + " records requested, " +
                nRowsAdded + " unique records actually added " +
                "[" + (nOps - nRowsAdded) + " duplicates], " +
                nRowsTotal + " records total in table");
        } else {
            logger.info(
                nOps + " records requested, " +
                nRowsAdded + " unique records added, " +
                nRowsTotal + " records total in table");
        }
    }

    private void addRow(final MapValue row) {
        final PutRequest putRqst = new PutRequest();
        putRqst.setCompartment(compartment).setTableName(tableName);
        putRqst.setValue(row);

        final PutResult putRslt = ociNoSqlHndl.put(putRqst);

        if (putRslt.getVersion() == null) {
            final String msg =
                "PUT: Failed to insert row [table=" + tableName +
                ", row = " + row + "]";
        }
    }

    /* Retrieves and deletes each row from the table. */
    private void deleteExistingData() {
        final String selectAll = "SELECT * FROM " + tableName;
        final QueryRequest queryRqst = new QueryRequest();
        queryRqst.setCompartment(compartment).setStatement(selectAll);

        long cnt = 0;
        do {
            QueryResult queryRslt = ociNoSqlHndl.query(queryRqst);
            final List<MapValue> rowMap = queryRslt.getResults();
            for (MapValue row : rowMap) {
                final DeleteRequest delRqst = new DeleteRequest();
                delRqst.setCompartment(compartment).setTableName(tableName);
                delRqst.setKey(row);
                final DeleteResult delRslt = ociNoSqlHndl.delete(delRqst);
                if (delRslt.getSuccess()) {
                    cnt++;
                }
            }
        } while (!queryRqst.isDone());
        logger.info(cnt + " records deleted");
    }

    /* Counts the number of rows in the table. */
    private long nRowsInTable() {
        final String selectAll = "SELECT * FROM " + tableName;
        final QueryRequest queryRqst = new QueryRequest();
        queryRqst.setCompartment(compartment).setStatement(selectAll);

        long cnt = 0;
        do {
            QueryResult queryRslt = ociNoSqlHndl.query(queryRqst);
            final List<MapValue> rowMap = queryRslt.getResults();
            for (MapValue row : rowMap) {
                cnt++;
            }
        } while (!queryRqst.isDone());
        return cnt;
    }

    /* Convenience method for displaying output when debugging. */
    private void displayRow() {
        if (logger.isLoggable(Level.FINE)) {
            final String selectAll = "SELECT * FROM " + tableName;
            final QueryRequest queryRqst = new QueryRequest();
            queryRqst.setCompartment(compartment).setStatement(selectAll);
            do {
                QueryResult queryRslt = ociNoSqlHndl.query(queryRqst);
                final List<MapValue> rowMap = queryRslt.getResults();
                for (MapValue row : rowMap) {
                    logger.fine(row.toString());
                }
            } while (!queryRqst.isDone());
        }
    }

    /* Generates randomized data with which to populate the table. */
    private List<MapValue> generateData(final long count) {

        List<MapValue> rows = new ArrayList<>();

        final BigDecimal[] numberArray = {
                new BigDecimal("3E+8"),
                new BigDecimal("-1.7976931348623157E+2"),
                new BigDecimal("12345.76455"),
                new BigDecimal("12345620.789"),
                new BigDecimal("1234562078912345678988765446777475657"),
                new BigDecimal("1.7976931348623157E+305"),
                new BigDecimal("-1.7976931348623157E+304")
        };

        final Timestamp[] timeArray_p0 = {
               TimestampUtil.parseString("2010-05-05T10:45:00"),
               TimestampUtil.parseString("2011-05-05T10:45:01"),
               Timestamp.from(Instant.parse("2021-07-15T11:31:21Z"))
        };

        final Timestamp[] timeArray_p3 = {
               TimestampUtil.parseString("2011-05-05T10:45:01.123"),
               Timestamp.from(Instant.parse("2021-07-15T11:31:47.549Z")),
               Timestamp.from(Instant.parse("2021-07-15T11:32:12.836Z"))
        };

        final Timestamp[] timeArray_p6 = {
               TimestampUtil.parseString("2014-05-05T10:45:01.789456Z"),
               TimestampUtil.parseString("2013-08-20T12:34:56.123456Z"),
               Timestamp.from(Instant.parse("2021-07-15T11:31:47.549213Z")),
               Timestamp.from(Instant.parse("2021-07-15T11:32:12.567836Z"))
        };

        final Timestamp[] timeArray_p9 = {
               Timestamp.from(Instant.parse("2021-07-15T12:46:35.574639954Z")),
               Timestamp.from(Instant.parse("2021-07-15T12:47:32.883922660Z")),
               Timestamp.from(Instant.parse("2021-07-15T12:48:11.321131987Z"))
        };

        final String[] enumArray = {"S", "M", "L", "XL", "XXL", "XXXL"};

        for(int i = 1; i <= count; ++i) {
            byte[] byteArray = new byte[16];
            generator.nextBytes(byteArray);

            MapValue row = new MapValue(true,16);

            row.put("ID", i);
            row.put("AINT", generator.nextInt());
            row.put("ALON", mapLongVal(generator.nextLong()));

            if (i % 5 == 0) {
                row.put("ADOU", Double.POSITIVE_INFINITY);
            } else if (i % 3 == 0) {
                row.put("ADOU", Double.NEGATIVE_INFINITY);
            } else if (i % 7 == 0) {
                row.put("ADOU", Double.NaN);
            } else {
                row.put("ADOU", generator.nextDouble());
            }
            row.put("ANUM", 
                    numberArray[generator.nextInt(numberArray.length)]);
            row.put("AUUID", UUID.randomUUID().toString());

            /* TIMESTAMP */
            row.put("ATIM_P0",
                    timeArray_p0[generator.nextInt(timeArray_p0.length)]);
            row.put("ATIM_P3",
                    timeArray_p3[generator.nextInt(timeArray_p3.length)]);
            row.put("ATIM_P6",
                    timeArray_p6[generator.nextInt(timeArray_p6.length)]);
            row.put("ATIM_P9",
                    timeArray_p9[generator.nextInt(timeArray_p9.length)]);

            /* ENUM */
            row.put("AENU", enumArray[i % enumArray.length]);

            /* BOOLEAN */
            row.put("ABOO", generator.nextBoolean());

            /* BINARY & FIXED_BINARY stored as strings */
            row.put("ABIN",  byteArray);
            row.put("AFBIN", byteArray);

            /* ARRAY of INTEGER */
            ArrayValue integerArr = new ArrayValue();
            for (int j = 0; j < 3; ++j) {
                integerArr.add(generator.nextInt());
            }
            row.put("ARRY", integerArr);

            /* MAP of DOUBLE */
            MapValue map = new MapValue(true,3);
            map.put("d1", generator.nextDouble());
            map.put("d2", generator.nextDouble());
            row.put("AMAP", map);

            /* RECORD of LONG, TIMESTAMP, NUMBER, STRING, ARRAY of DOUBLE */
            MapValue record = new MapValue(true,5);

            /* LONG element */
            record.put("BLON", mapLongVal(generator.nextLong()));

            /* TIMESTAMP element */
            record.put("BTIM_P6",
                       timeArray_p6[generator.nextInt(timeArray_p6.length)]);

            /* NUMBER element */
            record.put("BNUM",
                       numberArray[generator.nextInt(numberArray.length)]);

            /* STRING element */
            record.put("BSTR", Double.toString(generator.nextDouble()));

            /* ARRAY of DOUBLE element */
            ArrayValue doubleArr = new ArrayValue();
            for (int j = 0; j < 3; ++j) {
                doubleArr.add(generator.nextDouble());
            }
            record.put("BRRY", doubleArr);
            row.put("AREC", record);

            /* JSON */
            MapValue json = new MapValue(true,5);
            json.put("id", i);
            json.put("name", "name_" + i);
            json.put("age", i + 10);
            row.put("AJSON", json);

            rows.add(row);
        }
        return rows;
    }

    /*
     * Convenience method that maps the given long value to value that
     * the OCI Console can display without error. The issue is that
     * if a field (or an element of a field) of the table being generated
     * by this program contains a long value that is outside of the
     * interval [-9007199254741111L, 9007199254741112L], then when the
     * user executes the query 'SELECT * FROM <table>' in the OCI Console,
     * the following error message is displayed by the console:
     *
     *   "[BAD_PROTOCOL_MESSAGE] Error in service protocol for
     *    operation QueryOp: [BAD_PROTOCOL_MESSAGE] End of stream
     *    reached while reading packed long"
     *
     * Note that any value in the interval [Long.MAX_VALUE, Long.MIN_VALUE]
     * (that is, [-9223372036854775808L,9223372036854775807L]) is a valid
     * long value. But [-9007199254741111L, 9007199254741112L] is only a
     * subset of the interval [Long.MAX_VALUE, Long.MIN_VALUE]. As a result,
     * to avoid confusing users, and having them possibly assume there is
     * an error in how the NoSql Cloud Service and/or Driver handles these
     * 'larger' long values, this method can be invoked to convert a long
     * value that causes problems for the console to a value that falls
     * in the smaller interval; that does not cause problems for the
     * console.
     *
     * This method thus allows this program to maintain some of the randomness
     * it originally intended in any field that specifies long values for
     * its contents, while avoiding the error message that can result when
     * using the OCI Console to query the table generated by this example
     * program.
     *
     * UPDATE: the issue above is a limitation of the javascript/node.js
     *         code used by the OCI Console when processing long values.
     *         A fix has been implemented. But as of 10/20/22, that fix
     *         has not been deployed, because the fix has not yet received
     *         approval. Once the fix is deployed, this method can then
     *         be removed.
     */
    private long mapLongVal(final long longVal) {
        final double L_END_PT = -9007199254741111d;
        final double R_END_PT =  9007199254741112d;
        double y1 = 0d;
        double y2 = 0d;
        double x1 = 0d;
        double x2 = 0d;
        double longDbl = (double) longVal;
        if (longDbl < L_END_PT) {
            /* Falls within [Long.MIN_VALUE, -9007199254741111L) */
            y1 = L_END_PT;
            y2 = 0L;
            x1 = (double) Long.MIN_VALUE;
            x2 = L_END_PT - 1L;
        } else if (longDbl > R_END_PT) {
            /* Falls within (9007199254741112L, Long.MAX_VALUE] */
            y1 = 0L;
            y2 = R_END_PT;
            x1 = R_END_PT + 1L;
            x2 = (double) Long.MAX_VALUE;
        } else {
            /* Falls within [-9007199254741111L, 9007199254741112L] */
            return longVal;
        }
        /* Map it to a value in [-9007199254741111L, 9007199254741112L] */
        final double m = (y2 - y1) / (x2 - x1); /* slope */
        /* Point-slope formula for linear mapping */
        final long mappedLongVal =
                (new Double((m * (longDbl - x1)) + y1)).longValue();
        return mappedLongVal;
    }
}
