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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.oracle.fleet.constant.TableConstants;
import com.oracle.fleet.exception.DAOException;
import com.oracle.fleet.to.FeedTO;
import com.oracle.fleet.to.MileageTO;
import com.oracle.fleet.util.StringUtil;

import oracle.kv.Direction;
import oracle.kv.FaultException;
import oracle.kv.Version;
import oracle.kv.table.ArrayValue;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.RecordValue;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableIterator;
import oracle.kv.table.TableIteratorOptions;

public class MileageDAO extends BaseDAO {

	private Table mileageTable = null;

	public static final String TABLE_NAME = "fleet.mileage";

	// private List<FeedTO> feedList = new ArrayList<FeedTO>();

	// secondary index
	public static final String DATE_INDEX = "dateIndex";

	public MileageDAO() {
		super();
		mileageTable = super.getTable(TABLE_NAME);
	}// FleetDAO

	@Override
	/**
	 *  This method would create FLEET.MILEAGE table in the database. its a child table to the parent fleet
	 *  Another factor is the key structure of your table  What have you defined as your primary key and shard key.
	 *   These are important in order to ensure that your rows are distributed across partitions and not localized on the same partition, which would affect scale.

	 */
	public void createTable() {
		String tableStr = "CREATE TABLE fleet.mileage ("
				+ "date STRING, "
				+ "driverId STRING, "
				+ "startOdometer INTEGER, "
				+ "avgSpeed FLOAT, "
				+ "totalDistance INTEGER , "
				+ "totalFuel FLOAT, "
				+ "mpg FLOAT, "
				+ "counter INTEGER, "
				+ "feeds ARRAY( "
				+ "RECORD ("
				+ "currentTime LONG, "
				+ "longitude FLOAT, "
				+ "latitude FLOAT, "
				+ "odometer INTEGER, "
				+ "fuelUsed FLOAT, "
				+ "speed INTEGER" + ")"
				+ "), " + "PRIMARY KEY (date)" + ")";

		String mpgIndex = "CREATE INDEX mpgIndex ON fleet.mileage(date, mpg)";
		String fuelIndex = "CREATE INDEX fuelIndex ON fleet.mileage(date, totalFuel)";
		String distanceIndex = "CREATE INDEX distanceIndex ON fleet.mileage(date, totalDistance)";

		try {
			// create table

			getKVStore().executeSync(tableStr);

			// create indexes

			getKVStore().executeSync(mpgIndex);
			getKVStore().executeSync(fuelIndex);
			getKVStore().executeSync(distanceIndex);

			System.out
					.println("fleet.mileage TABLE & INDEX mpgIndex, fuelIndex, distanceIndex  created...");
		} catch (IllegalArgumentException e) {
			System.out.println("The statement is invalid: " + e);
		} catch (FaultException e) {
			System.out.println("There is a transient problem, retry the "
					+ "operation: " + e);
		}// try

	}// createTable

	/**
	 * Overloaded method to insert a feed and update the averages with each feed
	 * for that VIN and for an hr
	 * 
	 * @param vin
	 * @param time
	 * @param fuel
	 * @param speed
	 * @param odometer
	 * @return true if successful
	 * @throws DAOException
	 */
	public boolean add(String vin, long time, float fuelUsed, int speed,
			int odometer) throws DAOException {

		FeedTO feedTO = new FeedTO();
		feedTO.setVin(vin);
		feedTO.setTime(time);
		feedTO.setFuelUsed(fuelUsed);
		feedTO.setSpeed(speed);
		feedTO.setOdometer(odometer);

		return this.add(feedTO);
	}

