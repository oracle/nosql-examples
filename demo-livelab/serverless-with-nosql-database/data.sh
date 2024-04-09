mkdir ~/BaggageData
cd ~/BaggageData
curl https://c4u04.objectstorage.us-ashburn-1.oci.customer-oci.com/p/EcTjWk2IuZPZeNnD_fYMcgUhdNDIDA6rt9gaFj_WZMiL7VvxPBNMY60837hu5hga/n/c4u04/b/livelabsfiles/o/data-management-library-files/BaggageData.tar.gz -o BaggageData.tar.gz
tar xvzf BaggageData.tar.gz
rm  BaggageData.tar.gz

# Create a file to do a multi line load
rm -f load_multi_line.json
for file in `ls -1 ~/BaggageData/baggage_data* | tail -20`; do
  echo $file
  cat $file | tr '\n' ' ' >> load_multi_line.json
  echo >> load_multi_line.json
done

