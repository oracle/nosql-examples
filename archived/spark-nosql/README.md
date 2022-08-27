Environment:

  Java 1.8.0 is required to compile and run this sample.

  The following jar files to be in the CLASSPATH variable:

  spark-1.6.0-bin-hadoop2.6/lib/spark-assembly-1.6.0-hadoop2.6.0.jar
  kv-3.5.2/lib/kvstore.jar

  Its assumed that the Oracle NoSQL Database is running on localhost:5000
  and the store name is kvstore

Build:

  javac loganalyzer/*.java

Usage:

  To push the logs into Oracle NoSQL Database:

    java loganalyzer.PushLogs <log_directory>

  For log analysis usage:
    
    java loganalyzer.LogAnalyzer --help
