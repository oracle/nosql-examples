/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2016 Oracle and/or its affiliates.  All rights reserved.
 *
 *  Oracle NoSQL Database is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation, version 3.
 *
 *  Oracle NoSQL Database is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License in the LICENSE file along with Oracle NoSQL Database. If not,
 *  see <http://www.gnu.org/licenses/>.
 *
 *  An active Oracle commercial licensing agreement for this product
 *  supercedes this license.
 *
 *  For more information please contact:
 *
 *  Vice President Legal, Development
 *  Oracle America, Inc.
 *  5OP-10
 *  500 Oracle Parkway
 *  Redwood Shores, CA 94065
 *
 *  or
 *
 *  berkeleydb-info_us@oracle.com
 *
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  EOF
 *
 */ 

package com.oracle.fleet.dao;

import java.util.Iterator;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
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

	/**
	 * Each DAO class need to define how to create the table
	 */
	public abstract void createTable();

	public static void main(String[] args) {
		System.out.println("Testing connection to local NoSQL Database...");
		BaseDAO.getKVStore();
		System.out.println("userTable: " + BaseDAO.getTable("Fleet"));

		// getOraConnect();

	}

}
