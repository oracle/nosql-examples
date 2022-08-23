package com.oracle.kv.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.Durability;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyRange;


public class BulkDelete {

    public static  String KVSTORE_NAME="kvstore";  
    public static  String KVSTORE_URL="localhost:5000";

    private static KVStore kvStore = null;

   
    public static KVStore getKVStore() {
        if (kvStore == null) {
            try {

                kvStore =
                        KVStoreFactory.getStore(new KVStoreConfig(KVSTORE_NAME, KVSTORE_URL));

            } catch (Exception e) {
                System.err.println("ERROR: Please make sure Oracle NoSQL Database is up and running at '" +
                                   KVSTORE_URL + "' with store name as: '" + KVSTORE_NAME +
                                   "'");
                e.printStackTrace();
            }
        } 

        return kvStore;
    } 

    private static void usage(){
        System.out.println("MultiDeleteData kvstore kvhost:kvport parentkey[durability [startsubrange endsubrange]]");
        System.out.println(" defaults: COMMIT_NO_SYNC startsubrange:0 endsubrange:0");
        System.out.println(" durability values COMMIT_NO_SYNC COMMIT_SYNC COMMIT_WRITE_NO_SYNC");
        
        System.out.println( "Example for Key Space: ");
        System.out.println( "/home/family/fred/100 ");
        System.out.println( "/home/family/fred/200 ");
        System.out.println( "/home/family/fred/300 ");
        System.out.println( "/home/family/fred/400 ");
        System.out.println( "/home/family/sally/400 ");
        System.out.println( "Program arguments:   storeName  host:port  /home/family/fred  COMMIT_NO_SYNC  200  300 ");
        System.out.println( "The above would delete all keys and values under /home/family/fred between range 200 and 300 inclusive");
        System.out.println();
        System.out.println( "Example program arguments:   storeName  host:port  /home/family");
        System.out.println( "The above would delete all keys and values under /home/family  , including all /fred and /sally");
        
    }
    
  public static void main(String[] args) throws IOException {

      long begtime = System.currentTimeMillis();
      String parentkey = null;
      String begrange = null;
      String endrange = null;
      String durabilityStr = null;
      Durability durability = Durability.COMMIT_NO_SYNC;

      if (args.length>=3){
          System.out.println("args:"+Arrays.asList(args).toString());
          KVSTORE_NAME=args[0]; 
          KVSTORE_URL=args[1]; 
          parentkey = args[2];
            if (args.length >= 4) {
                durabilityStr = args[3];
                if(!durabilityStr.equals("COMMIT_NO_SYNC")&&!durabilityStr.equals("COMMIT_SYNC")&&!durabilityStr.equals("COMMIT_WRITE_NO_SYNC")){
                    usage();
                    return;
                }
                durability = durabilityStr.equals("COMMIT_NO_SYNC")?Durability.COMMIT_NO_SYNC:durability;
                durability = durabilityStr.equals("COMMIT_WRITE_NO_SYNC")?Durability.COMMIT_WRITE_NO_SYNC:durability;
                durability = durabilityStr.equals("COMMIT_SYNC")?Durability.COMMIT_SYNC:durability;
            } 
            if (args.length == 6) {
                begrange = args[4];
                endrange = args[5];                
            } 

            } else {
          usage();
          return;
      }

      Key key = Key.createKey( getKeyFromPathString(parentkey) );
      KeyRange kr = null;
    
 
      int res = 0;
      try {
	      if(begrange != null){
	          kr = new KeyRange(begrange,true,endrange,true);
	      }
	      else {
	          kr = null; 
	      }
	      System.out.println("arg values:"+Arrays.asList(KVSTORE_NAME,KVSTORE_URL,key.toString(),durabilityStr, (kr==null)?"":kr.getStart(),(kr==null)?"":kr.getEnd()).toString());
      
	      boolean enditerate = false;  
      
	      //NOTE - move the following line of code inside the while loop, if you want to run this with multiple threads/processes - treats the iterator like a queue.
	      Iterator<Key> iter = getKVStore().storeKeysIterator(Direction.UNORDERED, 0, key, kr, Depth.DESCENDANTS_ONLY);

          while(! enditerate){
              if(iter.hasNext()){
                Key iterkey = iter.next();
                iterkey = Key.createKey(iterkey.getMajorPath());
                int delNb = getKVStore(). multiDelete(iterkey, kr, Depth.PARENT_AND_DESCENDANTS, durability, 0, null) ;   
                res += delNb;
              }
              else {
                  enditerate = true;
              }
          }
          
      }catch(Exception e){
          System.err.println("Error:"+e.getMessage());
          e.printStackTrace();
            usage();   
             return;
      }
       getKVStore().close();    
      long endtime = System.currentTimeMillis();
      System.out.println("elapsed :"+((endtime-begtime)/1000)+ " seconds for "+res+" deletes");

  }
  
  private static ArrayList<String>  getKeyFromPathString( String path ){
  	
  	ArrayList<String>  components = new ArrayList<String>();
  	
  	String[] pieces = path.split("/", -1);
  	for( String s : pieces){
  		if( !s.isEmpty() )
  			components.add(s);
  	}
  	return components;
  }
}
