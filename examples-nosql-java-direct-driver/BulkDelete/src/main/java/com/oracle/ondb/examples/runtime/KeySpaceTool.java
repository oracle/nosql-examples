/**
 * 
 */
package com.oracle.ondb.examples.runtime;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.oracle.ondb.examples.utility.CommIO;

import oracle.kv.Direction;
import oracle.kv.Durability;
import oracle.kv.DurabilityException;
import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.RequestTimeoutException;
import oracle.kv.ReturnValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;


/**
 * @author rcgreene
 *
 */
	
	public class KeySpaceTool {

	    private final KVStore store;
		static String STORE_NAME = "kvstore";
		static String HOST_NAME = "localhost";
		static String HOST_PORT = "5000";
		static ArrayList<Key> keyCatalog;
		CommIO io = new CommIO();
		

		private String response = "true";


	    public static void main(String args[]) {
	        try {
	            KeySpaceTool example = new KeySpaceTool(args);
	            example.runExample();
	        } catch (RuntimeException e) {
	            e.printStackTrace();
	        }
	    }

	    /**
	     * Parses command line args and opens the KVStore.
	     */
	    KeySpaceTool(String[] argv) {
	        
	        KeySpaceTool.keyCatalog = new ArrayList<Key>();

	        final int nArgs = argv.length;
	        int argc = 0;

	        while (argc < nArgs) {
	            final String thisArg = argv[argc++];

	            if (thisArg.equals("-store")) {
	                if (argc < nArgs) {
	                    STORE_NAME = argv[argc++];
	                } else {
	                    usage("-store requires an argument");
	                }
	            } else if (thisArg.equals("-host")) {
	                if (argc < nArgs) {
	                    HOST_NAME = argv[argc++];
	                } else {
	                    usage("-host requires an argument");
	                }
	            } else if (thisArg.equals("-port")) {
	                if (argc < nArgs) {
	                    HOST_PORT = argv[argc++];
	                } else {
	                    usage("-port requires an argument");
	                }
	            } else {
	                usage("Unknown argument: " + thisArg);
	            }
	        }

	        String[] hosts =  { HOST_NAME + ":" + HOST_PORT };  //Normally you would specify more than 1 
	        store = KVStoreFactory.getStore(new KVStoreConfig(  STORE_NAME ,  hosts ));
	        initialize( );
	    }
	    
	    void initialize(  ){
	    	
	    	Iterator<Key> keys = store.storeKeysIterator(Direction.UNORDERED, 0);
	    	
	    	while( keys.hasNext() ){
	    		KeySpaceTool.keyCatalog.add(keys.next());
	    	}
	    	
	    }

	    private void usage(String message) {
	        System.out.println("\n" + message + "\n");
	        System.out.println("usage: " + getClass().getName());
	        System.out.println("\t-store <instance name> (default: "+ KeySpaceTool.STORE_NAME + ")" +
	                           "-host <host name> (default: " + KeySpaceTool.HOST_NAME + ")" +
	                           "-port <port number> (default: " + KeySpaceTool.HOST_PORT + ")" );
	        System.exit(1);
	    }

		
		void runExample( ){
		
			try {

			printMenu();

			while (response.equals("true")) {
				printRequest();
				switch (CommIO.c) {
				case 0:
					printMenu();
					break;
				case 1:
					createKey( );
					break;
				case 2:
					showKeys( );
					break;
				case 3:
					createValueWithExistingKey( );
					break;
				case 4:
					createValueWithNewKey( );
					break;
				case 5:
					populateExistingKeySpace( );
					break;
				case 6:
					populateSampleKeySpace( );
					break;
				case 7:
					showKeysAndValues( );
					break;
				case 8:
					deleteAllKeysAndValues( );
					break;
				case 9:
					response = "false";
					break;
				default:
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	
		private void populateExistingKeySpace( ){
			
			String key = io.getUserInput("What is the key?");
			Key created = Key.createKey(getKeyFromPathString(key));
			if( !checkKeyExists(created) ){
				System.out.println( "Key does not exist");
				return;
			}
			
			int value = io.getInt("How many values do you want in your distribution?");
			
			int mid = value/2;
			int stddev = 1;
			Random r = new Random();
			double sigma = r.nextGaussian();
			
			for( int i=0; i<value; i++ ){
				byte[] extendedKey = Key.addComponent(created.toByteArray(), false, String.valueOf(i) );
				Key extended = Key.fromByteArray(extendedKey);
				KeySpaceTool.keyCatalog.add(extended);
				store.put(extended, Value.fromByteArray(ByteBuffer.allocate(4).putInt((int) (mid + stddev * sigma)).array() ));
			}
			
			
		}
		
		private void populateSampleKeySpace( ){
			
			System.out.println( "Populating keyspace......");

			String[] paths = {"/home/family/fred","/home/family/melissa","/home/family/lilia","/home/family/sam"};
			Key[] keys = new Key[4];
			for( int i=0; i<4; i++ ){
				Key created = Key.createKey(getKeyFromPathString(paths[i]));
				
				if(  !checkKeyExists(created)  ){
					store.put(created, Value.EMPTY_VALUE);
					KeySpaceTool.keyCatalog.add(created);
				}else{
					System.out.println( "Key already exists, can only populate sample keyspace once, use Delete all system keys and values before running again.");
					return;
				}
				keys[i] = created;
			}
			
			int value = 10000;
			
			for( Key k : keys ){
			
				int mid = value/2;
				int stddev = 1;
				Random r = new Random();
				double sigma = r.nextGaussian();
				
				for( int i=0; i<value; i++ ){
					byte[] extendedKey = Key.addComponent(k.toByteArray(), false, String.valueOf(i) );
					Key extended = Key.fromByteArray(extendedKey);
					KeySpaceTool.keyCatalog.add(extended);
					store.put(extended, Value.fromByteArray(ByteBuffer.allocate(4).putInt((int) (mid + stddev * sigma)).array() ));
				}
			}
			
			System.out.println("Populated a keyspace with 4 major paths, 10000 sub keys each");
			System.out.println("/home/family/fred" + ", " + "/home/family/melissa" + ", " + "/home/family/lilia" + ", " + "/home/family/sam");
			System.out.println("Use menu option 2 to see the keyspace");
		}

		private void createKey(  ){
			 	
			String key = io.getUserInput("Enter the key you want to create:  ");

			Key created = Key.createKey(getKeyFromPathString(key));
			if(  !checkKeyExists(created)  ){
				store.put(created, Value.EMPTY_VALUE);
				KeySpaceTool.keyCatalog.add(created);
			}else{
				System.out.println( "Key already exists");
			}
			
		}
		
		private boolean checkKeyExists(  Key key  ){
			
			if( KeySpaceTool.keyCatalog.contains(key)) {
				return true;
			}
			return false;
			
		}
		
		private void createValueWithExistingKey(  ){
		 	
			String key = io.getUserInput("Enter the existing key ");

			Key created = Key.createKey(getKeyFromPathString(key));
			if( !checkKeyExists(created) ) {
				io.say("Key does not  exists, use showKeys menu option to see all keys");
				return;
			}		
			String value = io.getUserInput("Enter the Value for the key ");
			store.put(created, Value.createValue(value.getBytes()));
			
		}
		
		private void createValueWithNewKey(  ){
		 	
			String key = io.getUserInput("Enter the key you want to create:  ");
			

			Key created = Key.createKey(getKeyFromPathString(key));

			if( checkKeyExists(created)) {
				io.say("Key already exists");
				return;
			}
			
			String value = io.getUserInput("Enter the Value for the key ");
			store.put(created, Value.createValue(value.getBytes() ));
			KeySpaceTool.keyCatalog.add(created);
			
		}
		
		private void showKeys( ){
			Iterator<Key>  allKeys = KeySpaceTool.keyCatalog.iterator();
			System.out.println( "ALL SYSTEM KEYS: ");
			while( allKeys.hasNext() ){
				Iterator<String> components = allKeys.next().getFullPath().iterator();
				while( components.hasNext()){
					System.out.print(  "/" + components.next() );
				}
				System.out.println( "" );
			}
		}
		
		private void showKeysAndValues( ){
			
	    	
	    	Iterator<KeyValueVersion> keys = store.storeIterator(Direction.UNORDERED, 0);
	    	
	    	Key kNow;
	    	Value vNow;
	    	
	    	while( keys.hasNext() ){
	    		KeyValueVersion ver = keys.next();
	    		kNow = ver.getKey();
	    		vNow = ver.getValue();
	    		
	    		Iterator<String> components = kNow.getFullPath().iterator();
				while( components.hasNext()){
					System.out.print( "/" + components.next() );
				}
				System.out.print( "==>" );
				System.out.println( vNow.toString() );
	    	}
		}
		
		private void deleteAllKeysAndValues( ){
			
			
		      boolean enditerate = false;
		      int res = 0;
		      
		      //NOTE - move the following line of code inside the while loop, if you want to run this with multiple threads/processes - treats the iterator like a queue.
		      Iterator<Key> iter = store.storeKeysIterator(Direction.UNORDERED, 0 );

	          while(! enditerate){
	              if(iter.hasNext()){
	                Key iterkey = iter.next();
	                iterkey = Key.createKey(iterkey.getMajorPath());
	                int delNb = store. multiDelete(iterkey, null, null) ;   
	                res += delNb;
	              }
	              else {
	                  enditerate = true;
	              }
	          }
			
			/**
	    	
	    	Iterator<KeyValueVersion> keys = store.storeIterator(Direction.UNORDERED, 0);
	    	
	    	Key kNow;
	    	int count = 0;
	    	int total = 0;
	    	
	    	while( keys.hasNext() ){
	    		KeyValueVersion ver = keys.next();
	    		kNow = ver.getKey();
	    		count = store.multiDelete(kNow, null, null);
	    		total += count;
	    	}  */
	    	System.out.println( "Total key-values deleted: " + res );
	    	System.out.println( "Application now exiting to force cache invalidation" );
	    	System.exit(0);  
		}
	
	
	   /**
     * Perform Update operation. 
     */
    void updateExample( Key psKey, int newReading ) {

    	boolean retry = true;
    	int count = 5;
        final byte[] measure = ByteBuffer.allocate(4).putInt(newReading).array();  
        final Value newValue = Value.createValue(measure);
        final Version matchVersion = store.get(psKey).getVersion();
        
        Durability durability =
        		new Durability(Durability.SyncPolicy.SYNC, // Master sync
        		Durability.SyncPolicy.NO_SYNC, // Replica sync
        		Durability.ReplicaAckPolicy.SIMPLE_MAJORITY);
        
        ReturnValueVersion prevValue = new ReturnValueVersion(
        												ReturnValueVersion.Choice.VERSION);
        retry = true;
        while( retry && count > 0){
        	retry = false;
        	try{
        		store.putIfVersion( psKey, newValue, matchVersion, prevValue, 
        								durability, 1000, TimeUnit.MILLISECONDS);
        	}catch( DurabilityException de ){
        		//Lets Discuss Durability  ...... 
        	}catch( RequestTimeoutException re ){
        		//Lets Discuss Timeout
        	}catch( FaultException fe ){
        		//Lets Discuss Faults
        	}
        }
    }
    
    /**
     * Perform PUT and Get operation. 
     */
    void putExample( String sector, String id, int reading ) {

    	ArrayList<String> keyHierarchy = new ArrayList<String>();
    	keyHierarchy.add("PS");
    	keyHierarchy.add(sector);
    	keyHierarchy.add(id);

        final byte[] measure = ByteBuffer.allocate(4).putInt(reading).array();
        
        final Key k = Key.createKey(keyHierarchy);
        final Value v = Value.createValue(measure);

        store.put(k,v);

        final ValueVersion valueVersion = store.get(k);

        System.out.println(keyHierarchy + " " + new String(valueVersion.getValue().getValue()));
    }
    
    private ArrayList<String>  getKeyFromPathString( String path ){
    	
    	ArrayList<String>  components = new ArrayList<String>();
    	
    	String[] pieces = path.split("/", -1);
    	for( String s : pieces){
    		if( !s.isEmpty() )
    			components.add(s);
    	}
    	return components;
    }
    
    
	public void printMenu() {

		// Note - if you add to this menu you must change the range in <printRequest()>

		System.out.println("0 - Print Menu");
		System.out.println("1 - Create a new Key");
		System.out.println("2 - Show all sytem Keys");
		System.out.println("3 - Create a value with Existing Key");
		System.out.println("4 - Create a value with New Key");
		System.out.println("5 - Populate an existing keyspace");
		System.out.println("6 - Populate a sample keyspace");
		System.out.println("7 - Show all sytem Keys and values");
		System.out.println("8 - Delete all sytem Keys and values");
		System.out.println("9 - Quit");
		System.out.println();

	}

	public void printRequest() {

		// Need to change this range if you add or remove menu items from printMenu method
		int range = 9;
		try {
			io.getInt("Make a selection - 0 for menu of choices");
		} catch (NumberFormatException n) {
			System.out.println("Invalid selection - pick a number!");
			printRequest();
		}
		if (CommIO.c > range) {
			System.out
					.println("Invalid selection - pick a number between 0 and "
							+ range);
			printRequest();
		}
	}
	


}
