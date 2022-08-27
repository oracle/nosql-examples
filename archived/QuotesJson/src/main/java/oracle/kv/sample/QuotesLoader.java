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
 *  License in the LICENSE file along with Oracle NoSQL Database.  If not,
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
package oracle.kv.sample;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import oracle.kv.Consistency;
import oracle.kv.Direction;
import oracle.kv.StatementResult;
import oracle.kv.query.PreparedStatement;
import oracle.kv.sample.common.BaseLoader;
import oracle.kv.sample.common.util.FileUtil;
import oracle.kv.sample.loader.ChildTableLoader;
import oracle.kv.sample.loader.TableLoader;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.RecordValue;
import oracle.kv.table.Row;
import oracle.kv.table.TableIterator;
import oracle.kv.table.TableIteratorOptions;

/**
 * The Main Loader Class. This class will load all the data from the TXT file
 * passed as argument into a table specified in the config.properties
 * 
 * @author vsettipa
 * 
 */
public class QuotesLoader extends BaseLoader {
    
    private TableLoader tableLoader;
    private ChildTableLoader childTableLoader;
    
    /**
     * Constructor
     * 
     * @param args
     */
    public QuotesLoader(String[] args) {
	super(args);
    }
    
    /**
     * This method performs validation on the input arguments. If this method
     * fails then program exits.
     * 
     * @return true if there is no validation errors otherwise false.
     */
    public boolean validate() {
	boolean flag = true;
	
	// arguments that are required.
	if (inputPathStr != null) {
	    if (!FileUtil.isValidPath(inputPathStr)) {
		flag = false;
		logError("Input directory argument: '-i' can not be empty.");
	    }
	}
	return flag;
    }
    
    /**
     * After successful validation this method is run to load the content of a
     * TXT file into the given table
     * 
     * @throws IOException
     */
    private void loadData() throws IOException {
	if (inputPathStr != null) {
	    File dir = new File(inputPathStr);
	    File file = null;
	    File[] files = null;
	    int len = 0;
	    
	    // If input path is a directory then load data from all the files
	    // that are under the directory. Make sure all the files are of same
	    // type
	    // i.e. TXT (mix and match is not allowed)
	    if (dir.exists()) {
		if (dir.isDirectory()) {
		    System.out.println("Reading multiple files to load");
		    files = dir.listFiles();
		    len = files.length;
		    
		    // loop through all the files and load the content one by
		    // one
		    for (int i = 0; i < len; i++) {
			file = files[i];
			tableLoader = new TableLoader(idNumber,columns, noOfStreams,
				noOfRecords, tableh, table);
			tableLoader.loadFileToTable(file);
			childTableLoader = new ChildTableLoader(idNumber,cidNumber,childColumns,
				noOfStreams, noOfRecords, tableh, childTable);
			childTableLoader.loadFileToTable(file);
		    } // EOF for
		} else {
		    System.out.println("Reading single file to load");
		    file = dir;
		    tableLoader = new TableLoader(idNumber,columns, noOfStreams,
			    noOfRecords, tableh, table);
		    tableLoader.loadFileToTable(file);
		    childTableLoader = new ChildTableLoader(idNumber,cidNumber,childColumns,
			    noOfStreams, noOfRecords, tableh, childTable);
		    childTableLoader.loadFileToTable(file);
		}
	    } else {
		System.out.println("There are no Files to Load");
	    }
	}
	
	if (id != null) {
	    getData();
	}
    }
    
    /**
     * this method will be called if there is a bulk get call
     */
    private void getData() {
	
	System.out.println("Get Customer Data");
	Date date = new Date();
	final int maxConcurrentRequests = 9;
	final int batchResultsSize = 0;
	// Direction - UNORDERED - Iterate in no particular key order.
	// CONSISTENCY - NONE_REQUIRED - A consistency policy that lets a
	// transaction on a replica using this policy proceed regardless of the
	// state of the Replica relative to the Master.
	final TableIteratorOptions tio = new TableIteratorOptions(
		Direction.UNORDERED, Consistency.NONE_REQUIRED, 0, null,
		maxConcurrentRequests, batchResultsSize);
	
	// Create Primary key with the value passed as argument
	PrimaryKey myKey = table.createPrimaryKey();
	
	myKey.put("id", new Long(id).longValue());
	
	// Add Child table to multi row options so that all rows related to the
	// given parent table is also retrieved.
	MultiRowOptions mro = new MultiRowOptions(null, null,
		Arrays.asList(childTable));
	
	// Create the table iterator and pass te primary Key, Muti Row Options
	// and Table Iterator options.
	System.out.println(date.toString());
	final TableIterator<Row> iterator = tableh.tableIterator(myKey, mro,
		tio);
	System.out.println(date.toString());
	// Now retrieve the records.
	try {
	    while (iterator.hasNext()) {
		Row row = (Row) iterator.next();
		System.out.println("Row Retrieved :" + row.toJsonString(true));
		System.out.println(date.toString());
	    }
	} finally {
	    // iterator close
	    if (iterator != null) {
		iterator.close();
	    }
	}
	
	// // Compile the statement.
	PreparedStatement pStmt = kvStore.prepare(
		// "DECLARE $minAge integer; $maxAge integer; " +
		"SELECT id, email,phone,age FROM customer WHERE "
			+ "age >= 30 and age < 40 ");
	//
	// // Execute the statement
	StatementResult result = kvStore.executeSync(pStmt);
	//
	// Get the results in the current decade
	for (RecordValue record : result) {
	    System.out.println("id: " + record.get("id").asLong().get());
	    System.out
		    .println("email: " + record.get("email").asString().get());
	    System.out
		    .println("phone: " + record.get("phone").asString().get());
	    System.out.println("age: " + record.get("age").asInteger().get());
	}
	// System.out.println("Query Result :" + result.getResult());
	// System.out.println(date.toString());
	PreparedStatement ctStmt = kvStore
		.prepare("SELECT q.qid, q.quotes FROM customer.quotes q");
	
	// Execute the statement
	StatementResult ctresult = kvStore.executeSync(ctStmt);
	for (RecordValue record1 : ctresult) {
	    System.out.println("qid: " + record1.get("qid").asLong().get());
	    System.out.println(
		    "quotes: " + record1.get("quotes").asMap().toString());
	}
	// System.out.println(ctresult.getResult());
    }
    
    public static void main(String[] args) {
	QuotesLoader loader = new QuotesLoader(args);
	try {
	    loader.loadData();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}