	/**
	 * This method inserts an incoming mileage feed from a Fleet and update the
	 * averages
	 * 
	 * @param feedTO
	 * @return true if successful
	 * @throws DAOException
	 */
	public boolean add(FeedTO feedTO) throws DAOException {

		Version version = null;
		boolean status = false;
		String date = null;
		String vin = null;
		long time = 0;
		int counter = 1;
		int startOdo = 0;
		int totalDistance = 0;
		float avgSpeed = 0;
		float totalFuel = 0;
		float mpg = 0;

		/**
		 * " "avgSpeed" : null, "totalDistance" : null, "totalFuel" : null,
		 * "mpg" : null,
		 */

		// create a row for Mileage table
		Row mileageRow = this.mileageTable.createRow();
		ArrayValue feeds = mileageRow.putArray(TableConstants.FEEDS_ARRAY);
		RecordValue feedRecord = null;

		// make sure none of the value is empty
		if (feedTO != null) {

			vin = feedTO.getVin();
			// Let's take the trailing two zeros and convert it into the String
			time = feedTO.getTime() / 100;
			date = String.format("%04d", time);

			// mileage values
			totalFuel = feedTO.getFuelUsed();
			totalDistance = 0;
			avgSpeed = feedTO.getSpeed();
			mpg = 0;

			// set VIN as the parent key
			mileageRow.put(TableConstants.VIN_COLUMN, vin);
			mileageRow
					.put(TableConstants.DRIVERID_COLUMN, feedTO.getDriverId());
			mileageRow.put(TableConstants.DATE_COLUMN, date);
			mileageRow.put(TableConstants.START_ODO_COLUMN,
					feedTO.getOdometer());

			// set aggregate values
			mileageRow.put(TableConstants.MPG_COLUMN, mpg);
			mileageRow.put(TableConstants.TOTAL_DIST_COLUMN, totalDistance);
			mileageRow.put(TableConstants.TOTAL_FUEL_COLUMN, totalFuel);
			mileageRow.put(TableConstants.AVG_SPEED_COLUMN, avgSpeed);
			mileageRow.put(TableConstants.COUNTER_COLUMN, counter);

			// add a new FEED to the Array
			feedRecord = feeds.addRecord();
			this.addFeed(feedRecord, feedTO);

			// Insert Mileage info into the database.
			version = getTableAPI().putIfAbsent(mileageRow, null, null);

			/**
			 * Unsuccessful somebody probably else modified the the record
			 */
			if (version == null) {
				// as there is already a value available then read the value
				// first and then update it
				mileageRow = this.getRow(feedTO.getVin(), date);
				feeds = mileageRow.get(TableConstants.FEEDS_ARRAY).asArray();
				feedRecord = feeds.addRecord();

				// now consolidate old value with new feed
				counter = mileageRow.get(TableConstants.COUNTER_COLUMN)
						.asInteger().get() + 1;
				totalFuel = mileageRow.get(TableConstants.TOTAL_FUEL_COLUMN)
						.asFloat().get()
						+ feedTO.getFuelUsed();
				startOdo = mileageRow.get(TableConstants.START_ODO_COLUMN)
						.asInteger().get();
				totalDistance = feedTO.getOdometer() - startOdo;
				avgSpeed = mileageRow.get(TableConstants.AVG_SPEED_COLUMN)
						.asFloat().get();
				// updated avgSpeed by reading the old avgSpeed and multiplying
				// with counter and adding the new speed and then dividing with
				// new counter
				avgSpeed = ((counter - 1) * avgSpeed + feedTO.getSpeed())
						/ counter;

				// let's calculate the MPG
				if (totalDistance > 0 && totalFuel > 0) {
					mpg = totalDistance / totalFuel;
				}

				// update aggregate values
				mileageRow.put(TableConstants.MPG_COLUMN, mpg);
				mileageRow.put(TableConstants.TOTAL_DIST_COLUMN, totalDistance);
				mileageRow.put(TableConstants.TOTAL_FUEL_COLUMN, totalFuel);
				mileageRow.put(TableConstants.AVG_SPEED_COLUMN, avgSpeed);
				mileageRow.put(TableConstants.COUNTER_COLUMN, counter);

				// add new feed
				this.addFeed(feedRecord, feedTO);

				/**
				 * Now write it back to database.
				 */
				version = getTableAPI().put(mileageRow, null, null);

				// check again if successfully written
				if (version == null) {
					throw new DAOException(
							"Failed to update the feed for VIN: " + vin);
				} else {
					System.out.println("Mielage for : VIN " + feedTO.getVin()
							+ "' has been updated");
				}
			} else {
				status = true;
				System.out
						.println("Successfully Inserted Mileage Information for VIN: "
								+ feedTO.getVin());
			}

		}// EOF if
		return status;
	}// add

	private void addFeed(RecordValue feedRecord, FeedTO feedTO) {
		// add column values to the row object
		feedRecord.put(TableConstants.TIME_COLUMN, feedTO.getTime());
		feedRecord.put(TableConstants.LNG_COLUMN, feedTO.getLongitude());
		feedRecord.put(TableConstants.LAT_COLUMN, feedTO.getLatitude());
		feedRecord.put(TableConstants.ODO_COLUMN, feedTO.getOdometer());
		feedRecord.put(TableConstants.SPEED_COLUMN, feedTO.getSpeed());
		feedRecord.put(TableConstants.FUEL_COLUMN, feedTO.getFuelUsed());

	}

