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

import java.io.BufferedReader;
import java.util.concurrent.BlockingQueue;

/**
 * Thread class which loads each line into the Blocking queue
 * 
 * @author vsettipa
 * 
 */
public class QuotesReader implements Runnable {
    
    private BlockingQueue<String>[] queueList;
    private BufferedReader br;
    
    public QuotesReader(BufferedReader br, BlockingQueue<String>[] queueList) {
    	this.br = br;
    	this.queueList = queueList;
    }
    
    public void run() {
	int i = 0;
	String line = "";
	
	// add each line from the TXT file to the Blocking Queue
	try {
		//line = br.readLine();
	    
		for (i=1;i<10;i++) {
			line = br.readLine();
			if (i==9 || line==null){
				queueList[i].put("EOF");
			}else{
				queueList[i++].put(line);
			}
	    }
	    br.close();
	} catch (Exception e) {
	    System.out.println("Exception:" + e);
	}
    }
}