### Begin Script ###
load -file parentchild.ddl
import -table ticket  -file ticket.json
import -table ticket.bagInfo  -file bagInfo.json
import -table ticket.passengerInfo  -file passengerInfo.json
import -table ticket.bagInfo.flightLegs  -file flightLegs.json
### End Script ###