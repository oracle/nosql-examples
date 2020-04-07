import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.Region;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.ops.*;
import oracle.nosql.driver.values.JsonOptions;
import oracle.nosql.driver.values.MapValue;

import java.io.File;

public class HelloWorld {

    /**
     * Authenticate with the NoSQL cloud service
     *
     * @return A session handle to the NoSQL Database cloud service
     */
    private static NoSQLHandle getNoSQLConnection() {

        SignatureProvider authProvider = new SignatureProvider(
                "Your tenant OCID goes here",
                "Your user OCID goes here",
                "Your key fingerprint goes here",
                new File("~/.oci/oci_api_key.pem"), // path to your private key file
                "The pass phrase for your key goes here".toCharArray());
        /**
	 * Use the Ashburn, US region in this example OR
	 * Zurich, Switzerland by changing to Region.EU_ZURICH_1
      	 */
        return(NoSQLHandleFactory.createNoSQLHandle(
                new NoSQLHandleConfig(Region.US_ASHBURN_1, authProvider)));
    }
 
    /**
     * Creates a table with two columns: An ID PK and some JSON content
     *
     * @param serviceHandle Authenticated NoSQL Database cloud service handle
     *
     * @throws Exception on Unexpected table creation error
     */
    private static void createHelloWorldTable(NoSQLHandle serviceHandle) 
	throws Exception {
        TableRequest req = new TableRequest().setStatement(
          	"CREATE TABLE if not exists hello_world(id LONG, " +
        		"content JSON, primary key (id))");
        /**
	 * Set the number of reads per sec to 25, writes per second to 25, 
	 * and GB storage to 25
	 */
        req.setTableLimits(new TableLimits(25, 25, 25));

        TableResult tr = serviceHandle.tableRequest(req);

        /**
  	 * Table creation is async so wait for the table to become active 
	 * before returning.  This call will wait for a total of up to 2 minutes
	 * for the table to become up and will check every 500 milliseconds
	 * to see if the table has become active
	 */
        tr.waitForCompletion(serviceHandle, 120000, 500);


        /*  
	 * We waited long enough.  If the table is still not active then 
	 * something went wrong
	 */
        if (tr.getTableState() != TableResult.State.ACTIVE)  {
            throw new Exception("Unable to create table hello_world " +
 			    tr.getTableState());
        }
     }

    /**
     * Writes a single record to the "hello_world" table
     *
     * @param serviceHandle Authenticated NoSQL Database cloud service handle
     * @param id Primary key.  If the key exists, the record will be overwritten
     * @param jsonContent The content to write
     */
    private static void writeOneRecord(NoSQLHandle serviceHandle, long id, 
			String jsonContent) {
        MapValue value = new MapValue().put("id", id);

        MapValue contentVal = value.putFromJson("content", jsonContent, 
				new JsonOptions());
        PutRequest putRequest = new PutRequest()
                .setValue(value)
                .setTableName("hello_world");
        serviceHandle.put(putRequest);
    }

    /**
     * Reads a single record from the "hello_world" table
     *
     * @param serviceHandle Authenticated NoSQL Database cloud service handle
     * @param id PK of the record to read
     * @return The JSON string containing the content of the requested record 
     *         or null if the requested record does not exist
     */
    private static String readOneRecord(NoSQLHandle serviceHandle, long id) {
        GetRequest getRequest = new GetRequest();
        getRequest.setKey(new MapValue().put("id", id));
        getRequest.setTableName("hello_world");

        long before = System.currentTimeMillis();
        GetResult gr = serviceHandle.get(getRequest);

        if (gr != null) {
            return (gr.getValue().toJson(new JsonOptions()));
        } else {
            return(null);
        }
    }

    public static void main (String args[]) {
        try {
            NoSQLHandle handle = getNoSQLConnection();
            createHelloWorldTable(handle);
            writeOneRecord(handle, 1, "{\"hello\":\"world\"}");
            System.out.println(readOneRecord(handle, 1));
	    System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
	   System.exit(-1);
        }
    }
}
