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

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import com.oracle.fleet.util.StringUtil;

public abstract class BaseTO {

	private static JsonNodeFactory factory = null;
	private static ObjectMapper jsonMapper = null;

	public BaseTO() {
		super();
		if (factory == null) {
			factory = JsonNodeFactory.instance;
			jsonMapper = new ObjectMapper();
		}// EOF if
	}// BaseTO

	protected ObjectNode getObjectNode() {
		return new ObjectNode(factory);
	} // getObjectNode

	protected ArrayNode getArrayNode() {
		return new ArrayNode(factory);
	} // getArrayNode
	
	
	protected ObjectNode parseJson(String jsonTxt)
			throws JsonProcessingException {
		ObjectNode objectNode = null;
		
		try {
			if (StringUtil.isNotEmpty(jsonTxt))
				objectNode = (ObjectNode) jsonMapper.readTree(jsonTxt);
		} catch (IOException e) {
			// This shouldn't be thrown
			e.printStackTrace();
		}
		return objectNode;
	}// parse

	public abstract String toJsonString();

}// BaseTO

