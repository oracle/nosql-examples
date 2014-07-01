java -jar %KVHOME%\lib\kvstore.jar makebootconfig ^
-root %KVROOT%\dataCenter1\kvroot ^
-port 5000 ^
-admin 5001 ^
-host RCGREENE-LAP ^
-harange 5010,5020 ^
-num_cpus 0 ^
-memory_mb 100 ^
-store-security none

java -jar %KVHOME%\lib\kvstore.jar makebootconfig ^
-root %KVROOT%\dataCenter2\kvroot ^
-port 6000 ^
-admin 6001 ^
-host RCGREENE-LAP ^
-harange 6010,6020 ^
-num_cpus 0 ^
-memory_mb 100 ^
-store-security none

java -jar %KVHOME%\lib\kvstore.jar makebootconfig ^
-root %KVROOT%\dataCenter3\kvroot ^
-port 7000 ^
-admin 7001 ^
-host RCGREENE-LAP ^
-harange 7010,7020 ^
-num_cpus 0 ^
-memory_mb 100 ^
-store-security none

start /B java -jar %KVHOME%\lib\kvstore.jar start -root %KVROOT%\dataCenter1\kvroot
start /B java -jar %KVHOME%\lib\kvstore.jar start -root %KVROOT%\dataCenter2\kvroot
start /B java -jar %KVHOME%\lib\kvstore.jar start -root %KVROOT%\dataCenter3\kvroot

