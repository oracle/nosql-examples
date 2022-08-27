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
import java.util.Arrays;
import java.util.List;

import com.oracle.email.dao.BaseDAO;
import com.oracle.email.dao.MessageDAO;
import com.oracle.email.dao.UserDAO;
import com.oracle.email.exception.DAOException;
import com.oracle.email.to.MessageTO;
import com.oracle.email.to.UserTO;
import com.oracle.email.util.StringUtil;

public class EmailCLI {

	private static final String ZERO = "0";
	private static final String ONE = "1";
	private static final String TWO = "2";
	private static final String THREE = "3";
	private static final String FOUR = "4";

	private static List<String> validActions = null;

	private UserDAO userDAO = new UserDAO();
	private MessageDAO messageDAO = new MessageDAO();
	private BufferedReader br = new BufferedReader(new InputStreamReader(
			System.in));
	private String email = null;

	private void mainMenu() {

		String action = null;

		/*
		 * Collect user input from the console There are 3 valid activities: 1.
		 * Show customer accounts 2. Add new customer account 3. Login
		 */

		validActions = Arrays.asList(ONE, TWO, THREE, FOUR);

		try {
			while (true) {
				action = null;

				System.out.println();
				System.out
						.println("=============================================================");
				System.out.println("What do you want to do?");
				System.out.println("1. Show existing accounts");
				System.out.println("2. Add new account");
				System.out.println("3. Login to your account");
				System.out.println("4. Exit");
				System.out.println();
				System.out.print("Enter action [1]:  ");
				action = br.readLine();

				// default to show customer profile
				if (!validActions.contains(action))
					System.out.println("Not a valid response. Please try again.");

				// Run appropriate method based on the activity selection
				if (action.equals(ONE))					
					DisplayItem.showAccounts();
				else if (action.equals(TWO))
					this.addAccount();
				else if (action.equals(THREE))
					this.emailMainMenu();
				else
					System.exit(0);

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}// mainMenu

	private void emailMainMenu() {
		UserTO userTO = null;
		String userId = null;

		String password = null;
		boolean loop = true;
		int index = -1;

		// Press 0=Get Mail, 1=Write, 2=Read, 3=Delete, 4=Logout
		validActions = Arrays.asList(ZERO, ONE, TWO, THREE, FOUR);
		String action = null;

		try {
			System.out.println("Username [email]: ");
			email = br.readLine();
			System.out.println("Password [****]: ");
			password = br.readLine();

			if (StringUtil.isNotEmpty(email) && StringUtil.isNotEmpty(password)) {

				index = email.indexOf('@');
				// make sure @email.com exist. If not then add that
				email = index < 0 ? email + "@email.com" : email;
				userTO = userDAO.getUser(email, password);
				//System.out.println("userTO2: " + userTO.toString());
				
				if (userTO != null) {

					userId = userTO.getUserId();
					System.out.println("Welcome back " + userTO.getFirst()
							+ "\t");
					// Display user emails
					while (loop) {
						action = null;

						// Show emails that belongs to user
						DisplayItem.showEmails(userId);

						// Read user input
						action = br.readLine();

						// default to show customer profile
						if (!validActions.contains(action))
							System.out.println("Not a valid response. Please try again.");

						// Run appropriate method based on the activity
						// selection
						if (action.equals(ZERO)) {
							// DO NOTHING
						} else if (action.equals(ONE)) {
							//Write
							System.out.println("Anand message : " + email);
							this.addMessage(email);
						} else if (action.equals(TWO)) {
							//Read
							this.responseMenu();
						} else if (action.equals(THREE)) {
							// TODO - Implement Delete functionality 
							System.out
									.println("Functionality Not Yet Implemented");
						} else if (action.equals(FOUR)) {
							//Logout
							loop = false;
						}

					}// EOF while
				} else {
					System.out
							.println("Not a valid email/password combination");
				}// EOF if(userTO!=null)
			}// if(StringUtil.isNotEmpty(email) &&
				// StringUtil.isNotEmpty(password))
		} catch (DAOException de) {
			System.out.println(de.getMessage());
		} catch (IOException de) {
			System.out.println(de.getMessage());
		}
	}// emailMenu

	private void responseMenu() {

		String messageId = null;
		boolean loop = true;

		// 1=Reply, 2=Reply All, 3=Forward, 4=Back
		validActions = Arrays.asList(ONE, TWO, THREE, FOUR);
		String action = null;

		try {
			System.out.println("MessageId [ID]: ");
			messageId = br.readLine();

			if (StringUtil.isNotEmpty(messageId)) {

				// show message
				DisplayItem.showEmail(messageId);

				// Display user emails
				while (loop) {
					action = null;

					// Read user input
					action = br.readLine();

					// default to show customer profile
					if (!validActions.contains(action))
						System.out.println("Not a valid response. Please try again.");

					// Run appropriate method based on the activity
					// selection
					if (action.equals(ONE)) {
						//Reply
						DisplayItem.replyEmail(messageId, email);
						loop = false;
					} else if (action.equals(TWO)) {
						//Reply All
						DisplayItem.replyAllEmail(messageId, email);
						loop = false;
					} else if (action.equals(THREE)) {
						//Forward
						DisplayItem.forwardEmail(messageId, email);
						loop = false;
					} else if (action.equals(FOUR)) {
						loop = false;
					}

				}// EOF while
			} else {
				System.out.println("Not a valid email/password combination");
			}// EOF if(userTO!=null)

		} catch (IOException de) {
			System.out.println(de.getMessage());
		} catch (DAOException de) {
			de.printStackTrace();
		}
	}// emailMenu

	private void addAccount() throws IOException, DAOException {
		String first = null;
		String middle = null;
		String last = null;
		String password = null;
		String gender = null;
		String age = null;
		
		UserTO userTO = new UserTO();

		System.out.println("2. Add new account\n");
		System.out.println("First Name : ");
		first = br.readLine();
		System.out.println("Middle Name : ");
		middle = br.readLine();
		System.out.println("Last Name : ");
		last = br.readLine();
		System.out.println("Password : ");
		password = br.readLine();
		System.out.println("Gender [M/F]: ");
		gender = br.readLine();
		System.out.println("Age : ");
		age = br.readLine();

		// Set inputs to userTO
		userTO.setFirst(first);
		userTO.setMiddle(middle);
		userTO.setLast(last);
		userTO.setGender(gender);
		userTO.setPassword(password);
		userTO.setAge(age);
		
		// add user
		userDAO.addUser(userTO);

	}// addAccount

	private void addMessage(String from) throws IOException, DAOException {
		MessageTO messageTO = new MessageTO();
		String to = null;
		String subject = null;
		String body = null;
		String cc = null;
		String bcc = null;
		
		
		System.out.println("1. Write Email");
		System.out
				.println("Fields with '*' are required and ',' separated email list is allowed.\n");
		System.out.println("* To : ");
		to = br.readLine();
		System.out.println("CC : ");
		cc = br.readLine();
		System.out.println("BCC : ");
		bcc = br.readLine();
		System.out.println("* Subject : ");
		subject = br.readLine();
		System.out.println("* Body : ");
		body = br.readLine();

		if (StringUtil.isNotEmpty(to) && StringUtil.isNotEmpty(subject)
				&& StringUtil.isNotEmpty(body)) {

			// add field values to messageTO
			messageTO.setTo(to);
			messageTO.setFromId(from);
			messageTO.setBcc(bcc);
			messageTO.setCc(cc);
			messageTO.setSubject(subject);
			messageTO.setBody(body.getBytes());

			// insert message to the database
			messageDAO.addMessage(messageTO);
		} else {
			System.out
					.println("Required fields were empty. Try composing the email again");
		}

	}// addMessage

	public static void main(String[] args) {
		int len = args.length;
		String hostPort = "localhost:5000";
		String store = "kvstore";

		// If there are argument passed then consider first one as hostPort and
		// second one
		// as store name
		if (len == 2) {
			hostPort = args[0];
			store = args[1];
		}else if(len==1){
			hostPort = args[0];
		}// EOF if

		BaseDAO.setHostPort(hostPort);
		BaseDAO.setStoreName(store);

		EmailCLI cli = new EmailCLI();
		cli.mainMenu();
	}
}
