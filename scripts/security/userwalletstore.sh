#Direct the securityconfig utility to use Oracle Wallet as the password storage mechanism. The auto login wallets store the password in obfuscated state
java -jar $KVHOME/lib/kvstore.jar securityconfig wallet secret -dir /u01/clientWallet/store.wallet -set -alias jsmith

