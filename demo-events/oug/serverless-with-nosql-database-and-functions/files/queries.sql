SELECT *
FROM demoKeyVal d
WHERE d.value.ticketNo = "1762322446040"

SELECT *
FROM demo d
WHERE d.ticketNo = "1762386738153"


SELECT fullname, size(d.baginfo.flightLegs) as flightLegs,
d.baginfo.flightLegs.flightNo as flightNo
FROM demo d

SELECT  {
      "fullName" : d.fullname,
      "ticketNum" : d.ticketNo,
      "flightLegs" : size(d.baginfo.flightLegs),
      "flightNo" : d.baginfo.flightLegs.flightNo
 } as content
FROM demo d
