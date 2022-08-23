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
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import oracle.kv.Direction;
import oracle.kv.FaultException;
import oracle.kv.Version;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableIterator;
import oracle.kv.table.TableIteratorOptions;

import com.oracle.email.constant.table.Folder;
import com.oracle.email.constant.table.Message;
import com.oracle.email.exception.DAOException;
import com.oracle.email.to.FolderTO;
import com.oracle.email.to.MessageTO;
import com.oracle.email.to.UserTO;
import com.oracle.email.util.SequenceUtil;
import com.oracle.email.util.StringUtil;

public class MessageDAO extends BaseDAO {

	private Table messageTable = null;
	private List<String> recipientList = new ArrayList<String>();
	private UserDAO userDAO = null;
	private UserFolderMessageDAO userMessageDAO = null;
	private UserFolderDAO userFolderDAO = null;

	public MessageDAO() {
		messageTable = getTable(Message.TABLE_NAME);
		userDAO = new UserDAO();
		userMessageDAO = new UserFolderMessageDAO();
		userFolderDAO = new UserFolderDAO();
	}

	@Override
	/**
	 *  This method would create MESSAGE table in the database.
	 */
	public void createTable() {
		String tableStr = "CREATE TABLE IF NOT EXISTS message ("
								+ "messageId STRING, "
								+ "toId STRING, "
								+ "fromId STRING, "
								+ "cc STRING, "
								+ "bcc STRING, "
								+ "subject STRING, "
								+ "body BINARY, "
								+ "attachment BINARY, "
								+ "createdOn LONG, "
								+ "modifiedOn LONG, "
								+ "PRIMARY KEY (messageId,createdOn)"
							+ ")";
		
		
		try {
			//create table
			//getTableAPI().executeSync(tableStr);
			getKVStore().executeSync(tableStr);
			
			System.out.println("MESSAGE table created...");
		} catch (IllegalArgumentException e) {
			System.out.println("The statement is invalid: " + e);
		} catch (FaultException e) {
			System.out.println("There is a transient problem, retry the "
					+ "operation: " + e);
		}// try
		
	}//createTable
	
	/**
	 * This method takes a messageTO and persist it into the Message table.
	 * After that for all TO, CC, BCC it sets the email to their INBOX folder
	 * and for the sender it sets the message to SENT folder.
	 * 
	 * @param messageTO
	 *            - with all the information about the message
	 * @return - unique messageId
	 * @throws DAOException if message fail to be persisted 
	 */
	public String addMessage(MessageTO messageTO) throws DAOException {

		Long timestamp = new Date().getTime();
		Version version = null;
		Row messageRow = null;
		String messageId = null;

		if (messageTO != null) {

			//Create Message Row object from TO 
			messageRow = this.toMessageRow(messageTO);
			
			
			

			// check if conversion was successful
			if (messageRow != null) {

				// get a unique messageId
				messageId = SequenceUtil.getNext(Message.TABLE_NAME);

				messageRow.put(Message.MESSAGEID, messageId);
				messageRow.put(Message.CREATEDON, timestamp);
				messageRow.put(Message.MODIFIEDON, timestamp);

				//System.out.println("messageJSON: " + messageRow.toJsonString(true));

				// Insert message profile into the database.
				version = getTableAPI().putIfAbsent(messageRow, null, null);

				if (version == null) {
					throw new DAOException("Message with same : Key /'Message/"
							+ messageId + "' already exist in database. ");
				} else {
					// get email id from TO, CC & BCC
					this.recipientList = this.getRecipients(messageTO);

					// Add message to INBOX folder for all the above recipients
					this.addUserMessage(recipientList, Folder.INBOX, messageId);

					// Also add message to sender's sent folder
					this.addUserMessage(messageTO.getFromId(), Folder.SENT,
							messageId);

					System.out
							.println("Email succesfully sent to all recipients.");

				}// if(email != null)
			}// if(messageRow != null)
		}// if (version == null)// if(row !=null){

		return messageId;
	}// addMessage

