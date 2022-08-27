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
package com.oracle.email.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.List;

import com.oracle.email.constant.table.Folder;
import com.oracle.email.dao.MessageDAO;
import com.oracle.email.dao.UserDAO;
import com.oracle.email.exception.DAOException;
import com.oracle.email.to.MessageTO;
import com.oracle.email.to.UserTO;
import com.oracle.email.util.StringUtil;

public class DisplayItem {

	private static UserDAO userDAO = new UserDAO();
	private static MessageDAO messageDAO = new MessageDAO();
	
	private static String action = null;
	private static BufferedReader br = new BufferedReader(new InputStreamReader(
			System.in));

	public static void displayMessages(String folderName,
			List<MessageTO> messageList) {
		String recepientTag = "From";
		String recepient = null;
		int count = 0;
		
		if(messageList !=null) {

		if (Folder.SENT.equals(folderName))
			recepientTag = "To";

		System.out.println(folderName);
		System.out.println("\t" + "ID" + "\t " + "Subject" + "\t "
				+ recepientTag + "\t\t\t " + "Date");
		for (MessageTO messTO : messageList) {

			// Display only top 10 emails per folder
			if (++count >= 10)
				break;

			if (Folder.SENT.equals(folderName))
				// TODO - you can also add CC & BCC list here
				recepient = messTO.getTo();
			else
				recepient = messTO.getFromId();

			System.out.println("\t" + messTO.getMessageId() + "\t "
					+ StringUtil.formatString(messTO.getSubject(), 10) + "\t "
					+ StringUtil.formatString(recepient, 16) + "\t "
					+ messTO.getDateString());
		}
		} else {
			System.out.println("\t" + "No new messages to display");
			
		}
		// EOF for
	}// displayMessages

	public static void showAccounts() {
		List<UserTO> userList = userDAO.getUsers();

		int count = 0;
		String name = null;
		String email = null;
		if (userList != null && userList.size() > 0)
			for (UserTO userTO : userList) {
				name = StringUtil.formatString(userTO.getFirst() + " " + userTO.getMiddle() + " "
						+ userTO.getLast(), 24);
				email = StringUtil.formatString(userTO.getEmail() + ",", 30);
				System.out.println(++count + "\t Email:" + email + "\t Name:"
						+ name);
			}// EOF for
		else
			System.out.println("No account exist yet");
	}// showAccounts

	public static void showEmails(String userId) {
		List<MessageTO> messageList = null;

		// get messages for userId
		Hashtable<String, List<MessageTO>> messageHash = messageDAO
				.getMessages(userId);

		// get Inbox
		messageList = messageHash.get(Folder.INBOX);
		DisplayItem.displayMessages(Folder.INBOX, messageList);

		// get Sent
		messageList = messageHash.get(Folder.SENT);
		DisplayItem.displayMessages(Folder.SENT, messageList);

		// get Trash
		messageList = messageHash.get(Folder.TRASH);
		DisplayItem.displayMessages(Folder.TRASH, messageList);

		System.out
				.println("\n================================================================================");
		System.out
				.println("Press 0=Get Mail, 1=Write, 2=Read, 3=Delete, 4=Logout\n");

	}// showEmails

	public static void showEmail(String messageId) {

		MessageTO messageTO = null;

		messageTO = messageDAO.getMessage(messageId);

		if (messageTO != null) {

			System.out.println("Date: " + messageTO.getDateString());
			System.out.println("From: " + messageTO.getFromId());
			System.out.println("To: " + messageTO.getTo());
			System.out.println("Cc: " + messageTO.getCc());
			System.out.println("Bcc: " + messageTO.getBcc());
			System.out.println("Subject: " + messageTO.getSubject());
			System.out.println(new String(messageTO.getBody()));

			System.out
					.println("\n================================================================================");
			System.out
					.println("Press 1=Reply, 2=Reply All, 3=Forward, 4=Back\n");
		}

	}// showEmail