	/**
	 * This method returns three top or bottom (depending on Direction)
	 * Fleet.Mileage rows for the highest Date, which is the current date. This
	 * query helps to display best or worst vehicle for the selected hour.
	 * 
	 * @param date
	 *            - Time upto hrs. In our case we have dates in form of DDHHMM
	 *            and to make it granular upto Hr we remove last two digits so
	 *            that date becomes DDHH
	 * @param direction
	 *            - FORWORD for ascending and REVERSE for descending
	 * @param indexName
	 *            - pass the IndexName to sort the records. All these Indexes
	 *            have first field as date and second field as a different
	 *            columns
	 * @return - List of three records based on Direction and IndexName selected
	 */
	public List<MileageTO> getOrderedMileage(String date, Direction direction,
			String indexName) {

		List<MileageTO> mileageList = new ArrayList<MileageTO>();
		Iterator<Row> rows = null;
		Row row = null;
		Index index = null;
		IndexKey indexKey = null;
		int count = 0;
		MileageTO mileageTO = null;
		TableIteratorOptions tio = new TableIteratorOptions(direction, null, 0,
				null);

		/* get the index */
		index = mileageTable.getIndex(indexName);

		/*
		 * index is on vehicle MAKE
		 */
		indexKey = index.createIndexKey();

		// set date only if passed as an argument
		if (date == null)
			date = this.getHighestDate();

		// check again to make sure date is not null again
		if (date != null)
			indexKey.put(TableConstants.DATE_COLUMN, date);

		// Get Fleet by VIN

		rows = getTableAPI().tableIterator(indexKey, null, tio);

		// convert row into aggregateTO
		while (rows.hasNext()) {
			row = rows.next();
			// System.out.println(row.toJsonString(false));
			mileageTO = this.toMileageTO(row);
			mileageList.add(mileageTO);

			// we just need top 3 or bottom 3 records only
			if (++count >= 3)
				break;
		}// while

		return mileageList;
	}// getOrderedList

	/**
	 * This method returns the highest date in the Fleet.Mileage table at point
	 * of query
	 * 
	 * @return date column
	 */
	private String getHighestDate() {
		Row row = null;
		Index index = null;
		IndexKey indexKey = null;
		Iterator<Row> rows = null;
		String date = null;

		TableIteratorOptions tio = new TableIteratorOptions(Direction.REVERSE,
				null, 0, null);

		/* get the index */
		index = mileageTable.getIndex(TableConstants.MPG_INDEX);
		indexKey = index.createIndexKey();
		// get hold of the first batch of rows
		rows = getTableAPI().tableIterator(indexKey, null, tio);
		if (rows.hasNext()) {
			// get the top most row and fetch the date column
			row = rows.next();
			date = row.get(TableConstants.DATE_COLUMN).asString().get();
		}
		return date;
	}// getHighestDate

	// get mileage info for a VIN during a date
	public Row getRow(String vin, String date) throws DAOException {

		Row row = null;

		if (StringUtil.isNotEmpty(vin) && StringUtil.isNotEmpty(date)) {

			// Get Fleet by VIN
			PrimaryKey key = mileageTable.createPrimaryKey();
			key.put(TableConstants.VIN_COLUMN, vin);
			key.put(TableConstants.DATE_COLUMN, date);

			row = getTableAPI().get(key, null);

		} else {
			throw new DAOException("VIN & date can not be empty");
		}

		return row;
	}// getRow

	public List<MileageTO> getFleetMileages(String vin) throws DAOException {

		MileageTO mileageTO = null;
		List<MileageTO> mileageList = new ArrayList<MileageTO>();
		List<Row> mileageRows = null;

		if (StringUtil.isNotEmpty(vin)) {

			// Get Fleet by VIN
			PrimaryKey key = mileageTable.createPrimaryKey();
			key.put(TableConstants.VIN_COLUMN, vin);
			
			mileageRows = getTableAPI().multiGet(key, null, null);
			// System.out.println("Count is " + mileageRows.size());

			// convert row into mileageTO
			for (Row row : mileageRows) {
				// System.out.println(row.toJsonString(false));
				mileageTO = this.toMileageTO(row);
				mileageList.add(mileageTO);
			}// for

		} else {
			throw new DAOException("VIN can not be empty");
		}

		return mileageList;
	}// getFleetMileages

