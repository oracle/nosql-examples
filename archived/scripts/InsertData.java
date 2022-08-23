import oracle.kv.*;
import java.security.SecureRandom;
import java.math.BigInteger;

public class InsertData
{
    static KVStore store;
	
    public static void main(String[] args)
    {
    	//connecting to the NoSQL Database
        KVStoreConfig config = new KVStoreConfig("kvstore", "localhost:5000");
        store = KVStoreFactory.getStore(config);
	
       SecureRandom random = new SecureRandom();
       for(int i=1;;i++)
       {
    	   String key = new BigInteger(130, random).toString(32);
    	   Key mykey = Key.createKey( key );
    	   try{
    	    store.put(mykey,Value.createValue("data".getBytes()));
    	    System.out.print("Transaction " + i + " Succeeded\r");
    	    Thread.sleep(1000);
    	   }
    	   catch(Exception e)
    	   {
    		   System.err.println("Transaction " + i +" Failed    ");
    	   }
       }
     }
}
