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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

import oracle.kv.hadoop.table.TableInputFormat;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * A utility to analyze the log information stored in Oracle NoSQL Database.
 * It uses Apache Spark to do the analysis.
 *
 */
public class LogAnalyzer {

    static final String STORE_NAME = "kvstore";
    static final String TABLE_NAME = "Log";
    static final String KVHOST = "localhost:5000";
  
    public static JavaRDD<Row> filterOnLevel(JavaRDD<Row> logData, 
                                             final String fLevel){
        JavaRDD<Row> levelData = logData.filter(new Function<Row, Boolean>(){
          public Boolean call(Row row){
              String level = row.get("logLevel").asEnum().get();
              return fLevel.equals(level);
          }
        });

        return levelData;
    }

    public static JavaRDD<Row> filterOnHost(JavaRDD<Row> logData, 
                                            final String fHost){
        JavaRDD<Row> hostData = logData.filter(new Function<Row, Boolean>(){
            public Boolean call(Row row){
                String host = row.get("host").asString().get();
                return fHost.equals(host);
            }
        });
    
        return hostData;
    }

    public static JavaRDD<Row> filterOnTime(JavaRDD<Row> logData, 
                                          final String past){
        final long nowMillis = new Date().getTime();
        final long pastMillis = computePastMillis(past);
        
        JavaRDD<Row> timeData = logData.filter(new Function<Row, Boolean>(){
            public Boolean call(Row row){
                long tsMillis = row.get("tsMillis").asLong().get();
                long diffMillis = nowMillis - tsMillis;
                return diffMillis <= pastMillis;
            }
        });
    
        return timeData;
    }

    public static JavaRDD<Row> filterOnKeyword(JavaRDD<Row> logData, 
                                               final String keyword){
        String lKeyword = keyword.toLowerCase();
        JavaRDD<Row> hostData = logData.filter(new Function<Row, Boolean>(){
            public Boolean call(Row row){
                String mesg = row.get("message").asString().get();
                return mesg.toLowerCase().contains(lKeyword);
            }
        });
  
        return hostData;
    }


    public static Map<String, Long> aggregateEntries(JavaRDD<Row> logData){
        JavaRDD<String> noIntMesgs = logData.map(new Function<Row, String>(){
            public String call(Row row){
              return row.get("message").asString().get()
	                               .replaceAll("\\d+", "?");
            }
        });
    
        // Compute distinct counts
        Map<String, Long> counts = noIntMesgs.countByValue();
      
        return counts;
    }


    public void printAggr(Map<String, Long> aggr){
        Comparator<Map.Entry<String, Long>> byValue = 
            (entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue());
        List<Map.Entry<String, Long>> saggr = aggr.entrySet().stream()
	                                          .sorted(byValue.reversed())
						  .collect(Collectors.toList());
    }

    public static long computePastMillis(String past){
        long pastMillis = 0;
        switch(past){
          case "hour":  pastMillis = 1 * 60 * 60 * 1000L;
                        break;
          case "day":   pastMillis = 24 * 60 * 60 * 1000L;
                        break;
          case "week":  pastMillis = 7 * 24 * 60 * 60 * 1000L;
                        break;
          default:      pastMillis = 365 * 24 * 60 * 60 * 1000L;
                        break;
        }
    
        return pastMillis;
    }

    public static void printHelp(){
        System.out.println("Usage: java LogAnalyzer [options]");
        System.out.println("options:");
        System.out.println("  --for-the-past hour|day|week\tprocess logs for the past given time period");
        System.out.println("  --host <hostname>\t\tprocess logs for this host");
        System.out.println("  --level INFO|SEVERE|WARNING\tLog level to consider");
        System.out.println("  --search <keyword>\t\tSearch keyword");
    }
  
    public static void main(String[] args) {
        if(args.length % 2 != 0){
            printHelp();
            System.exit(0);
        }
    
        if(args.length == 1 && "--help".equals(args[0])){
            printHelp();
            System.exit(0);
        }
  
        Logger.getLogger("org").setLevel(Level.ERROR);
        Logger.getLogger("akka").setLevel(Level.ERROR);
        Logger.getLogger("Remoting").setLevel(Level.ERROR);
  
        HashMap<String, String> options = new HashMap<String, String>();
        for(int i = 0; i < args.length/2; i++){
            String option = args[2*i];
            String value = args[2*i + 1];
            System.out.println("option: " + option + " value: " + value);
            options.put(option, value);
        }
  
        SparkConf sparkConf = new SparkConf().setAppName("SparkTableInput")
	                                     .setMaster("local[2]");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        Configuration hconf = new Configuration();
        hconf.set("oracle.kv.kvstore", STORE_NAME);
        hconf.set("oracle.kv.tableName", TABLE_NAME);
        hconf.set("oracle.kv.hosts", KVHOST);
        JavaPairRDD<PrimaryKey,Row> jrdd = 
	    sc.newAPIHadoopRDD(hconf, TableInputFormat.class, 
	                       PrimaryKey.class, Row.class);
    
        JavaRDD<PrimaryKey> rddkeys = jrdd.keys();
        JavaRDD<Row> rddValues = jrdd.values();
      
        if(options.containsKey("--for-the-past")){
            rddValues = filterOnTime(rddValues, options.get("--for-the-past"));
        }
  
        if(options.containsKey("--host")){
            rddValues = filterOnHost(rddValues, options.get("--host"));
        }
  
        if(options.containsKey("--level")){
            rddValues = filterOnLevel(rddValues, options.get("--level"));
        }
  
        if(options.containsKey("--search")){
            rddValues = filterOnKeyword(rddValues, options.get("--search"));
        }
  
        System.out.println("Number of entries after filtering: " + 
	                   rddValues.count());
        Map<String, Long> counts = aggregateEntries(rddValues);
    
        for(Map.Entry<String, Long> entry: counts.entrySet()){
            System.out.println(entry.getValue() + ":\t" + entry.getKey());
        }
  
        sc.stop();
    }
}
