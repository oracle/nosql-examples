#!/bin/sh

java -jar $KVHOME/lib/kvcli.jar -host kvhost01 -port 5000 -store kvstore << EOF
#
# ****************************** Get all the Users with IDs beginning with UO1**********************************"

get table -name Users -field userId -start "U01" -end "UO2";

# ****************************** Get User with email id  foo.bar@email.com ***********************************"

get table -name Users -index emailIndex -field email -value 'foo.bar@email.com';

#******************************* Get all the folders for the User UO1 ******************************************" 

get table -name Users -field userId -value UO1 -child users.folder;

#******************************* Get all the Messages for the User UO1 in the Folder FO1(Inbox) ****************"

get table -name users.folder -field name -value "Inbox" -field userID -value "UO1" -ancestor users -child users.folder.message -pretty;


exit

EOF

