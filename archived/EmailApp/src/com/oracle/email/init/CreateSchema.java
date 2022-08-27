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
package com.oracle.email.init;

import com.oracle.email.dao.MessageDAO;
import com.oracle.email.dao.SequenceDAO;
import com.oracle.email.dao.UserDAO;
import com.oracle.email.dao.UserFolderDAO;
import com.oracle.email.dao.UserFolderMessageDAO;

/**
 *  This class need to be run at the very beginning so that all the tables required to run this applications
 *  are created.
 * 
 *
 */
public class CreateSchema {

	private static UserDAO userDAO = new UserDAO();
	private static UserFolderDAO folderDAO = new UserFolderDAO();
	private static UserFolderMessageDAO folderMessageDAO = new UserFolderMessageDAO();
	private static SequenceDAO sequenceDAO = new SequenceDAO();
	private static MessageDAO messageDAO = new MessageDAO();
	
	public static void main(String[] args){
		
		//create all the tables and indexes first
		userDAO.createTable();
		folderDAO.createTable();
		folderMessageDAO.createTable();
		sequenceDAO.createTable();
		messageDAO.createTable();
	}//main
}
