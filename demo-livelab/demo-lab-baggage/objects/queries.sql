SELECT * 
FROM demoKeyVal d
WHERE d.value.ticketNo = "1762322446040"

SELECT * 
FROM demo d 
WHERE ticketNo = "1762386738153"

SELECT * 
FROM demo d 
WHERE cast (ticketNo as Long)= 1762386738153

SELECT * 
FROM demo d  
WHERE ticketNo = 1762386738153

SELECT fullname, size(d.baginfo.flightLegs) as flightLegs, d.baginfo.flightLegs.flightNo as flightNo
FROM demo d

SELECT  {
      "fullName" : d.fullname,
      "ticketNum" : d.ticketNo,
      "flightLegs" : size(d.baginfo.flightLegs),
      "flightNo" : d.baginfo.flightLegs.flightNo
 } as content
FROM demo d

SELECT  {
      "fullName" : d.fullname,
      "ticketNum" : d.ticketNo,
      "flightLegs" : size(d.baginfo.flightLegs),
      "flightNo" : d.baginfo.flightLegs.flightNo
 } as content
 FROM demo d 
 WHERE ticketNo = "1762386738153"


select d.baginfo.flightLegs, 
       seq_transform(d.baginfo.flightLegs[],
                         {
                           "flightNo" : $.flightNo,
                           "flightDate" : $.flightDate,
						   "Actions" : $.actions
                         } ) as Item
from demo d
