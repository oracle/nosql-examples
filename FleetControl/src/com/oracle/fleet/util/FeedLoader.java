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
package com.oracle.fleet.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.oracle.fleet.dao.MileageDAO;
import com.oracle.fleet.exception.DAOException;
import com.oracle.fleet.to.FeedTO;

public class FeedLoader {

	private static MileageDAO mileageDAO = new MileageDAO();
	
	public static void readFile(String filePath) {
		BufferedReader br = null;
		FeedTO feedTO = null;
		int counter = 0;
		
		try {

			String feedJson;

			br = new BufferedReader(new FileReader(filePath));

			while ((feedJson = br.readLine()) != null) {
								
				if(StringUtil.isNotEmpty(feedJson)){
					System.out.println(feedJson);
					
					//convert JSON into FeedTO
					feedTO = new FeedTO(feedJson);
					
					//put feed into the mileage table
					mileageDAO.add(feedTO);
					
				}//EOF if
				
				//sleep for 1 second after each hundred insertion
				if(counter++%100==0)
					Thread.sleep(1000);
				
			}//while

		} catch (IOException | DAOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}//finally
	}// readFile
	
	public static void main(String[] args){
		String fleetHome = System.getenv("FLEET_HOME") + File.separator;
		FeedLoader.readFile(fleetHome + "data/fleet.mileage.dat");
		
	}//main
	
}
