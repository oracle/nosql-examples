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
package com.oracle.email.constant.table;

public interface User {

	// User Table
	public final String TABLE_NAME = "Users";
	
	public final String EMAIL_PSWD_INDEX = "emailIndex";
	public final String USERID = "userId";
	public final String PASSWORD = "password";
	public final String FIRST = "first";
	public final String MIDDLE = "middle";
	public final String LAST = "last";
	public final String EMAIL = "email";
	public final String ACTIVE = "active";
	public final String NAME = "name";
	public final String GENDER = "gender";
	public final String AGE = "age";
	public final String CREATEDON = "createdOn";
	public final String MODIFIEDON = "modifiedOn";

	public final String DOMAIN = "@email.com";
	public final String ALTERNATE_MIDDLE = "abcdefghizklmnopqrstuvwxyz";

}
