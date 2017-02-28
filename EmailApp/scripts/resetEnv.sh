## Killing java processes ##
echo "Killing Java processes ..."
jps | grep -v eclipse |  awk '{print $1}' | xargs -i kill -9 {}
ps -ef | grep kvstore | awk '{print $1}' | xargs -i kill -9 {}

echo "Deleting old kvroot..."
## Delete Directories ##
rm -rf /u02/kvroot

