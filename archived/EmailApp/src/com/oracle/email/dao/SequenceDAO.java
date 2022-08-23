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
package com.oracle.email.dao;

import com.oracle.email.constant.table.Sequence;
import com.oracle.email.to.SequenceTO;
import com.oracle.email.util.EncodeUtil;

import oracle.kv.FaultException;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;


public class SequenceDAO extends BaseDAO {

	private Table sequenceTable = null;
			
	public SequenceDAO() {
		super();
		sequenceTable = getTable(Sequence.TABLE_NAME);
	}

	
	@Override
	/**
	 *  This method would create Sequence table in the database.
	 */
	public void createTable() {
		String tableStr = "CREATE TABLE IF NOT EXISTS sequence ("
								+ "name STRING, "
								+ "current LONG, "
								+ "increment INTEGER DEFAULT 1, "
								+ "cache INTEGER DEFAULT 20, "
								+ "PRIMARY KEY (name)"
								+ ")";
		try {
			//getTableAPI().executeSync(tableStr);
			getKVStore().executeSync(tableStr);
			System.out.println("SEQUENCE table  created...");
		} catch (IllegalArgumentException e) {
			System.out.println("The statement is invalid: " + e);
		} catch (FaultException e) {
			System.out.println("There is a transient problem, retry the "
					+ "operation: " + e);
		}// try
		
	}//createTable
	
	/**
	 * This method returns the sequence row for the unique sequence name. This row contains information like
	 * current sequence number, number of sequences to be cached per insert.
	 * 
	 * @param sequenceName
	 *            - Unique name of the sequence
	 * @return SequenceTO - TO object with sequence details 
	 */
	public SequenceTO getSequence(String sequenceName) {

		SequenceTO sequenceTO = null;
		Row row = null;

		// Get User by userId
		PrimaryKey key = sequenceTable.createPrimaryKey();
		key.put(Sequence.NAME, sequenceName);
		row = getTableAPI().get(key, null);

		// convert row into categoryTO
		if (row != null) {
			sequenceTO = this.toSequenceTO(row);
		}

		return sequenceTO;
	} // getSequence


	/**
	 * This method updates a Sequence.
	 * 
	 * @param sequenceTO
	 *            with all the information about the Sequence
	 * @return SequenceTO
	 */
	public SequenceTO updateSequence(SequenceTO sequenceTO) {

		Row row = null;

		if (sequenceTO != null) {

			row = this.toSequenceRow(sequenceTO);

			// Add store
			if (row != null) {

				getTableAPI().put(row, null, null);
				//System.out.println("Sequence updated: " + sequenceTO.getName());
				//System.out.println(row.toJsonString(false));
			} // if (storeRow != null)
		} // if(storeTO!=null){
		return sequenceTO;
	} // updateSequence
	
	/**
	 * Internal method to construct the table row
	 * 
	 * @param SequenceTO
	 * @return Sequence table Row
	 */
	private Row toSequenceRow(SequenceTO sequenceTO) {

		Row row = null;

		if (sequenceTO != null) {

			// Create Category Row
			row = sequenceTable.createRow();

			row.put(Sequence.NAME, sequenceTO.getName());
			row.put(Sequence.CACHE, sequenceTO.getCache());
			row.put(Sequence.CURRENT,
					EncodeUtil.decode(sequenceTO.getCurrent()));
			row.put(Sequence.INCREMENT, sequenceTO.getIncrement());

		} // if (sequenceTO != null) {

		return row;

	} // toSequenceRow(sequenceTO)

	/**
	 * Internal method that converts Sequence row into SequenceTO
	 * 
	 * @param row
	 *            belongs to Sequence table
	 * @return sequenceTO
	 */
	private SequenceTO toSequenceTO(Row row) {

		SequenceTO sequenceTO = null;
		// Extract user information
		if (row != null) {

			//System.out.println(row.toJsonString(true));

			sequenceTO = new SequenceTO();
			// Now add field values

			sequenceTO.setName(row.get(Sequence.NAME).asString().get());
			sequenceTO.setCurrent(EncodeUtil.encode(row.get(Sequence.CURRENT)
					.asLong().get()));
			sequenceTO.setIncrement(row.get(Sequence.INCREMENT).asInteger()
					.get());
			sequenceTO.setCache(row.get(Sequence.CACHE).asInteger().asInteger()
					.get());

		} // if (row != null)

		return sequenceTO;
	} // toCategoryTO


	public static void main(String[] main){
		SequenceDAO seqDAO = new SequenceDAO();
		seqDAO.createTable();
		
	}//main
}

