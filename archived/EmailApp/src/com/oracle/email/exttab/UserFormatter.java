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
package com.oracle.email.exttab;

import com.oracle.email.to.UserTO;

import oracle.kv.KVStore;
import oracle.kv.exttab.TableFormatter;
import oracle.kv.table.Row;

/**
 *  This class is used to format the User table row that has 'name' as the nested field:
 *   {"first":"foo","middle":"","last":"bar"} and because of the nested
 *  field we need to write our own custom formatter. For other tables like user.folder or sequence etc we can
 *  use out of the box formatter as they have simple data types only.
 * 
 *
 */
public class UserFormatter implements TableFormatter {
	
	public UserFormatter() {
		super();
	}

	@Override
	public String toOracleLoaderFormat(Row row, KVStore kvStore) {
		
		String rowStr = null;
		UserTO userTO = null;
		
		if(row!=null){
			userTO = new UserTO(row);
			if(userTO != null){
				rowStr = userTO.toString();
			}
		}
		return rowStr;
	}//toOracleLoaderFormat
	
	
}//UserFormatter
