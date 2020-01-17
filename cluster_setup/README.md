# cluster_setup.sh

This script lets a user set up a small cluster (1-10 machines) quickly, for use in proof-of-concepts, small on premise installations, and cluster installations in cloud environments (OCI, AWS, Azure). It's intended to be run from a setup host that is not in the set of cluster hosts, typically a user laptop or other separate machine. The setup host should have a unix-like environment where bash can run. MacOS is fine.

You do not have to edit this script or pass any command-line parameters to it. It will prompt you for various inputs as it runs. All inputs will be saved, and running the script again will use the values from the previous run, so you don't have to retype everything if you run the script multiple times. You can change inputs when you run it again.

## Prerequisites

Before you can run the cluster_setup.sh script, you'll need to meet the following minimum requirements:

1. A setup host, from which you run the script, such as your laptop. The setup host must be capable of running bash in a unix-like environment (linux, macOS, cygwin, etc.)

2. One or more target host machines to where the NoSQL database engine will reside. These can be in OCI, or Amazon EC2, or... wherever. They must be running Linux. It's best if they all have the same hardware resources (cpu, ram, disk).

3. Ssh access, with no passphrase, to the target host machines from the setup host. This may require some setup in ~/.ssh/config, or alternatively, ssh options can be given in the cluster_setup.sh script.

4. Java 8 or greater installed on the target host(s). The minimum version is 8. Any installed java version 8 or above works fine.

5. One or more file system paths which specify the location(s) on the target hosts to be used to store NoSQL data. NVMe will result in the best performance, but any mounted drive (network or otherwise) will suffice. The amount of free space required varies greatly with the size and number of records you plan to store. In general, a minimum of 10GB free space is recommended.

6. (optional): Separate director(ies) for NoSQL logs, similar to #5.

7. A `tar.gz` or `zip` Oracle NoSQL kv installation file downloaded to the setup host.(For example: `kv-ee-19.5.13.tar.gz`). Downloads are available from https://www.oracle.com/database/technologies/nosql-database-server-downloads.html

8. (optional): A `tar.gz` or `zip` Oracle NoSQL Proxy installation file downloaded to the setup host, if you want to deploy a httpproxy to support the NoSQL http drivers. (For example: `oracle-nosql-proxy-5.1.10.tar.gz`).

## What the script does

The script will do the following tasks:

* Ask for the path to the downloaded Oracle NoSQL kv `tar.gz/.zip` file
* Ask for the (optional) path to the downloaded Oracle NoSQL Proxy `tar.gz/.zip` file
* Ask for all hostnames (or IP addresses) of target hosts, and ssh username/options
* Ask for the installation directory for NoSQL binaries/libraries
* Ask for the name of the store to be configured
* Ask for the data and log directory locations to store data/logs on each target host
* Ask for the starting port number for cluster communications
* Ask for the desired cluster data replication factor (if installing on more than one target host)
* Check ssh connections from your setup host to all target hosts
* Gather information from all target hosts (memory, cpu, etc.)
* Verify Java 8 or greater installations
* Ask for (optional) security information (for secure store setup)
* Optionally check network connectivity on several ports between all target hosts
* Show installation parameters and ask for confirmation
* Copy `.tar.gz/.zip` files to all target hosts in parallel
* Set up each target host, one by one, to run NoSQL Storage Nodes
* Create helper scripts in KVHOME/scripts on the setup hosts for various operations
* Deploy cluster topology and start all cluster Replication Nodes (this can take a while)
* Run a simple test to verify basic operation
* Set up the optional Oracle NoSQL Proxy running on each setup host
* Optionally run a longer extended test to get basic performance indicators
* Display store parameters for access to the newly running store


## What if it fails?
There are many situations where this script will fail. Most are due to network connectivity or ssh issues. You can rerun the script over and over, fixing issues as you encounter them. If you get completely stuck or have issues you just can't get to work, please send an email to `oraclenosql-info_ww@oracle.com` with "Oracle NoSQL Database cluster script" in the subject line, and someone will get back to you as soon as possible.

The most common failures when using this script are due to network ports not being open across the target hosts. See the note about ports at the end of this document.

## Notes for specific environments:

### Oracle Cloud Infrastructure (OCI):
* Spin up one or more compute instances – **Oracle Linux 7.x**
* Make sure that the port range 5000-5100 is open between all instances.
  * Refer to your local sysadmins for guidance on this. An example of how to do this in some systems may be:
  * `sudo firewall-cmd --permanent --direct --passthrough ipv4 -I INPUT_ZONES_SOURCE -p tcp --dport 5000:5100 -m conntrack --ctstate NEW,ESTABLISHED -j ACCEPT`
  * `sudo firewall-cmd --direct --passthrough ipv4 -I INPUT_ZONES_SOURCE -p tcp --dport 5000:5100 -m conntrack --ctstate NEW,ESTABLISHED -j ACCEPT`