	/**
	 * Method to persist User to Folder to MessageId relationship. This
	 * relationship is maintained in User.Folder.Message table so this method
	 * writes a record for each of the recipients mentioned in the recipientList
	 * 
	 * @param recipientList
	 *            - List with email addresses of all the recipients
	 * @param folderName
	 *            - Where this message should be saved (Inbox, Sent, Trash)
	 * @param messageId
	 *            - The real message is persisted under Message table but Id of
	 *            that message is tagged to all the recipients mail folders.
	 * @throws DAOException
	 */
	private void addUserMessage(List<String> recipientList, String folderName,
			String messageId) throws DAOException {
		Hashtable<String, String> emailHash = new Hashtable<String, String>();

		if (recipientList != null && StringUtil.isNotEmpty(messageId)
				&& StringUtil.isNotEmpty(folderName)) {
			for (String email : recipientList) {
				// if email has not been sent already to recipient then only
				// send email
				if (!emailHash.contains(email)) {
					// add the email to hash first
					emailHash.put(email, email);

					// insert row in User.Folder.Message table
					this.addUserMessage(email, folderName, messageId);
				}//
			}// for
		}// if
	}// addUserMessage

	/**
	 * Overloaded method that instead of taking a list of email ids take a
	 * single email, get the userId using that email and then insert the row in
	 * User.Folder.Message table.
	 * 
	 * @param email
	 *            - email of the user
	 * @param folderName
	 *            - unique folder name where the message belongs
	 * @param messageId
	 *            - primary key of the message
	 * @throws DAOException
	 */
	private void addUserMessage(String email, String folderName,
			String messageId) throws DAOException {
		String userId = null;
		UserTO userTO = null;

		userTO = userDAO.getUserByEmail(email);

		if (userTO != null) {
			userId = userTO.getUserId();
			//folderId = userFolderDAO.getFolderId(userId, folderName);

			// Add message for userId, messageId & folderName
			userMessageDAO.addUserMessage(userId, folderName, messageId);

		}// if(userTO != null)
	}// addUserMessage

	/**
	 * This method fetches list of recipient's email from TO, CC & BCC list and
	 * parses them as they could be comma delimited strings to create a list of
	 * email ids.
	 * 
	 * @param messageTO
	 * @return List of email id that happens to be in TO, CC, BCC list
	 */
	private List<String> getRecipients(MessageTO messageTO) {

		String cc = null;
		String bcc = null;
		String to = null;

		if (messageTO != null) {
			cc = messageTO.getCc();
			to = messageTO.getTo();
			bcc = messageTO.getBcc();
			// Add all the recipients in one list
			recipientList.addAll(this.parseEmails(cc));
			recipientList.addAll(this.parseEmails(to));
			recipientList.addAll(this.parseEmails(bcc));
		}// EOF if

		return recipientList;

	}// getRecipientList

	/**
	 * TO, CC & BCC are strings that contains email id of many recipients with
	 * comma delimiter. This method parse that string and constructs a list of
	 * all the emails of all the recipients present in the emailString.
	 * 
	 * @param emailString
	 * @return
	 */
	private List<String> parseEmails(String emailString) {
		StringTokenizer st = null;
		List<String> emailList = new ArrayList<String>();
		if (StringUtil.isNotEmpty(emailString)) {
			st = new StringTokenizer(emailString, ",");
			while (st.hasMoreTokens()) {
				emailList.add(st.nextToken());
			}// EOF while

		}// if
		return emailList;

	}// parseEmails

	/**
	 * To delete a message using the primary key.
	 * 
	 * @param messageId
	 *            - primary key of Message table
	 * @return - true when successful otherwise false
	 */
	public boolean deleteMessage(String messageId) {
		boolean status = false;
		// Get User by userId
		PrimaryKey key = messageTable.createPrimaryKey();
		key.put(Message.MESSAGEID, messageId);
		status = getTableAPI().delete(key, null, null);
		return status;
	}// deleteMessage

	/**
	 * This method returns message using the primary key messageId
	 * 
	 * @param messageId
	 *            - primary key of the Message table
	 * @return MessageTO with all the information about the message.
	 */
	public MessageTO getMessage(String messageId) {
		MessageTO messageTO = null;
		
		
		PrimaryKey msgpk = messageTable.createPrimaryKey();
		msgpk.put("messageId", messageId);

		TableIteratorOptions tio = new TableIteratorOptions(
				Direction.FORWARD, null, 0, null);
		TableIterator<Row> msgiter = getTableAPI().tableIterator(msgpk, null,
				tio);
		while (msgiter.hasNext()) {
			Row msgrow = msgiter.next();
			messageTO = this.toMessageTO(msgrow);
			//System.out.println(msgrow.toJsonString(false));
		}


	

		return messageTO;
	}

