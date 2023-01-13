#
# Copyright (c) 2023 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

DROP TABLE IF EXISTS USERS;
CREATE TABLE Users(uid INTEGER, person JSON,PRIMARY KEY(uid))  IN REGIONS CO , FR;
insert into users values(1,{"firstName":"jack","lastName":"ma","location":"FR"});
insert into users values(2, {"firstName":"foo","lastName":"bar","location":null});
update users u set u.person.location = "FR" where uid = 2;
update users u set u.person.location= "CO" where uid =1;
select * from users;