* Install Java 8 or greater on each the hosts (if a version 8 or greater is not already installed)
  * `sudo yum install java`
* Download a release distribution of Oracle NoSQL Database to your setup host
  * https://www.oracle.com/database/technologies/nosql-database-server-downloads.html
* Optionally download a release distribution of the Oracle NoSQL Database Proxy to your setup host, if using the NoSQL http based drivers.
* Download `cluster_setup.sh` and make it executable
  * `curl 'https://raw.githubusercontent.com/oracle/nosql-examples/master/cluster_setup/cluster_setup.sh' > cluster_setup.sh`
  * `chmod +x ./cluster_setup.sh`
* Run `cluster_setup.sh`.  This will guide you through the setup of a NoSQL Cluster on your OCI nstances
  * `./cluster_setup.sh`




### Amazon EC2:
* Spin up one or more virtual machines – **Red Hat Linux**.  NOTE: You will need something more powerful than a micro, otherwise requests might timeout
* Make sure that the port range 5000-5100 is open
  * Add a rule to the security group opening these ports
* Install java 8 on each VM (if a version 8 or greater is not already installed)
  * many EC2 instances have java7 installed. To get java 8, do:
  * `sudo yum remove java`
  * `sudo yum install java-1.8.0-openjdk`
* Download a release distribution of Oracle NoSQL Database to your setup host
  * https://www.oracle.com/database/technologies/nosql-database-server-downloads.html
* Optionally download a release distribution of the Oracle NoSQL Database Proxy to your setup host, if using the NoSQL http based drivers.
* Download `cluster_setup.sh` and make it executable
  * `curl 'https://raw.githubusercontent.com/oracle/nosql-examples/master/cluster_setup/cluster_setup.sh' > cluster_setup.sh`
  * `chmod +x ./cluster_setup.sh`
* Run `cluster_setup.sh`.  This will guide you through the setup of a NoSQL Cluster on your EC2 instances
  * `./cluster_setup.sh`


### Microsoft Azure:
* Spin up one or more virtual machines – **Oracle Linux 7.x**
* Make sure that the port range 5000-5100 is open.
  * Refer to your local sysadmins for guidance on this. An example of how to do this in some systems may be:
  * `sudo iptables -I INPUT -p tcp -m tcp --dport 5000:5100 -j ACCEPT`
* Install Java 8 on each the VMs (if a version 8 or greater is not already installed)
  * `sudo yum install java`
* Download a release distribution of Oracle NoSQL Database to your setup host
  * https://www.oracle.com/database/technologies/nosql-database-server-downloads.html
* Optionally download a release distribution of the Oracle NoSQL Database Proxy to your setup host, if using the NoSQL http based drivers.
* Download `cluster_setup.sh` and make it executable
  * `curl 'https://raw.githubusercontent.com/oracle/nosql-examples/master/cluster_setup/cluster_setup.sh' > cluster_setup.sh`
  * `chmod +x ./cluster_setup.sh`
* Run `cluster_setup.sh`.  This will guide you through the setup of a NoSQL Cluster on your Azure instances
  * `./cluster_setup.sh`


## Advanced script details

The script accepts a few command-line options for debugging and convenience. Use of these options is discouraged unless you fully understand what you're doing.

- `-f`: Force installation, stopping and overwriting any existing installation. This may be useful if the script had errors partway through and the cluster is in an unknown state.
- `-d`: Debug. This will run all scripts with `-x` added and dump a LOT of strange output to your console. May be useful for advanced sysadmins.
- `-v`: Verbose. Show a bit more about what's going on.
- `-t`: Test. Used internally at Oracle for automatic regression testing of this script. Probably not useful otherwise.


## A note about ports

Oracle NoSQL uses a range of ports for its communication. The main starting port is used for client-server communication, and a range of additional ports are used for various server-server (and some client-server) features.

The actual range of ports needed depends on the capacity of each of the target hosts (machines with many NVMe drives and lots of RAM, for example, will use more ports) and whether security is enabled or not. A large installation with high capacity hosts may use 100 or more ports. Most installations typically use less than 30. The script determines the port range needed and can optionally test those port ranges across the target hosts.

The script does not, however, make any attempt to change firewall rules or open ports in any way. Oracle recommends you work with your sysadmins to determine the proper steps / commands / processes to open ports in your environment.

The example firewall/port commands given above in this document may not be adequate or correct for your environment. Please contact your local system administrators for help with ports and firewall rules.
