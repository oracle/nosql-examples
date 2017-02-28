#!/bin/sh
java -jar $KVHOME/lib/onql.jar -helper-hosts kvhost01:5000 -store kvstore <<EOF
select userId,users.name.first,users.name.last,age from users;

select userID,users.name.first,users.name.last,age from users order by userID;

select userId,users.name.first,users.name.last,age from users where age > 10 and age <50;

select userId, size(u.name) as firstname from users u;

EOF

