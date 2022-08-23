/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2020 Oracle and/or its affiliates.  All rights reserved.
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

package loganalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.StatementResult;
import oracle.kv.table.Row;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Table;
import oracle.kv.table.TableIterator;
import oracle.kv.table.TableAPI;

/**
 * A utility to push the log information into Oracle NoSQL Database. 
 * This reads log files from a specified disk location and processes
 * each of the log files.
 *
 */
public class PushLogs{

    static int counter = 0;
    static String[] kvhosts = {"localhost:5000"};
    static final String STORE_NAME = "kvstore";
  
    static List<Path> listFiles(Path path) throws IOException{
  
        List<Path> files = null;
        files = Files.walk(path)
                     .filter(p -> Files.isRegularFile(p, 
		                                      LinkOption.NOFOLLOW_LINKS))
  		     .collect(Collectors.toList());
    
        return files;
    }
  
    static List<Path> getLogFiles(Path path) throws IOException{
        // E.g.: rg2-rn2_0.log
        final String pattern = "rg(.*)\\.log$";
        final Pattern r = Pattern.compile(pattern);
    
        List<Path> allFiles = listFiles(path);
        List<Path> logFiles = new ArrayList<Path>();
        for (Path entry: allFiles){
            String name = entry.getFileName().toString();
            Matcher m = r.matcher(name);
            if(m.find()){
                logFiles.add(entry);
            }
        }
        return logFiles;
    }
  
    static long dateToMillis(String dateStr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS z");
        try{
            Date date = sdf.parse(dateStr);
            return date.getTime();
        }catch(Exception e){
            System.out.println("dateToMillis: Unable to convert " + dateStr + 
                               " to date");
            System.out.println(e);
            return -1;
        }
    }
  
    public static void processFile(Path path, TableAPI tableH){
        Table logTable = null;
        System.out.println("processing file:" + path);
    
        try{
            logTable = tableH.getTable("Log");
        }catch(FaultException e){
            System.out.println("Unable to get table metadata");
            e.printStackTrace();
            return;
        }
  
        try{
            BufferedReader br = new BufferedReader(new FileReader(path.toString()));
            String line = null;
            while((line = br.readLine()) != null){
      
  	        // If the first field is not timestamp ignore that line.
  	        if(line.startsWith("201") == false)
  	            continue;
  
  	        String[] tokens = line.split("\\s");
  	        if(tokens.length < 5)
  	            continue;
  
  	        String ts = tokens[0] + " " + tokens[1] + " " + tokens[2];
  	        String logLevel = tokens[3];
  	        String host = tokens[4];
    
  	        // remove the enclosing square brackets.
  	        host = host.substring(1, host.length()-1);
  	        String mesg = "";
  	        for(int i = 5; i < tokens.length; i++){ 
  	            if (i > 5)
  	                mesg += " ";
          
  	            mesg += tokens[i];
  	        }
  
  	        Row row = logTable.createRow();
  
  	        // Assuming all the logs will be processed in a single run.
  	        counter++;
  	        row.put("id", counter); 
          
  	        row.put("tsMillis", dateToMillis(ts));
  	        row.putEnum("logLevel", logLevel);
  	        row.put("host", host);
  	        row.put("message", mesg);
  	        tableH.put(row, null, null);
            }
  
            System.out.println("Total number of log entries processed:" + 
	                       counter);
        }catch(IOException e){
            System.out.println("Error while reading log entries.");
            e.printStackTrace();
            return;
        }catch(FaultException e){
            System.out.println("Error inserting row");
            e.printStackTrace();
            return;
        }catch(IllegalArgumentException e){
            System.out.println("Invalid or incomplete prinary key given");
            e.printStackTrace();
        }
    }
  
    public static boolean createTable(KVStore kvstore){
  
  
        final TableAPI tableAPI = kvstore.getTableAPI();
        StatementResult result = null;
        String statement = null;
    
        try {
            statement =
              "CREATE TABLE IF NOT EXISTS Log(" +
    	      "id INTEGER," +
              "tsMillis LONG," +
              "logLevel ENUM(INFO, SEVERE, WARNING)," +
              "host STRING," +
              "message STRING," +
              "PRIMARY KEY (id))";
      
            result = kvstore.executeSync(statement);
    
        }catch(IllegalArgumentException e){
            System.out.println("Invalid statement:\n" + e.getMessage());
            return false;
        }catch(FaultException e){
            System.out.println("Statement couldn't be executed, please retry: " + e);
            return false;
        }
    
        return true;
    }
  

    public static void main(String[] args){
        if(args.length < 1){
            System.out.println("Usage: java PushLogs <Log directory>");
            System.exit(0);
        }
    
        final KVStoreConfig kvconfig = new KVStoreConfig(STORE_NAME, kvhosts);
    
        final KVStore kvstore = KVStoreFactory.getStore(kvconfig);
        final TableAPI tableH = kvstore.getTableAPI();
    
        if(createTable(kvstore) == false){
            System.exit(1);
        }
    
        String dir = args[0];
    
        List<Path> files = null;
    
        try{
            Path path = FileSystems.getDefault().getPath(dir);
            files = getLogFiles(path);
            System.out.println("number of log files: " + files.size());
        }catch(InvalidPathException e){
            System.out.println("Invalid path: " + dir);
            System.exit(1);
        }catch(IOException e){
            System.out.println("Error getting log files");
            System.exit(1);
        }
    
        for(Path f: files){
            processFile(f, tableH);
        }
    
        kvstore.close();
    }
}
