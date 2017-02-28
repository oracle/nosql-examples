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
package com.oracle.email.to;

import oracle.kv.table.RecordValue;
import oracle.kv.table.Row;

import com.oracle.email.constant.table.User;
import com.oracle.email.util.StringUtil;

public class UserTO {

	private String userId = null;
	private String email = null;
	private String first = null;
	private String middle = null;
	private String last = null;
	private String password = null;
	private int age = 0;
	private long createdOn = 0;
	private long modifiedOn = 0;
	private boolean active = true;
	private String gender = null;

	public UserTO() {
		
	}
	
	public UserTO(Row row) {

		// Extract user information
		if (row != null) {

			// Now add field values
			this.setEmail(row.get(User.EMAIL).asString().get());
			this.setPassword(row.get(User.PASSWORD).asString().get());
			this.setActive(row.get(User.ACTIVE).asBoolean().get());
			this.setCreatedOn(row.get(User.CREATEDON).asLong().get());
			this.setUserId(row.get(User.USERID).asString().get());
			this.setGender(row.get(User.GENDER).asEnum().get());
			this.setAge(row.get(User.AGE).asInteger().get());

			RecordValue name = row.get(User.NAME).asRecord();
			this.setFirst(name.get(User.FIRST).asString().get());
			this.setMiddle(name.get(User.MIDDLE).asString().get());
			this.setLast(name.get(User.LAST).asString().get());

		} // if (row != null)

	} // UserTO

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getMiddle() {
		return middle;
	}

	public void setMiddle(String middle) {
		this.middle = middle;
	}

	public String getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last = last;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}

	public long getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(long modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getAge() {
		return age;
	}

	// TODO - helper method.
	public String getAgeStr() {
		return String.valueOf(age);
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setAge(String age) {
		if(StringUtil.isNotEmpty(age))
			this.age = Integer.parseInt(age);
	}

	public String toString() {
		String str = null;
		str = this.getUserId() + "," + this.getEmail() + "," + this.age + "," + this.getFirst() + ","
				+ this.getMiddle() + "," + this.getLast() + ","
				+ this.getGender() + "," + this.getPassword();
		return str;

	}

}
