#Once the tables are defined run email client application
java -cp ../bin:../lib/kvclient.jar:$KVHOME/lib/jackson-mapper-asl.jar:$KVHOME/lib/jackson-core-asl.jar com.oracle.email.console.EmailCLI  localhost:5000 kvstore