	/**
	 * Get last 24 average data points for a VIN
	 * 
	 * @param vin
	 * @return List of AggregateTO
	 * @throws DAOException
	 */
	public List<MileageTO> get(String vin) throws DAOException {

		MileageTO aggregateTO = null;
		Row row = null;
		List<MileageTO> mileageList = new ArrayList<MileageTO>();
		TableIterator<Row> rows = null;
		TableIteratorOptions tio = new TableIteratorOptions(Direction.REVERSE,
				null, 0, null);

		int count = 0;

		if (StringUtil.isNotEmpty(vin)) {

			// Get Fleet by VIN
			PrimaryKey key = mileageTable.createPrimaryKey();
			key.put(TableConstants.VIN_COLUMN, vin);

			rows = getTableAPI().tableIterator(key, null, tio);

			// convert row into FleetTO
			while (rows.hasNext()) {
				row = rows.next();
				aggregateTO = this.toMileageTO(row);
				// System.out.println(row.toJsonString(true));
				mileageList.add(aggregateTO);

				// get top
				if (++count >= 24)
					break;
			}
		} else {
			throw new DAOException("VIN can not be empty");
		}

		return mileageList;
	}// get(String vin)

	/**
	 * Get all the records in the Fleet.Mileage table
	 * 
	 * @return
	 * @throws DAOException
	 */
	public Iterator<Row> getAllMileages() throws DAOException {

		Iterator<Row> rows = null;
		Index index = null;
		IndexKey indexKey = null;

		/* get the index */
		index = mileageTable.getIndex(DATE_INDEX);
		indexKey = index.createIndexKey();

		// Get all the fleet.mileage rows in ascending order
		rows = getTableAPI().tableIterator(indexKey, null, null);

		return rows;
	}// getAllMileages

	/**
	 * This method converts Row object in the MileageTO
	 * 
	 * @param row
	 *            - Fleet.Mileage Row
	 * @return MileageTO
	 */
	public MileageTO toMileageTO(Row mileageRow) {
		MileageTO mileageTO = null;

		if (mileageRow != null) {
			mileageTO = new MileageTO();
			mileageTO.setDriverId(mileageRow
					.get(TableConstants.DRIVERID_COLUMN).asString().get());
			mileageTO.setVin(mileageRow.get(TableConstants.VIN_COLUMN)
					.asString().get());
			mileageTO.setDate(mileageRow.get(TableConstants.DATE_COLUMN)
					.asString().get());
			mileageTO.setMpg(mileageRow.get(TableConstants.MPG_COLUMN)
					.asFloat().get());
			mileageTO.setCounter(mileageRow.get(TableConstants.COUNTER_COLUMN)
					.asInteger().get());
			mileageTO.setStartOdometer(mileageRow
					.get(TableConstants.START_ODO_COLUMN).asInteger().get());
			mileageTO.setAvgSpeed(mileageRow
					.get(TableConstants.AVG_SPEED_COLUMN).asFloat().get());
			mileageTO.setTotalDistance(mileageRow
					.get(TableConstants.TOTAL_DIST_COLUMN).asInteger().get());
			mileageTO.setTotalFuel(mileageRow
					.get(TableConstants.TOTAL_FUEL_COLUMN).asFloat().get());

			// TODO - add List of FeedTO to the MileageTO as well

			System.out.println(mileageTO.toString());
		}

		return mileageTO;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String vin = "W8F04A5OZ1218126Z407";
		MileageDAO mileageDAO = new MileageDAO();
		Long time = new Date().getTime();

		FeedTO mileageTO = new FeedTO();
		mileageTO.setDriverId("XSKU12");
		mileageTO.setVin(vin);
		mileageTO.setFuelUsed(32);
		mileageTO.setOdometer(12253);
		mileageTO.setSpeed(54);
		mileageTO.setLatitude(-116.20f);
		mileageTO.setLongitude(33.323f);
		mileageTO.setTime(time);

		// add to the database
		try {

			// test getOrderedList of aggregate rows
			mileageDAO.getOrderedMileage(null, Direction.FORWARD,
					TableConstants.MPG_INDEX);

			System.out.println(mileageDAO.getHighestDate());

			System.exit(1);

			// mileageDAO.add(mileageTO);
			mileageDAO.getFleetMileages(vin);
		} catch (DAOException e) {
			e.printStackTrace();
		}

	}// main

}
