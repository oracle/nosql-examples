NoSQL-examples
==============

This is a set of scripts to help with cluster creation automation for Windows users.

Edit the setenv.bat to point the environment variables KVHOME and KVROOT to point to your installation of Oracle NoSQL Database (KVHOME) and a directory where your database files will be stored (KVROOT).

Then by running a succession of these scripts you can create a local 3x3 cluster with the store name ONDB.

Run:
>setenv.bat                       :to setup your local environment.

>clean.bat                        :to clear the old contents of directories and setup fresh data directories.

>bootAndStart.bat                 :to create listening agent machine config files and start the agents.

>firstAdmin.bat  configStore.src  :to deploy a 3x3 cluster.

>secondAdmin.bat                  :to start an administrative command line interface to interact with the store.


These scripts can be easily modified to create different sized clusters that are deployed across physical machines.
