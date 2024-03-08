### CREATE table ticket if not present ###
CREATE TABLE IF NOT EXISTS ticket(ticketNo LONG, confNo STRING, PRIMARY KEY(ticketNo));

### CREATE table ticket.baginfo if not present ###
CREATE TABLE IF NOT EXISTS ticket.bagInfo(id LONG,tagNum LONG,routing STRING,lastActionCode STRING,lastActionDesc STRING,lastSeenStation STRING,lastSeenTimeGmt TIMESTAMP(4),bagArrivalDate TIMESTAMP(4), PRIMARY KEY(id));

### CREATE table ticket.bagInfo.flightLegs if not present ###
CREATE TABLE IF NOT EXISTS ticket.bagInfo.flightLegs(flightNo STRING, flightDate TIMESTAMP(4),fltRouteSrc STRING,fltRouteDest STRING,estimatedArrival TIMESTAMP(4), actions JSON, PRIMARY KEY(flightNo));

### CREATE table ticket.passengerInfo if not present ###
CREATE TABLE IF NOT EXISTS ticket.passengerInfo(contactPhone STRING, fullName STRING,gender STRING, PRIMARY KEY(contactPhone));