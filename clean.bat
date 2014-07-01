java -jar %KVHOME%\lib\kvstore.jar stop -root %KVROOT%\dataCenter1\kvroot
java -jar %KVHOME%\lib\kvstore.jar stop -root %KVROOT%\dataCenter2\kvroot
java -jar %KVHOME%\lib\kvstore.jar stop -root %KVROOT%\dataCenter3\kvroot

rmdir %KVROOT% /s/q

mkdir %KVROOT%\dataCenter1
mkdir %KVROOT%\dataCenter1\kvroot
mkdir %KVROOT%\dataCenter1\kvStorage
mkdir %KVROOT%\dataCenter1\kvroot\ondb

mkdir %KVROOT%\dataCenter2
mkdir %KVROOT%\dataCenter2\kvroot
mkdir %KVROOT%\dataCenter2\kvStorage
mkdir %KVROOT%\dataCenter2\kvroot\ondb

mkdir %KVROOT%\dataCenter3
mkdir %KVROOT%\dataCenter3\kvroot
mkdir %KVROOT%\dataCenter3\kvStorage
mkdir %KVROOT%\dataCenter3\kvroot\ondb