	public static void replyEmail(String messageId, String from) throws IOException, DAOException {

		MessageTO messageTO = null;
		String body = null;
		String to = null;		
		
		messageTO = messageDAO.getMessage(messageId);

		if (messageTO != null) {

			body = getMessageRepliedBody(messageTO);

			// Add reply to string to the subject
			messageTO.setSubject("RE: " + messageTO.getSubject());

			//Reply should be to the sender 
			to = messageTO.getFromId();
			//empty the Cc & Bcc list
			messageTO.setCc(null);
			messageTO.setBcc(null);
			
			System.out.println("From: " + from);
			System.out.println("To: " + to );
			System.out.println("Cc: ");
			System.out.println("Bcc: ");
			System.out.println("Subject: " + messageTO.getSubject());
			System.out.println("New Message: [type it]");
			
			// Read user input
			action = br.readLine();
			
			//Add this on top of last body
			body = action + "\n" + body;
			
			messageTO.setBody(body.getBytes());
			
			//change from and to
			messageTO.setFromId(from);
			messageTO.setTo(to);
			
			//send message
			messageDAO.addMessage(messageTO);
		}//EOF if

	}// replyEmail

	public static void replyAllEmail(String messageId, String from) throws IOException, DAOException {

		MessageTO messageTO = null;
		String body = null;
		String to = null;		
		
		messageTO = messageDAO.getMessage(messageId);

		if (messageTO != null) {

			body = getMessageRepliedBody(messageTO);

			// Add reply to string to the subject
			messageTO.setSubject("RE: " + messageTO.getSubject());

			//Reply should be to the sender 
			to = messageTO.getTo() + "," + messageTO.getFromId();
			
			//empty the Bcc list only			
			messageTO.setBcc(null);
			
			System.out.println("From: " + from);
			System.out.println("To: " + to );
			System.out.println("Cc: " + messageTO.getCc());
			System.out.println("Bcc: ");
			System.out.println("SubJect: " + messageTO.getSubject());
			System.out.println("New Message: [type it]");
			
			// Read user input
			action = br.readLine();
			
			//Add this on top of last body
			body = action + "\n" + body;
			
			messageTO.setBody(body.getBytes());
			
			//change from and to
			messageTO.setFromId(from);
			messageTO.setTo(to);
			
			//send message
			messageDAO.addMessage(messageTO);
		}//EOF if

	}// replyEmail
	
	public static void forwardEmail(String messageId, String from) throws IOException, DAOException {

		MessageTO messageTO = null;
		String body = null;
		String to = null;	
		String cc = null;
		String bcc = null;
		
		messageTO = messageDAO.getMessage(messageId);

		if (messageTO != null) {

			body = getMessageRepliedBody(messageTO);

			// Add reply to string to the subject
			messageTO.setSubject("Fwd: " + messageTO.getSubject());
				
			System.out.println("* To : ");
			to = br.readLine();
			System.out.println("CC : ");
			cc = br.readLine();
			System.out.println("BCC : ");
			bcc = br.readLine();
			
			//set user values to messageTO
			messageTO.setBcc(bcc);
			messageTO.setCc(cc);
			messageTO.setTo(to);
			
			System.out.println("Subject: " + messageTO.getSubject());
			System.out.println("New Message: [type it]");
			
			// Read user input
			action = br.readLine();
			
			//Add this on top of last body
			body = action + "\n" + body;
			
			messageTO.setBody(body.getBytes());
			
			//change from and to
			messageTO.setFromId(from);
			messageTO.setTo(to);
			
			//send message
			messageDAO.addMessage(messageTO);
		}//EOF if

	}// forwardEmail
	
	private static String getMessageRepliedBody(MessageTO messageTO) {
		String body = "\n--- On " + messageTO.getDateString() + "," + messageTO.getFromId() + " wrote: " + " -----------------------\n";
		body+= new String(messageTO.getBody());

		return body;
	}//getBody

	public static void main(String[] args){
		DisplayItem.showAccounts();
	}//main
	
}
