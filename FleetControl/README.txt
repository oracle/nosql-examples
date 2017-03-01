Objective:

The objective of this demo is to show the ability of Oracle NoSQL database 
being used for a Fleet application. This demo showcases:

1. Usage of Child Tables.
2. Secondary indexes

Following are some of the features built in this demo

1. Load tables from a continuous stream of data pumped from a fleet/Car.
2. Graphical Representation of Mileage
3. Top performing cars based on selectable criteria.
4. historic graphical representation of data
5. Near Real time data being inserted and read.

--------------------------------------------------------------------------------
To Run Email demo application from the compiled JAR file run following steps
--------------------------------------------------------------------------------

1. Set KVHOME environment variable to point to the latest home directory of 
   Oracle NoSQL Database binaries.

2. Next from the email sample application home directory change directory 
  to scripts folder.
   
   		$[user@localhost EmailApp]$ cd scripts
   		
3. Now we need to start the kvlite and there is a script here called 
   startKVLite.sh that will delete /tmp/kvroot directory and then use 
   /tmp/kvroot as the -root option. You can modify this simple script file 
   based on your need but it should run as is in any linux environment. 
   Let's run this script first:
   
   		$[user@localhost script]$ ./startKVLite.sh
   		Killing Java processes ...
		kill 2899: No such process
		Deleting old kvroot...
		Starting KVLite ...
		nohup: redirecting stderr to stdout
		
4. Wait for 10 seconds and then run java application to create schemas that 
   will be used by the application to store user profile information and email
    messages.  

	$[user@localhost script]$ ./createTables.sh				
	===============================================================
	 Successfully Connected to Oracle NoSQL DB @ localhost:
	 5000/kvstore
	===============================================================
	fleet TABLE & INDEX makeIndex created...
	fleet.mileage TABLE & INDEX mpgIndex, fuelIndex, distanceIndex  created.
	Connected to kvstore at kvhost01:5000.
		
	Inserted 100 rows in fleet table

		
5. Next step would be to run a continuous workload that will generate fleet data
   for 100 vehicles. To run this application make sure you have fleetControl.jar
   file under FLEET_HOME/bin directory. If not there then create a jar file and 
   include src & content packages.
   		
	[oracle@localhost scripts]$ ./startWorkload.sh 
	Re-initializing the workload
	Connected to kvstore at kvhost01:5000.
	0 row deleted.

	==================================================================
  	Successfully Connected to Oracle NoSQL DB @ localhost:5000/kvstore
	==================================================================
	{"vin":"023X43EKB0ON212J84F6","currentTime":10001,"driverID":
	"X6712184","longitude":58.0,"latitude":75.0,"odometer":7,"fuelUsed":
	0.37562913,"speed":70}
	Successfully Inserted Mileage Information for VIN: 023X43EKB0ON212
 	....

6. Once you get above output in the terminal window let the application run
   from that window and open another terminal window.
   
7. From this window start the dashboard to view the real time status of 
   fleets. To run do this:
	
    	oracle@localhost scripts]$ ./startGraphs.sh 
	==================================================================
  	Successfully Connected to Oracle NoSQL DB @ localhost:5000/kvstore
	==================================================================

8. You should get a swing based application dashboard. Now you can 
   press START button to view the TOP mileage vehicle in real time.
   
9. You can press STOP button to stop the display. There is a drop 
   down box on the top right hand side with three options (mileage, 
   fuel & distance), you can select any one option and select START
   again. You can STOP and this time click the button with label 
   TOP which will toggle it into label BOTTOM. Now when you press
   START button dashboard would display you 3 bottom most vehicle
   with mileage, fuel or distance characterstics.  
       
   ~~~~~~~~ Have fun playing with the application. ~~~~~~~~~~~
   
