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

import java.util.ArrayList;
import java.util.List;

public class MileageTO {

		
	private String vin = null;
	private String driverId = null;
	private String date = null;
	public float totalFuel = 0;
	private int totalDistance = 0;
	private int startOdometer = 0;
	
	private float avgSpeed = 0;
	private float mpg = 0; 
	private int counter = 0;

	
	private List<FeedTO> feedList = new ArrayList<FeedTO>(); 
	
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	public String getDriverId() {
		return driverId;
	}
	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String yyyyMMddHH) {
		this.date = yyyyMMddHH;
	}
	public float getTotalFuel() {
		return totalFuel;
	}
	public void setTotalFuel(float totalFuel) {
		this.totalFuel = totalFuel;
	}
	public int getTotalDistance() {
		return totalDistance;
	}
	public void setTotalDistance(int totalDistance) {
		this.totalDistance = totalDistance;
	}
	
	
	public float getAvgSpeed() {
		return avgSpeed;
	}
	public void setAvgSpeed(float avgSpeed) {
		this.avgSpeed = avgSpeed;
	}
	public float getMpg() {
		return mpg;
	}
	public void setMpg(float mpg) {
		this.mpg = mpg;
	}
	public int getCounter() {
		return counter;
	}
	public void setCounter(int counter) {
		this.counter = counter;
	}
	
	public int getStartOdometer() {
		return startOdometer;
	}
	public void setStartOdometer(int startOdometer) {
		this.startOdometer = startOdometer;
	}
	public List<FeedTO> getFeedList() {
		return feedList;
	}
	public void setFeedList(List<FeedTO> feedList) {
		this.feedList = feedList;
	}

	public void addFeed(FeedTO feedTO){
		this.feedList.add(feedTO);
	}
	
}
