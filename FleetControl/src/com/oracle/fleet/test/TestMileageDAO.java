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

package com.oracle.fleet.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import oracle.kv.Direction;

import com.oracle.fleet.constant.TableConstants;
import com.oracle.fleet.dao.MileageDAO;
import com.oracle.fleet.exception.DAOException;
import com.oracle.fleet.to.FeedTO;
import com.oracle.fleet.to.MileageTO;

public class TestMileageDAO {

	public static void main(String[] args) throws DAOException {
		
		MileageDAO mileageDAO = new MileageDAO();
		FeedTO feedTO = new FeedTO();
		List<MileageTO> mileageTOList;
		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String highestDate;
		int counter;
		
		////////Add two mileage feeds (for 2 different VINs) 
		////////for time 1 (this corresponds to date 100/100=1)
		//feed 1
		feedTO.setDriverId("MY_TEST_DRIVER1");
		feedTO.setVin("MY_TEST_VIN1");
		feedTO.setFuelUsed(10);
		feedTO.setOdometer(100);
		feedTO.setSpeed(10);
		feedTO.setLatitude(37.5308f);
		feedTO.setLongitude(-122.2636f);
		feedTO.setTime(100);
		mileageDAO.add(feedTO);
		
		//feed 2
		feedTO.setDriverId("MY_TEST_DRIVER2");
		feedTO.setVin("MY_TEST_VIN2");
		feedTO.setFuelUsed(20);
		feedTO.setOdometer(200);
		feedTO.setSpeed(20);
		feedTO.setLatitude(37.5308f);
		feedTO.setLongitude(-122.2636f);
		feedTO.setTime(100);
		mileageDAO.add(feedTO);
		
		//when called with date=null getOrderedMileage returns the mileage list for the highest available date
		//so mileageTOList is a list of mileageTO objects corresponding to the highest date from kvstore,
		//containing total distance traveled by each VIN on that date, ordered descending
		mileageTOList = mileageDAO.getOrderedMileage(null, Direction.REVERSE, TableConstants.DIST_INDEX);
		highestDate = mileageTOList.get(0).getDate();
		counter = mileageTOList.get(0).getCounter();
		//suspend/resume execution block
		try {
			System.out.println("Two rows INSERTED\n" 
					+ "Highest date: " + highestDate
					+ "\nItems in feeds array: " + counter
					+ "\nLook into the database and then press <Enter> to continue");
			br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		////////////////////////////////////
		
		////////Add two mileage feeds (for 2 different VINs) 
		////////for time 2 (this corresponds to date 101/100=1, so the date doesn't change 
		////////                                             => update the previous records)
		
		//feed 1
		feedTO.setDriverId("MY_TEST_DRIVER1");
		feedTO.setVin("MY_TEST_VIN1");
		feedTO.setFuelUsed(12);
		feedTO.setOdometer(110);
		feedTO.setSpeed(12);
		feedTO.setTime(101);
		mileageDAO.add(feedTO);
		
		//feed 2
		feedTO.setDriverId("MY_TEST_DRIVER2");
		feedTO.setVin("MY_TEST_VIN2");
		feedTO.setFuelUsed(22);
		feedTO.setOdometer(220);
		feedTO.setSpeed(22);
		feedTO.setTime(101);
		mileageDAO.add(feedTO);
		
		//when called with date=null getOrderedMileage returns the mileage list for the highest available date
		//so mileageTOList is a list of mileageTO objects corresponding to the highest date from kvstore,
		//containing total distance traveled by each VIN on that date, ordered descending
		mileageTOList = mileageDAO.getOrderedMileage(null, Direction.REVERSE, TableConstants.DIST_INDEX);
		highestDate = mileageTOList.get(0).getDate();
		counter = mileageTOList.get(0).getCounter();
		try {
			System.out.println("Two rows UPDATED\n" 
					+ "Highest date: " + highestDate
					+ "\nItems in feeds array: " + counter
					+ "\nLook into the database and then press <Enter> to continue");
			br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		////////////////////////////////////
		
		////////Add two mileage feeds (for 2 different VINs) 
		////////for time 200 (this corresponds to date 200/100=2, so the date changes
		//////// 												 => insert new records)
		
		//feed 1
		feedTO.setDriverId("MY_TEST_DRIVER1");
		feedTO.setVin("MY_TEST_VIN1");
		feedTO.setFuelUsed(14);
		feedTO.setOdometer(120);
		feedTO.setSpeed(30);
		feedTO.setTime(200);
		mileageDAO.add(feedTO);
		
		//feed 2
		feedTO.setDriverId("MY_TEST_DRIVER2");
		feedTO.setVin("MY_TEST_VIN2");
		feedTO.setFuelUsed(10);
		feedTO.setOdometer(230);
		feedTO.setSpeed(40);
		feedTO.setTime(200);
		mileageDAO.add(feedTO);
		
		//when called with date=null getOrderedMileage returns the mileage list for the highest available date
		//so mileageTOList is a list of mileageTO objects corresponding to the highest date from kvstore,
		//containing total distance traveled by each VIN on that date, ordered descending
		mileageTOList = mileageDAO.getOrderedMileage(null, Direction.REVERSE, TableConstants.DIST_INDEX);
		highestDate = mileageTOList.get(0).getDate();;
		counter = mileageTOList.get(0).getCounter();

		System.out.println("Two rows INSERTED\n" 
					+ "Highest date: " + highestDate
					+ "\nItems in feeds array: " + counter
					+ "\nFinished execution.");


		////////////////////////////////////
		
	}

}
