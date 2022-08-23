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
package com.oracle.email.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oracle.kv.FaultException;
import oracle.kv.Version;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableIterator;

import com.oracle.email.constant.table.Message;
import com.oracle.email.constant.table.User;
import com.oracle.email.constant.table.Folder;
import com.oracle.email.constant.table.UserFolderMessage;
import com.oracle.email.exception.DAOException;
import com.oracle.email.to.MessageTO;
import com.oracle.email.util.StringUtil;

public class UserFolderMessageDAO extends BaseDAO {

	private Table messageTable = null;

	public UserFolderMessageDAO() {
		messageTable = getTable(UserFolderMessage.TABLE_NAME);

	}

	@Override
	/**
	 *  This method would create USER.FOLDER.MESSAGE table in the database.
	 */
	public void createTable() {
		String tableStr = "CREATE TABLE IF NOT EXISTS users.folder.message ("
								+ "messageId STRING, "
								+ "read BOOLEAN DEFAULT FALSE, "
								+ "PRIMARY KEY (messageId)"
							+ ")";
		
		
		try {
			//create table
			//getTableAPI().executeSync(tableStr);
			getKVStore().executeSync(tableStr);
			
			System.out.println("USERS.FOLDER.MESSAGE table created...");
		} catch (IllegalArgumentException e) {
			System.out.println("The statement is invalid: " + e);
		} catch (FaultException e) {
			System.out.println("There is a transient problem, retry the "
					+ "operation: " + e);
		}// try
		
	}//createTable
	protected boolean deleteUserMessage(String userId, String folderId,
			String messageId) throws DAOException {

		boolean status = false;
		PrimaryKey key = null;

		if (StringUtil.isNotEmpty(userId) && StringUtil.isNotEmpty(folderId)
				&& StringUtil.isNotEmpty(messageId)) {

			// Get unique row
			key = messageTable.createPrimaryKey();
			key.put(Message.MESSAGEID, messageId);
			key.put(User.USERID, userId);
			key.put(Folder.FOLDERID, folderId);

			status = getTableAPI().delete(key, null, null);
		}

		return status;
	}// deleteUserMessage

	protected String addUserMessage(String userId, String folderName,
			String messageId) throws DAOException {

		Version version = null;
		Row messageRow = null;

		if (StringUtil.isNotEmpty(userId) && StringUtil.isNotEmpty(folderName)
				&& StringUtil.isNotEmpty(messageId)) {

			//System.out.println("userId: " + userId + " folderId: " + folderId + " messageId: " + messageId);
			
			messageRow = this.toUserMessageRow(userId, folderName, messageId);

			// check if conversion was successful
			if (messageRow != null) {

				// System.out.println("messageJSON: " +
				// messageRow.toJsonString(true));

				// Insert message profile into the database.
				version = getTableAPI().putIfAbsent(messageRow, null, null);

				if (version == null) {
					throw new DAOException("Message with messageId: "
							+ messageId + " in folder: " + folderName
							+ " already exist for the user: " + userId);
				}
			}// if(messageRow != null)
		}// if (version == null)// if(row !=null){

		return messageId;
	}// addUserMessage

	protected String moveUserMessage(String userId, String messageId,
			String fromFolder, String toFolder) throws DAOException {

		return null;
	}

	/**
	 * Method returns the list of messageIds when userId and folderId is passed as an input argument.
	 * In business sense when you need all the messages in a folder 'folderId' for user 'userId' you 
	 * would use this method.
	 * 
	 * @param userId
	 *            - unique key of User table
	 * @param folderName
	 *            - unique key of folder table
	 * @return List of MessageTO for a user by userId and in the folder by folderId
	 */
	public List<MessageTO> getMessageIds(String userId, String folderName) {

		List<MessageTO> messageTOs = new ArrayList<MessageTO>();

		List<Row> messages = null;
		String messageId = null;
		boolean read = false;
		MessageTO messageTO = null;

		// Get User by userId
		PrimaryKey key = messageTable.createPrimaryKey();
		key.put(User.USERID, userId);
		key.put(Folder.NAME, folderName);

		messages = getTableAPI().multiGet(key, null, null);

		// convert row into categoryTO
		for (Row row : messages) {
			// System.out.println(row.toJsonString(false));
			messageId = row.get(Message.MESSAGEID).asString().get();
			read = row.get(UserFolderMessage.READ).asBoolean().get();

			// create a new MessageTO
			messageTO = new MessageTO();
			messageTO.setMessageId(messageId);
			messageTO.setRead(read);

			// now set messageTO to the list
			messageTOs.add(messageTO);
		}// for

		return messageTOs;

	}// getMessageIds

	private Row toUserMessageRow(String userId, String folderName,
			String messageId) {

		Row row = null;

		// Create User Row
		row = messageTable.createRow();

		row.put(Message.MESSAGEID, messageId);
		row.put(User.USERID, userId);
		row.put(Folder.NAME, folderName);
		row.put(UserFolderMessage.READ, false);

		return row;

	} // toUserFolderMessageRow(messageTO)
	
	

		
		
		
	}


