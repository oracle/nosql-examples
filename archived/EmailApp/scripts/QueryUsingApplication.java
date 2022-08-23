import java.util.Arrays;

import oracle.kv.Consistency;
import oracle.kv.Direction;
import oracle.kv.Durability;
import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.table.FieldRange;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;
import oracle.kv.table.TableIterator;
import oracle.kv.table.TableIteratorOptions;



/**
 * @author oracle Sample Class that demonstrates how to query nosql tables using
 *         JAVA API
 */
public class QueryUsingApplication {

	public static void main(String[] argv) throws Exception {

		QueryUsingApplication queryApp = new QueryUsingApplication();

		queryApp.getUsersInRange("UO1", "UO2");
		queryApp.getUserByEmail("foo.bar@email.com");
		queryApp.getUsersFolder("UO1");
		queryApp.getUsersFolderMessages("UO1", "Inbox");

	}

	private Table userTable = null;
	private Table nestedUserFolderTable = null;
	private Table nestedUserFolderMessageTable = null;
	public static String host = "localhost";
	public static String port = "5000";
	public static String store = "kvstore";
	private KVStore kvs;

	private TableAPI tableImpl;

	/**
	 * Constructor for QueryUsingApplication Establishes a connection with the
	 * KVStore
	 */
	public QueryUsingApplication() {

		try {
			KVStoreConfig config = new KVStoreConfig(store, host + ":" + port);
			config.setConsistency(Consistency.ABSOLUTE);
			config.setDurability(new Durability(Durability.SyncPolicy.SYNC,
					Durability.SyncPolicy.SYNC, Durability.ReplicaAckPolicy.ALL));
			kvs = KVStoreFactory.getStore(config);
		} catch (FaultException ex) {
			if (ex.toString().indexOf("Could not contact any RepNode") != -1) {
				System.err.println("Can not open connection to "
						+ " Oracle NoSQL store [" + store + "] at " + host
						+ ':' + host + ":" + port
						+ ".\nPlease make sure a store is running.");

			}
			System.err.println("Error : " + ex.toString());
			throw ex;

		}

	}

	/**
	 * This method returns User's profile for email argument passed
	 * 
	 * @param email
	 *            - email of the user
	 * @return UserTO with everything about the user
	 */
	public void getUserByEmail(String email) {

		Row row = null;

		Index index = null;
		IndexKey indexKey = null;
		TableIterator<Row> iter = null;

		System.out
				.println("************ Get User with email id  foo.bar@email.com ************");
		System.out.println();

		try {
			tableImpl = kvs.getTableAPI();

			userTable = tableImpl.getTable("Users");
			tableImpl = kvs.getTableAPI();

			// System.out.println("Search user by email: " + email);

			if (StringUtil.isNotEmpty(email)) {

				// lower case the email
				email = email.trim().toLowerCase();

				/* get the index */
				index = userTable.getIndex("emailIndex");

				/* index is on email & password field */
				indexKey = index.createIndexKey();
				// Add only Email
				indexKey.put("email", email);

				// get all the rows that have same email and password

				iter = tableImpl.tableIterator(indexKey, null, null);
				while (iter.hasNext()) {
					row = iter.next();
					System.out.println(row.getTable().getName() + ": ");
					System.out.println(row.toJsonString(false));
					System.out.println();

				}
			}
		} finally {

			if (iter != null)
				iter.close();
		}

	} // getUserByEmail

	/**
	 * Method to return all the folder for a given User
	 * 
	 * @param userID
	 */

	public void getUsersFolder(String userID) {

		System.out
				.println("************ Get all the folders for the User UO1 ************");
		System.out.println();

		TableIterator<Row> iter = null;

		try {
			tableImpl = kvs.getTableAPI();

			userTable = tableImpl.getTable("users");
			nestedUserFolderTable = tableImpl.getTable("users.folder");

			// Specify the primary key
			PrimaryKey pk = nestedUserFolderTable.createPrimaryKey();
			pk.put("userId", userID);

			// Define MultiRowOptions to retrieve child table
			MultiRowOptions mro = new MultiRowOptions(null,
					Arrays.asList(userTable), null);

			iter = tableImpl.tableIterator(pk, mro, null);

			while (iter.hasNext()) {
				Row row = iter.next();

				System.out.println(row.toJsonString(false));
				System.out.println();
			}
		} finally {
			if (iter != null)
				iter.close();
		}

	}

	/**
	 * method to return the all the messages for the given user in a given
	 * folder
	 * 
	 * @param userID
	 * @param folderID
	 */

	public void getUsersFolderMessages(String userID, String folderName) {

		Table messageTable = null;
		TableIterator<Row> iter = null;

		System.out
				.println("************ Get all the Messages for the User UO1 in the Folder FO1 ************");
		System.out.println();
		try {
			tableImpl = kvs.getTableAPI();

			userTable = tableImpl.getTable("users");
			nestedUserFolderTable = tableImpl.getTable("users.folder");
			nestedUserFolderMessageTable = tableImpl
					.getTable("users.folder.message");
			messageTable = tableImpl.getTable("message");

			// Specify the primary key
			PrimaryKey pk = nestedUserFolderMessageTable.createPrimaryKey();
			pk.put("userId", userID);
			pk.put("name", folderName);

			// Define MultiRowOptions to retrieve child table
			MultiRowOptions mro = new MultiRowOptions(null,
					Arrays.asList(userTable), null);

			iter = tableImpl.tableIterator(pk, mro, null);

			while (iter.hasNext()) {
				Row row = iter.next();

				System.out.println(row.toJsonString(false));

				PrimaryKey msgpk = messageTable.createPrimaryKey();
				msgpk.put("messageId", row.get(0).toString());

				TableIteratorOptions tio = new TableIteratorOptions(
						Direction.FORWARD, null, 0, null);
				TableIterator<Row> msgiter = tableImpl.tableIterator(msgpk,
						null, tio);
				while (msgiter.hasNext()) {
					Row msgrow = msgiter.next();
					System.out.println(msgrow.toJsonString(false));
				}

				System.out.println();

			}
		} finally {

			if (iter != null)
				iter.close();
		}
	}

	/**
	 * Method to search for users within the range of their IDs
	 * 
	 * @param startUID
	 * @param endUID
	 */

	public void getUsersInRange(String startUID, String endUID) {

		System.out
				.println("************ Get all the Users with IDs beginning with UO1 ************");
		System.out.println();
		TableIterator<Row> iter = null;
		try {
			tableImpl = kvs.getTableAPI();

			userTable = tableImpl.getTable("users");

			// Specify FieldRange and value

			FieldRange fh = userTable.createFieldRange("userId");

			fh.setStart(startUID, true);
			fh.setEnd(endUID, true);
			MultiRowOptions mro = fh.createMultiRowOptions();

			iter = tableImpl.tableIterator(userTable.createPrimaryKey(), mro,
					null);

			while (iter.hasNext()) {
				Row row = iter.next();
				System.out.println(row.getTable().getName() + ": ");
				System.out.println(row.toJsonString(false));
				System.out.println();
			}
		} finally {
			iter.close();
		}

	}
}