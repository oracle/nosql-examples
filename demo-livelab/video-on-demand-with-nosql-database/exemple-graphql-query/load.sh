# Copyright (c) 2022 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
#

ls -ld ../data/User
retCode=`echo $?`
if [ ! $retCode -eq 0 ]; then 
  exit
fi


ls -ld ../data/DataShow
retCode=`echo $?`
if [ ! $retCode -eq 0 ]; then 
  exit
fi


for serie in serie1 serie2 
do
  for fileUser in  `ls -1 ../data/User/User_$serie* `
  do 
    echo $fileUser
    cp $fileUser user.json
    sh createStream.sh
    for fileDataShow in `ls -1 ../data/DataShow/DataShow_$serie*  | shuf -n 5`
    do 
      echo "-> " $fileDataShow
      cp $fileDataShow show.json  
      sh updateStream.sh
    done
    sh queryStreamById.sh
  done
done
