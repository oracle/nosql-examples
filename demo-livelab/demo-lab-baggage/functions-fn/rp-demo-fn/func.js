const fdk=require('@fnproject/fdk');
const process = require('process');

const fs = require('fs')


fdk.handle(function(input){
  let name = 'World';
  if (input.name) {
    name = input.name;
  }
  console.log('\nInside Node Hello World function')

  const sessionTokenFilePath = process.env.OCI_RESOURCE_PRINCIPAL_RPST
  const rpst = fs.readFileSync(sessionTokenFilePath, {encoding: 'utf8'})

  const payload = rpst.split('.')[1]
  const buff = Buffer.from(payload, 'base64')
  const payloadDecoded = buff.toString('ascii')
  const claims = JSON.parse(payloadDecoded)

  const tenancyId = claims.res_tenant
  const claimCompartmentId = claims.res_compartment;
  const region = claims.res_id.split('.')[3];

  const privateKeyPath = process.env.OCI_RESOURCE_PRINCIPAL_PRIVATE_PEM
  const privateKey = fs.readFileSync(privateKeyPath, 'ascii')

  const keyId = `ST$${rpst}`

  const nosqlcmp = process.env.NOSQL_COMPARTMENT_ID;
  const version = process.env.OCI_RESOURCE_PRINCIPAL_VERSION

  return process.env.OCI_RESOURCE_PRINCIPAL_REGION;
  return process.version;
})
