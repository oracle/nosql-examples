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
import java.util.Iterator;
import java.util.List;

import com.oracle.fleet.exception.DAOException;
import com.oracle.fleet.to.FleetTO;
import com.oracle.fleet.util.StringUtil;

import oracle.kv.FaultException;
import oracle.kv.Version;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

public class FleetDAO extends BaseDAO {

	private Table fleetTable = null;
	public static final String TABLE_NAME = "fleet";

	// few more constants
	public static final String DISEAL = "D";
	public static final String GAS = "G";
	public static final String ELECTRIC = "E";

	// Index keys
	public static final String MAKE_INDEX = "makeIndex";

	// private TableAPI tableImpl = null;

	public FleetDAO() {
		super();
		fleetTable = super.getTable(TABLE_NAME);
	}// FleetDAO

	@Override
	/**
	 *  This method would create FLEET table in the database.
	 */
	public void createTable() {
		String tableStr = "CREATE TABLE fleet (" + "vin STRING, "
				+ "make STRING, " + "model STRING, "
				+ "year INTEGER, "
				+ "fuelType ENUM(D,G,C), "
				+ "vehicleType ENUM(CAR,LIGHT_TRUCK,TRUCK,SUV,VAN), "
				+ "PRIMARY KEY (vin)" + ")";

		String makeIndex = "CREATE INDEX makeIndex ON fleet(make)";

		try {
			// Synchronously execute a sync statement to create the Table

			getKVStore().executeSync(tableStr);

			// Create index

			getKVStore().executeSync(makeIndex);

			System.out.println("fleet TABLE & INDEX makeIndex created...");
		} catch (IllegalArgumentException e) {
			System.out.println("The statement is invalid: " + e);
		} catch (FaultException e) {
			System.out.println("There is a transient problem, retry the "
					+ "operation: " + e);
		}

	}
	
	/**
	 * Insert record in the Fleet Table
	 * @param vin
	 * @param make
	 * @param model
	 * @param year
	 * @param fuelType
	 * @return
	 * @throws DAOException
	 */
	
	

	public boolean addFleet(String vin, String make, String model, int year,
			String fuelType) throws DAOException {

		// create a row for Fleet table
		Row fleetRow = this.fleetTable.createRow();
		Version version = null;

		// make sure none of the value is empty
		if (StringUtil.isNotEmpty(vin) && StringUtil.isNotEmpty(make)
				&& StringUtil.isNotEmpty(model) && year > 0) {

			// add column values to the row object
			fleetRow.put(FleetTO.VIN_COLUMN, vin);
			fleetRow.put(FleetTO.MAKE_COLUMN, make);
			fleetRow.put(FleetTO.MODEL_COLUMN, model);
			fleetRow.put(FleetTO.YEAR_COLUMN, year);
			fleetRow.putEnum(FleetTO.FUEL_COLUMN, fuelType);

			// Insert Fleet profile into the database.
			version = getTableAPI().putIfAbsent(fleetRow, null, null);
			
			/**
			 * Unsuccessful somebody probably else modified the the record
			 */

			if (version == null) {
				throw new DAOException("Fleet with same : VIN " + vin
						+ "' already exist in database. ");
			} else {
				System.out
				.println("Successfully Inserted Fleet Information for VIN: "
						+ vin);
			}

		}// EOF if
		return false;
	}// add

	public FleetTO getFleet(String vin) throws DAOException {

		Row row = null;
		FleetTO fleetTO = null;

		if (StringUtil.isNotEmpty(vin)) {

			// Get Fleet by VIN
			PrimaryKey key = fleetTable.createPrimaryKey();
			key.put(FleetTO.VIN_COLUMN, vin);
			row = getTableAPI().get(key, null);

			// System.out.println(row.toJsonString(false));

			// convert row into FleetTO
			if (row != null) {
				fleetTO = new FleetTO();
				fleetTO.setMake(row.get(FleetTO.MAKE_COLUMN).asString().get());
				fleetTO.setModel(row.get(FleetTO.MODEL_COLUMN).asString().get());
				fleetTO.setVin(row.get(FleetTO.VIN_COLUMN).asString().get());
				fleetTO.setYear(row.get(FleetTO.YEAR_COLUMN).asInteger().get());
				fleetTO.setFuelType(row.get(FleetTO.FUEL_COLUMN).asEnum().get());

				// System.out.println(row.toJsonString(false));
			}
		} else {
			throw new DAOException("VIN can not be empty");
		}

		return fleetTO;
	}

	public List<String> getVINs() throws DAOException {

		List<String> vinList = new ArrayList<String>();
		Iterator<Row> fleetRows = null;
		Row row = null;
		String vin = null;
		Index index = null;
		IndexKey indexKey = null;

		/* get the index */
		index = fleetTable.getIndex(MAKE_INDEX);

		/*
		 * index is on vehicle MAKE
		 */
		indexKey = index.createIndexKey();

		// Get Fleet by VIN
		// PrimaryKey key = fleetTable.crecreatePrimaryKey();
		fleetRows = getTableAPI().tableIterator(indexKey, null, null);

		// convert row into mileageTO
		while (fleetRows.hasNext()) {
			row = fleetRows.next();

			vin = row.get(FleetTO.VIN_COLUMN).asString().get();
			System.out.println(vin);
			vinList.add(vin);

		}// for

		return vinList;
	}// getFleetMileages

	public static void main(String[] args) {

		FleetDAO fleetDAO = new FleetDAO();
		String vin = "BA121KJJGH7865KOJ";

		try {
			// add
			fleetDAO.addFleet(vin, "Toyota", "Camry", 2012, DISEAL);

			// Display fleet details
			fleetDAO.getFleet(vin);
			fleetDAO.getVINs();

		} catch (DAOException e) {
			e.printStackTrace();
		}

	}// main

}