	/**
	 * This method returns all the messages for userId ordered by folder name
	 * and sorted by date
	 * 
	 * @param userId
	 *            - primary key of user table
	 * @return Hashtable with key as folderName and value as list of messages
	 *         ordered by date
	 */
	public Hashtable<String, List<MessageTO>> getMessages(String userId) {
		Hashtable<String, List<MessageTO>> messageHash = new Hashtable<String, List<MessageTO>>();
		List<MessageTO> folderMessageList = null;
		List<MessageTO> messageTOs = null;
		MessageTO newMessageTO = null;
		List<FolderTO> userFolders = null;
		//String folderId = null;
		String folderName = null;

		userFolders = userFolderDAO.getFolders(userId);
		// Iterate through all the folders that belong to a user by userID
		if (userFolders != null)
			for (FolderTO folderTO : userFolders) {
				//folderId = folderTO.getFolderId();
				folderName = folderTO.getName();
				if (StringUtil.isNotEmpty(folderName)) {
					// initialize folderMessages
					folderMessageList = new ArrayList<MessageTO>();
					// Now set this list in the hash with folderName as the key
					messageHash.put(folderName, folderMessageList);

					messageTOs = userMessageDAO.getMessageIds(userId, folderName);
					// iterate through all the messageIds for each folderId
					if (messageTOs != null)
						for (MessageTO messageTO : messageTOs) {
							newMessageTO = this.getMessage(messageTO
									.getMessageId());
							newMessageTO.setRead(messageTO.isRead());

							// add the folder message to folderMessages
							folderMessageList.add(newMessageTO);
						}// for

					// sort the mail list by date
					Collections.sort(folderMessageList);

				}// EOF if(StringUtil.isNotEmpty
			}// for

		return messageHash;
	}// getMessages

	private Row toMessageRow(MessageTO messageTO) {

		Row row = null;

		if (messageTO != null) {

			// Create User Row
			row = messageTable.createRow();
			
			// lower case the email as that is case insensitive
			row.put(Message.MESSAGEID, messageTO.getMessageId());
			row.put(Message.FROM, messageTO.getFromId());
			row.put(Message.SUBJECT, messageTO.getSubject());
			row.put(Message.BODY, messageTO.getBody());
			row.put(Message.TO, messageTO.getTo());

			if (StringUtil.isNotEmpty(messageTO.getCc()))
				row.put(Message.CC, messageTO.getCc());
			if (StringUtil.isNotEmpty(messageTO.getBcc()))
				row.put(Message.BCC, messageTO.getBcc());
			if (messageTO.getAttachment() != null)
				row.put(Message.ATTACHMENT, messageTO.getAttachment());

			row.put(Message.CREATEDON, messageTO.getCreatedOn());
			row.put(Message.MODIFIEDON, messageTO.getModifiedOn());

		} // if (userTO != null) {

		return row;

	} // toMessageRow(messageTO)

	private MessageTO toMessageTO(Row row) {

		MessageTO messageTO = null;

		// Extract user information
		if (row != null) {

			messageTO = new MessageTO();
			// Now add field values
			messageTO.setMessageId(row.get(Message.MESSAGEID).asString().get());
			messageTO.setTo(row.get(Message.TO).asString().get());
			messageTO.setFromId(row.get(Message.FROM).asString().get());
			messageTO.setSubject(row.get(Message.SUBJECT).asString().get());
			messageTO.setBody(row.get(Message.BODY).asBinary().get());

			messageTO.setCc(row.get(Message.CC).isNull()?null:row.get(Message.CC).asString().get());
			messageTO.setBcc(row.get(Message.BCC).isNull()?null:row.get(Message.BCC).asString().get());
			messageTO.setAttachment(row.get(Message.ATTACHMENT).isNull()?null:row.get(Message.ATTACHMENT).asBinary()
					.get());

			messageTO.setCreatedOn(row.get(Message.CREATEDON).asLong().get());
			messageTO.setModifiedOn(row.get(Message.MODIFIEDON).asLong().get());

		} // if (row != null)

		return messageTO;
	} // toMessageTO

	public static void main(String[] args) {
		MessageDAO messageDAO = new MessageDAO();
		MessageTO messageTO = new MessageTO();
		String userId = "1";

		messageTO.setFromId("anuj.sahni@email.com");
		messageTO.setTo("Mike.smith@email.com");
		// messageTO.setBcc("mike.a.smith@email.com");
		// messageTO.setCc("mike.b.smith@email.com");

		messageTO.setSubject("Hello Mike");
		messageTO.setBody("How's everything there".getBytes());
		// messageTO.setAttachment("No Attachment".getBytes());

		try {
			messageDAO.addMessage(messageTO);
			messageDAO.getMessages(userId);
		} catch (DAOException e) {
			e.printStackTrace();
		}

	}// main

}// MessageDAO
