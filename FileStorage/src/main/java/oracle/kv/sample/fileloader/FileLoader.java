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
package oracle.kv.sample.fileloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;

import oracle.kv.Consistency;
import oracle.kv.Direction;
import oracle.kv.sample.common.BaseLoader;
import oracle.kv.sample.common.util.FileUtil;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.TableIterator;
import oracle.kv.table.TableIteratorOptions;

/**
 * The Main Loader Class. This class will load all the data from the TXT file
 * passed as argument into a table specified in the config.properties
 * 
 * @author vsettipa
 * 
 */
public class FileLoader extends BaseLoader {
    
    private String id;
    private String fileOwner;
    private String fileDate;
    private byte[] binaryFile;
    private Row row;
    private File dir;
    
    /**
     * Constructor
     * 
     * @param args
     */
    public FileLoader(String[] args) {
	super(args);
    }
    
    /**
     * This method performs validation on the input arguments. If this method
     * fails then program exits.
     * 
     * @return true if there is no validation errors otherwise false.
     */
    public boolean validate() {
	boolean flag = true;
	
	// arguments that are required.
	if (inputPathStr != null) {
	    if (!FileUtil.isValidPath(inputPathStr)) {
		flag = false;
		logError("Input directory argument: '-i' can not be empty.");
	    }
	}
	return flag;
    }
    
    /**
     * After successful validation this method is run to load the content of a
     * TXT file into the given table
     * 
     * @throws IOException
     */
    private void loadData() throws IOException {
	if (inputPathStr != null) {
	    dir = new File(inputPathStr);
	    File file = null;
	    File[] files = null;
	    int len = 0;
	    
	    // If input path is a directory then load data from all the files
	    // that are under the directory. Make sure all the files are of same
	    // type
	    // i.e. TXT (mix and match is not allowed)
	    if (dir.exists()) {
		System.out.println("There are " + dir.listFiles().length
			+ " to be loaded.");
		if (dir.isDirectory()) {
		    files = dir.listFiles();
		    len = files.length;
		    
		    // loop through all the files and load the content one by
		    // one
		    for (int i = 0; i < len; i++) {
			file = files[i];
			try {
			    Path filePath = Paths.get(file.getPath());
			    BasicFileAttributes attr = Files.readAttributes(
				    filePath, BasicFileAttributes.class,
				    LinkOption.NOFOLLOW_LINKS);
			    DateFormat formatter = new SimpleDateFormat(
				    "MM/dd/yyyy");
			    FileOwnerAttributeView fileOwnerAttributeView = Files
				    .getFileAttributeView(filePath,
					    FileOwnerAttributeView.class);
			    UserPrincipal userPrincipal = fileOwnerAttributeView
				    .getOwner();
			    
			    id = Integer.toString(i);
			    fileDate = formatter
				    .format(attr.lastAccessTime().toMillis());
			    fileOwner = userPrincipal.getName();
			    binaryFile = FileUtils.readFileToByteArray(file);
			    
			    row = table.createRow();
			    row.put("id", id);
			    row.put("date", fileDate.toString());
			    row.put("owner", fileOwner);
			    row.put("file", binaryFile);
			    
			    tableh.put(row, null, null);
			} catch (Exception e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
		    }
		} else {
		    System.out.println("There are no Files to Load");
		}
	    }
	}
	
	if (fileId != null) {
	    getData();
	}
    }
    
    /**
     * this method will be called if there is a bulk get call
     */
    private void getData() {
	
	System.out.println("Get File");
	final int maxConcurrentRequests = 9;
	final int batchResultsSize = 0;
	// Direction - UNORDERED - Iterate in no particular key order.
	// CONSISTENCY - NONE_REQUIRED - A consistency policy that lets a
	// transaction on a replica using this policy proceed regardless of the
	// state of the Replica relative to the Master.
	final TableIteratorOptions tio = new TableIteratorOptions(
		Direction.UNORDERED, Consistency.NONE_REQUIRED, 0, null,
		maxConcurrentRequests, batchResultsSize);
	
	// Create Primary key with the value passed as argument
	PrimaryKey myKey = table.createPrimaryKey();
	
	myKey.put("id", fileId);
	
	// Create the table iterator and pass te primary Key, Muti Row Options
	// and Table Iterator options.
	final TableIterator<Row> iterator = tableh.tableIterator(myKey, null,
		tio);
	
	// Now retrieve the records.
	try {
	    while (iterator.hasNext()) {
		Row row = (Row) iterator.next();
		File newFile = new File(dir + "/" + fileId + ".pdf");
		try {
		    FileUtils.writeByteArrayToFile(newFile,
			    row.get("file").asBinary().get());
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	} finally {
	    // iterator close
	    if (iterator != null) {
		iterator.close();
	    }
	}
    }
    
    public static void main(String[] args) {
	FileLoader loader = new FileLoader(args);
	try {
	    loader.loadData();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
    }
}