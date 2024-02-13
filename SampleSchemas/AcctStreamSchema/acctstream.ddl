CREATE TABLE IF NOT EXISTS stream_acct(
acct_id INTEGER,
profile_name STRING,
account_expiry TIMESTAMP(9),
acct_data JSON, 
PRIMARY KEY(acct_id)
);
