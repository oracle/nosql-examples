package com.oracle.email.dao;

import java.util.Iterator;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.table.FieldRange;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;

public abstract class BaseDAO {

	private static KVStore kvStore = null;
	private static TableAPI tableImpl = null;
	private static String hostPort = "localhost:5000";
	private static String storeName = "kvstore";

	public BaseDAO() {
		super();

	} // BaseDAO

	public static KVStore getKVStore() {
		if (kvStore == null) {
			try {
				System.out
						.println("================================================================================");

				

				kvStore = KVStoreFactory.getStore(new KVStoreConfig(storeName,
						hostPort));

				System.out
						.println("  Successfully Connected to Oracle NoSQL DB @ "
								+ hostPort + "/" + storeName);

			} catch (Exception e) {
				System.out
						.println("ERROR: Please make sure Oracle NoSQL Database instance is up and running. ");
				System.out
						.println("Action: Either start a NoSQL instance at '"
								+ hostPort
								+ "' with store-name: '"
								+ storeName
								+ "' or run client by passing '<hostname:port> <storename>' at the command line.");
				System.exit(1);

			}
			System.out
					.println("================================================================================");
		} // EOF if(kvstore==null)

		return kvStore;
	} // getKVStore

	public static String getHostPort() {
		return hostPort;
	}

	public static void setHostPort(String hostPort) {
		BaseDAO.hostPort = hostPort;
	}

	public static String getStoreName() {
		return storeName;
	}

	public static void setStoreName(String storeName) {
		BaseDAO.storeName = storeName;
	}

	public static Table getTable(String tablePath) {
		Table table = null;
		try {
			tableImpl = getTableAPI();
			table = tableImpl.getTable(tablePath);
		} catch (Exception e) {
			System.err.println("Failed to get table: " + tablePath);
			// e.printStackTrace();
		}
		return table;
	}

	public static Iterator<Row> getTableRows(Table table) {

		MultiRowOptions mro = new MultiRowOptions(null, null, null);
		return tableImpl.tableIterator(table.createPrimaryKey(), mro, null);
	}

	public static TableAPI getTableAPI() {
		if (tableImpl == null) {
			tableImpl = getKVStore().getTableAPI();
		}
		return tableImpl;
	}
	
	public static FieldRange getFieldRange(Table table){
		
		return table.createFieldRange(table.getPrimaryKey().get(0));
		
	}
	

	public static void main(String[] args) {
		System.out.println("Testing connection to local NoSQL Database...");
		BaseDAO.getKVStore();
		System.out.println("userTable: " + BaseDAO.getTable("User"));

		// getOraConnect();

	}

	/**
	 * Each DAO class need to define how to create the table
	 */
	public abstract void createTable();

}
