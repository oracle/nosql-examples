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

package com.oracle.fleet.constant;

public interface TableConstants {

	/**
	 * Fleet.Mileage Table
	 */
	// Column names
	public static final String START_ODO_COLUMN = "startOdometer";
	public static final String AVG_SPEED_COLUMN = "avgSpeed";
	public static final String TOTAL_FUEL_COLUMN = "totalFuel";
	public static final String TOTAL_DIST_COLUMN = "totalDistance";
	public static final String MPG_COLUMN = "mpg";
	public static final String COUNTER_COLUMN = "counter";

	// Row columns names
	public static final String VIN_COLUMN = "vin";
	public static final String DRIVERID_COLUMN = "driverID";
	public static final String LNG_COLUMN = "longitude";
	public static final String LAT_COLUMN = "latitude";
	public static final String ODO_COLUMN = "odometer";
	public static final String FUEL_COLUMN = "fuelUsed";
	public static final String SPEED_COLUMN = "speed";
	public static final String TIME_COLUMN = "currentTime";
	public static final String FEEDS_ARRAY = "feeds";
	public static final String DATE_COLUMN = "date";

	// Indexes
	public static final String MPG_INDEX = "mpgIndex";
	public static final String DIST_INDEX = "distanceIndex";
	public static final String FUEL_INDEX = "fuelIndex";
}
