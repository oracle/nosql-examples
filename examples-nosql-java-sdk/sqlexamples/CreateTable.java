/*Copyright (c) 2020, 2023 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
import java.io.File;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.Region;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.QueryResult;
import oracle.nosql.driver.ops.GetRequest;
import oracle.nosql.driver.ops.GetResult;
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.ops.PutResult;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableResult;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.ops.QueryIterableResult;

public class CreateTable{
    /* Name of your table */
   final static String tableName = "stream_acct";

   public static void main(String[] args) throws Exception {
        NoSQLHandle handle = generateNoSQLHandle();
        try {
          createTab(handle);

       } catch (Exception e) {
            System.err.print(e);
        } finally {
            handle.close();
        }
    }

    /* Create a NoSQL handle to access the cloud service */
    private static NoSQLHandle generateNoSQLHandle() throws Exception {

        SignatureProvider ap = new SignatureProvider();
        /* Create a NoSQL handle to access the cloud service */
        NoSQLHandleConfig config = new NoSQLHandleConfig(Region.US_ASHBURN_1, ap);
        // set your compartment OCID here
        config.setDefaultCompartment("<your_compartment_ocid>");
        NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
        return handle;
    }

    /**
     * Create a table stream_acct and set your desired table capacity
     */
    private static void createTab(NoSQLHandle handle) throws Exception {
        String createTableDDL = "CREATE TABLE IF NOT EXISTS " + tableName +
                                                              "(acct_Id INTEGER," +
                                                              "profile_name STRING," +
                                                              "account_expiry TIMESTAMP(1) ," +
                                                              "acct_data JSON, " +
                                                              "PRIMARY KEY(acct_Id))";

        TableLimits limits = new TableLimits(20, 20, 1);
        TableRequest treq = new TableRequest()
            .setStatement(createTableDDL).setTableLimits(limits);

        System.out.println("Creating table " + tableName);
        TableResult tres = handle.tableRequest(treq);

        /* The request is async,
         * so wait for the table to become active.
        */
        System.out.println("Waiting for "
		    + tableName + " to become active");
        tres.waitForCompletion(handle, 60000, /* wait 60 sec */
            1000); /* delay ms for poll */
        System.out.println("Created Table: " + tableName);
    }
}
