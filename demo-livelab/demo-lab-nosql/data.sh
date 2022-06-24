mkdir ~/BaggageData
cd ~/BaggageData
curl https://objectstorage.us-ashburn-1.oraclecloud.com/p/GzwEz9xBCBHrd0gx1QE0U8hqvNTKzmKcs1pSx3CQ2Ip9A05Z1vHgPNeVRMx_1cLp/n/c4u04/b/livelabsfiles/o/data-management-library-files/BaggageData.tar.gz -o BaggageData.tar.gz
tar xvzf BaggageData.tar.gz
rm  BaggageData.tar.gz

# Create a file to do a multi line load
rm -f load_multi_line.json
for file in `ls -1 ~/BaggageData/baggage_data* | tail -50`; do
  echo $file
  cat $file | tr '\n' ' ' >> load_multi_line.json
  echo >> load_multi_line.json
done

