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

package com.oracle.fleet.to;

import org.codehaus.jackson.node.ObjectNode;

import com.oracle.fleet.constant.TableConstants;
import com.oracle.fleet.util.StringUtil;

public class FeedTO extends BaseTO {

	
	
	private String driverId = null;
	private float longitude = 0;
	private float latitude = 0;
	private int odometer = 0;
	private float fuelUsed = 0;
	private long time = 0;
	private int speed = 0;
	private String vin = null;
	
	public FeedTO() {
		super();
	}
	
	public FeedTO(String feedJSON) {
		super();
		ObjectNode feedNode = null;

		if (StringUtil.isNotEmpty(feedJSON)) {
			try {
				feedNode = super.parseJson(feedJSON.trim());

				this.setVin(feedNode.get(TableConstants.VIN_COLUMN).getTextValue());
				this.setDriverId(feedNode.get(TableConstants.DRIVERID_COLUMN)
						.getTextValue());
				this.setTime(feedNode.get(TableConstants.TIME_COLUMN).getLongValue());
				this.setLatitude(((Double) feedNode.get(TableConstants.LAT_COLUMN)
						.getDoubleValue()).floatValue());
				this.setLongitude(((Double) feedNode.get(TableConstants.LNG_COLUMN)
						.getDoubleValue()).floatValue());
				this.setFuelUsed(((Double) feedNode.get(TableConstants.FUEL_COLUMN)
						.getDoubleValue()).floatValue());
				this.setSpeed(feedNode.get(TableConstants.SPEED_COLUMN).getIntValue());
				this.setOdometer(feedNode.get(TableConstants.ODO_COLUMN).getIntValue());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}//if (StringUtil.isNotEmpty(feedJSON))
	}//FeedTO

	public String getDriverId() {
		return driverId;
	}

	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public int getOdometer() {
		return odometer;
	}

	public void setOdometer(int odometer) {
		this.odometer = odometer;
	}

	public float getFuelUsed() {
		return fuelUsed;
	}

	public void setFuelUsed(float fuelUsed) {
		this.fuelUsed = fuelUsed;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public String toString() {
		String str = "Vin: " + this.vin + " driverId: " + driverId + " fuel: "
				+ this.fuelUsed + " speed: " + speed + " odo: " + this.odometer
				+ " time: " + this.time;
		return str;
	}

	@Override
	public String toJsonString() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
