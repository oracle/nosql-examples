CREATE TABLE IF NOT EXISTS BaggageInfo (
ticketNo LONG,
fullName STRING,
gender STRING,
contactPhone STRING,
confNo STRING,
bagInfo JSON,
PRIMARY KEY (ticketNo)
);