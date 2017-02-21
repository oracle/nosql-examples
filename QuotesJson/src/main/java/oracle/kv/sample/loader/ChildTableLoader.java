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
package oracle.kv.sample.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import oracle.kv.BulkWriteOptions;
import oracle.kv.Durability;
import oracle.kv.DurabilityException;
import oracle.kv.EntryStream;
import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.KeyValue;
import oracle.kv.RequestTimeoutException;
import oracle.kv.sample.QuotesReader;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;

/**
 * Child Table Loader which reads from TXT, creates the streams for Bulk Put and
 * loads into table.
 * 
 * @author vsettipa
 * 
 */
public class ChildTableLoader {
    
    private static QuotesReader csvReader;
    protected static String columns = "";
    protected static String noOfRecords = "";
    protected static String noOfStreams = "";
    protected static KVStore kvStore;
    protected static TableAPI tableh;
    protected static Table childTable;
    protected static AtomicLong cidNumber;
    protected static AtomicLong idNumber;
    
    public ChildTableLoader(AtomicLong cidNumber, AtomicLong idNumber, String columns2, String noOfStreams2,
	    String noOfRecords2, TableAPI tableh2, Table table2) {
	this.columns = columns2;
	this.noOfRecords = noOfRecords2;
	this.noOfStreams = noOfStreams2;
	this.tableh = tableh2;
	this.childTable = table2;
	this.cidNumber = cidNumber;
	this.idNumber = idNumber;
    }
    
    /**
     * This method takes File object as an argument and loop through each line
     * and Bulk Load the content into a table in the kv-store
     * 
     * @param file
     *            with data that is TXT type
     */
    public void loadFileToTable(File file) throws IOException {
	List<EntryStream<Row>> streamList = null;
	Durability durability = null;
	BulkWriteOptions bulkWriteOptions = null;
	BufferedReader br = null;
	int nStreams = Integer.parseInt(noOfStreams);
	int nRecords = Integer.parseInt(noOfRecords);
	BlockingQueue<String>[] queueList = new BlockingQueue[nStreams];
	File csvFile = file;
	
	// check for file permission
	if (!csvFile.canRead()) {
	    System.out.println("File does not READ permissions");
	    System.exit(0);
	}
	// create a blocking queue of the number of records in each queue to be
	// passed to a Bulk Load Stream
	
	for (int i = 0; i < nStreams; i++) {
	    queueList[i] = new ArrayBlockingQueue<String>(nRecords);
	}
	
	// durability options are set below
	// Defines the synchronization policy to be used when committing a
	// transaction.
	// param1 = Sync policy for Master - SYNC - Write and synchronously
	// flush the log on trasaction commit.
	// param2 = Sync policy for Replica - SYNC - Write and synchronously
	// flush the log on trasaction commit.
	// param1 = Replica Acknowledge policy for Master - SIMPLE_MAJORITY
	durability = new Durability(Durability.SyncPolicy.SYNC,
		Durability.SyncPolicy.SYNC,
		Durability.ReplicaAckPolicy.SIMPLE_MAJORITY);
	
	// create the bulk write options
	bulkWriteOptions = new BulkWriteOptions(durability, 0, null);
	
	// start a thread to read all lines from CSV and load into Blocking
	// Queue.
	try {
	    br = new BufferedReader(
		    new InputStreamReader(new FileInputStream(csvFile)));
	    
	    streamList = new ArrayList<EntryStream<Row>>(nStreams);
	    csvReader = new QuotesReader(br, queueList);
	    new Thread(csvReader).start();
	    
	    for (int i = 0; i < nStreams; i++) {
		// Create each stream with nRecords to be passed to Bulk Put
		streamList.add(new BulkLoadDataStreamToChildTable(idNumber,cidNumber,queueList[i],
			i, childTable));
			cidNumber.incrementAndGet();
			idNumber.incrementAndGet();
	    }
	    try {
		// Bulk Put data in all the streams
		tableh.put(streamList, bulkWriteOptions);
	    } catch (Throwable t) {
		t.printStackTrace();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new RuntimeException();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
    
    /**
     * Inner Class implementation of the EntryStream<Row> class the getNext
     * Method creates each table Row which will be passed to Bulk Put
     * 
     * @author vsettipa
     * 
     */
    class BulkLoadDataStreamToChildTable implements EntryStream<Row> {
	private BlockingQueue<String> dataQueue;
	private int streamNo;
	private AtomicLong rowProcessedCount;
	private AtomicLong keyExistsCount;
	private Table childTable;
	private AtomicLong cidNumber;
	private AtomicLong idNumber;
	
	public BulkLoadDataStreamToChildTable(AtomicLong cidNumber,AtomicLong idNumber,BlockingQueue<String> queue,
		int streamNo, Table table) {
	    this.dataQueue = queue;
	    this.rowProcessedCount = new AtomicLong();
	    this.keyExistsCount = new AtomicLong();
	    this.childTable = table;
	    this.cidNumber = cidNumber;
	    this.idNumber = idNumber;
	}
	
	public KeyValue entryToString(KeyValue keyValue) {
	    return keyValue;
	}
	
	public void catchException(RuntimeException exception, Row row) {
	    System.err.println(name() + " catch exception: "
		    + exception.getMessage() + " for row: " + row);
	}
	
	public void completed() {
	    System.err.println("Completed loading for table stream: " + streamNo
		    + ":" + name());
	}
	
	public void keyExists(Row row) {
	    keyExistsCount.incrementAndGet();
	}
	
	public String name() {
	    return ("Row Count: " + rowProcessedCount.get() + "/"
		    + keyExistsCount.get());
	}
	
	public long getCount() {
	    return rowProcessedCount.get();
	}
	
	public long getKeyExistsCount() {
	    return keyExistsCount.get();
	}
	
	public Row getNext() {
	    String line = null;
	    Row row = null;
	    
	    try {
		if (!dataQueue.isEmpty()) {
		    try {
			line = dataQueue.take();
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
		if (line == null) {
		    return (null);
		}
		
		// read the contents of the Blocking Queue till it finds an EOF
		if (!line.equals("EOF")) {
		    
		    row = childTable.createRow();
		    // create Value
		    String[] columnData = columns.split(",");
		    String[] columnValue = new String[2];
		    if (line != null) {
			String[] split = line.split(";");
			columnValue[0] = split[0];
			columnValue[1] = split[4];
		    }
		    
		    // If COlumns Data and Column Value length are the same only
		    // then proceed.
		    if (columnData.length == columnValue.length) {
			System.out.println("Loading Child Table");
			
			for (int i = 0; i < columnData.length; i++) {
			    if (columnData[i].equals("qid")) {
				row.put("qid", new Long(columnValue[i]).longValue());
				row.put("id",
						new Long(columnValue[i]).longValue());
			    } else if (columnData[i].equals("quotes")) {
				row.putJson(columnData[i], columnValue[i]);
			    }
			}
		    }
		    
		    rowProcessedCount.incrementAndGet();
		}
	    } catch (DurabilityException e) {
		System.out.println("Exception - Check Durability Settings");
		e.printStackTrace();
	    } catch (RequestTimeoutException e) {
		System.out.println("Exception - Request Timed Out");
		e.printStackTrace();
	    } catch (FaultException e) {
		System.out.println("Exception - Fault Exception");
		e.printStackTrace();
	    }
	    return (row);
	}
	
	private String getRandomId() {
	    SecureRandom random = new SecureRandom();
	    return new BigInteger(130, random).toString(32);
	}
    }
}
