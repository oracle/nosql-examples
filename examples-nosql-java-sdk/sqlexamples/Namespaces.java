/*Copyright (c) 2020, 2023 Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;
import oracle.nosql.driver.ops.SystemResult;
import oracle.nosql.driver.ops.SystemRequest;

public class Namespaces{
   /* Name of your namespace */
   final static String nsName = "ns1";

   public static void main(String[] args) throws Exception {
      //Replace the placeholder below with your full hostname
      String kvstore_endpoint ="http://<your_hostname>:8080";
      NoSQLHandle handle = generateNoSQLHandleonPrem(kvstore_endpoint);
      try {
         createNS(handle);
         dropNS(handle);
      } catch (Exception e) {
         System.err.print(e);
      } finally {
         handle.close();
      }
   }
   /* Create a NoSQL handle to access the onPremise Oracle NoSQL database */
   private static NoSQLHandle generateNoSQLHandleonPrem(String kvstore_endpoint) throws Exception {
      NoSQLHandleConfig config = new NoSQLHandleConfig(kvstore_endpoint);
      config.setAuthorizationProvider(new StoreAccessTokenProvider());
      /* If using a secure store pass the username, password of the store to StoreAccessTokenProvider*/
      /*config.setAuthorizationProvider(new StoreAccessTokenProvider(username, password));*/
      NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
      return handle;
   }

   private static void createNS(NoSQLHandle handle) throws Exception {
      String createNSDDL = "CREATE NAMESPACE IF NOT EXISTS ns1";
      SystemRequest sysreq = new SystemRequest();
      sysreq.setStatement(createNSDDL.toCharArray());
      SystemResult sysres = handle.systemRequest​(sysreq);
      sysres.waitForCompletion​(handle, 60000,1000);
      System.out.println("Namespace " + nsName + " is created");
   }
   private static void dropNS(NoSQLHandle handle) throws Exception {
      String dropNSDDL = "DROP NAMESPACE " + nsName;
      SystemRequest sysreq = new SystemRequest();
      sysreq.setStatement(dropNSDDL.toCharArray());
      SystemResult sysres = handle.systemRequest​(sysreq);
      sysres.waitForCompletion​(handle, 60000,1000);
      System.out.println("Namespace " + nsName + " is dropped");
   }
}
