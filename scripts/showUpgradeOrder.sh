 ####  Display the new upgrade order
  echo "Latest upgrade Order..."
  java -jar $NEW_KVHOME/lib/kvstore.jar runadmin -port 5000 -host kvhost01 show upgrade

