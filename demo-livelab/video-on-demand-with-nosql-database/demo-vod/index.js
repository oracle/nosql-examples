//
// Copyright (c) 2022 Oracle, Inc.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
//

import { ApolloServer } from '@apollo/server'
import { startStandaloneServer } from '@apollo/server/standalone'

import fs from 'fs'
import pkg from 'oracle-nosqldb'
const { NoSQLClient, ServiceType, CapacityMode } = pkg

process
  .on('SIGTERM', function () {
    console.log('\nTerminating')
    if (client) {
      console.log('\nclose client SIGTERM')
      client.close()
    }
    process.exit(0)
  })
  .on('SIGINT', function () {
    console.log('\nTerminating')
    if (client) {
      console.log('\nclose client SIGINT')
      client.close()
    }
    process.exit(0)
  })

async function createTable (client) {
  const createDDL = fs.readFileSync('demo-stream-acct.ddl', 'utf8')
  // readUnits, writeUnits, storageGB using same values as for Always free
  const resTab = await client.tableDDL(createDDL, {
    tableLimits: {
      // mode: CapacityMode.ON_DEMAND,
      mode: CapacityMode.PROVISIONED,
      readUnits: 50,
      writeUnits: 50,
      storageGB: 25
    },
    complete: true
  })
  await client.forCompletion(resTab)
  console.log('  Creating table %s', resTab.tableName)
  console.log('  Table state: %s', resTab.tableState.name)
  //let indexes = ['demo-stream-acct-idx1.ddl','demo-stream-acct-idx2.ddl','demo-stream-acct-idx3.ddl'];
  //for (let idx of indexes) {
  //    const indexDDL = fs.readFileSync(idx, 'utf8')
  //    await client.tableDDL(indexDDL, {complete: true});
  //    console.log('  Creating index %s', idx);
  //}
}

function createNoSQLClient () {
  console.log(process.env.NOSQL_ServiceType)
  console.log(process.env.NOSQL_REGION)
  console.log(process.env.NOSQL_COMPID)
  

  switch (process.env.NOSQL_ServiceType) {
    case 'useInstancePrincipal':
      return new NoSQLClient({
        region: process.env.NOSQL_REGION,
        compartment: process.env.NOSQL_COMPID,
        auth: {
          iam: {
            useInstancePrincipal: true
          }
        }
      })
    case 'useDelegationToken':
      return new NoSQLClient({
        region: process.env.NOSQL_REGION,
        compartment: process.env.NOSQL_COMPID,
        auth: {
          iam: {
            useInstancePrincipal: true,
            delegationTokenProvider: process.env.OCI_DELEGATION_TOKEN_FILE
          }
        }
      })
    case 'useResourcePrincipal':
      return new NoSQLClient({
        region: process.env.NOSQL_REGION,
        compartment: process.env.NOSQL_COMPID,
        auth: {
          iam: {
            useResourcePrincipal: true,
          }
        }
      })
    default:
      // on-premise non-secure configuration or Cloud Simulator
      return new NoSQLClient({
        serviceType: ServiceType.KVSTORE,
        endpoint: process.env.NOSQL_ENDPOINT + ':' + process.env.NOSQL_PORT
      })
  }
}

//
// NoSQL Access Helpers
//
const TABLE_NAME = 'stream_acct'

async function getAllStreamsHelper () {
  const statement = `SELECT d.id, d.info as info FROM ${TABLE_NAME} d LIMIT 100`
  const rows = []
  for await(const res of client.queryIterable(statement)) {
    rows.push.apply(rows, res.rows);
  }
  return rows
}

async function getByLastNameHelper (lastName) {
  const statement = `DECLARE $v_lastName STRING; SELECT d.id, d.info as info FROM ${TABLE_NAME} d WHERE d.info.lastName = $v_lastName`
  const rows = []
  const preparedStmt = await client.prepare(statement)
  preparedStmt.bindings = {
    $v_lastName: lastName
  }
  for await(const res of client.queryIterable(preparedStmt)) {
    rows.push.apply(rows, res.rows);
  }
  return rows
}

async function peopleWatching (country) {
  const statement = `DECLARE $v_country STRING; SELECT $show.showId, count(*) as cnt FROM ${TABLE_NAME} $s, unnest($s.info.shows[] as $show) WHERE $s.info.country = $v_country GROUP BY $show.showId ORDER BY count(*) DESC`
  const rows = []
  const preparedStmt = await client.prepare(statement)
  preparedStmt.bindings = {
    $v_country: country
  }
  for await(const res of client.queryIterable(preparedStmt)) {
    rows.push.apply(rows, res.rows);
  }
  return rows
}

async function watchTime () {
  const statement = 'SELECT $show.showName, $seriesInfo.seasonNum, sum($seriesInfo.episodes.minWatched) AS length FROM stream_acct n, unnest(n.info.shows[] AS $show, $show.seriesInfo[] as $seriesInfo) GROUP BY $show.showName, $seriesInfo.seasonNum ORDER BY sum($seriesInfo.episodes.minWatched)'
  const rows = []
  for await(const res of client.queryIterable(statement)) {
    rows.push.apply(rows, res.rows);
  }
  return rows
}

async function getOneStreamHelper (id) {
  const res = await client.get(TABLE_NAME, { id })
  // let z = Object.assign({id :id}, res.row.info)
  return res.row
}

async function createStreamHelper (input) {
  input.shows = []
  const res = await client.putIfAbsent(TABLE_NAME, {
    info: input
  })
  const newStream = { id: res.generatedValue, info: input }
  return newStream
}

async function updateStreamHelper (id, input) {
  const myJSON = JSON.stringify(input)
  const statement = `DECLARE $v_show JSON; $v_id INTEGER; UPDATE ${TABLE_NAME} p ADD p.info.shows $v_show WHERE id=$v_id RETURNING *`
  // statement = `DECLARE $v_id INTEGER; UPDATE ${TABLE_NAME} p ADD p.info.shows ${myJSON} WHERE id=$v_id RETURNING *`;
  const preparedStmt = await client.prepare(statement)
  preparedStmt.bindings = {
    $v_id: id,
    $v_show: input
  }
  const res = await client.query(preparedStmt)
  return (res.rows[0])
}

/*
async function updateStreamHelper(id, input) {
  res = await client.putIfPresent(TABLE_NAME, {
        id: id,
        info: input
  });
  let updStream = {id :id,  info: input};
  return updStream;
}
*/

async function deleteStreamHelper (id) {
  const res = await client.delete(TABLE_NAME, {
    id
  })
  const delStream = { id, info: null }
  return delStream
}

// A schema is a collection of type definitions (hence "typeDefs")
// that together define the "shape" of queries that are executed against
// your data.
const typeDefs = `#graphql
type episodes {
  episodeID: Int!,
  lengthMin: Int!,
  minWatched: Int!,
  date: String
}

type seriesInfo {
  seasonNum: Int!,
  numEpisodes: Int!,
  episodes: [episodes]
}
type shows {
  showName: String!
  showId: Int,
  type: String,
  genres: [String],
  numSeasons: Int,
  seriesInfo: [seriesInfo]
}
type StreamContent {
  firstName: String!,
  lastName: String!,
  country: String!,
  shows: [shows]
}
type Stream {
  id: Int!,
  info: StreamContent
}
type AggResult1 {
  showId: Int!,
  cnt: Int!
}
type AggResult2 {
  showName: String!,
  seasonNum: Int!,
  length: Int!
}
type Query {
  streams: [Stream],
  stream(id: Int): Stream,
  streamByLastName(lastName: String): [Stream],
  peopleWatching(country: String!): [AggResult1!],
  watchTime: [AggResult2!]
}

input StreamEntry {
  firstName: String!,
  lastName: String!,
  country: String!,
}
input episodesEntry {
  episodeID: Int!,
  lengthMin: Int!,
  minWatched: Int!,
  date: String
}

input seriesInfoEntry {
  seasonNum: Int!,
  numEpisodes: Int!,
  episodes: [episodesEntry]
}
input showsEntry {
  showName: String!
  showId: Int,
  type: String,
  genres: [String],
  numSeasons: Int,
  seriesInfo: [seriesInfoEntry]
}
type Mutation {
  createStream(input: StreamEntry): Stream!,
  updateStream(id: Int, input: showsEntry): Stream!,
  deleteStream(id: Int): Stream!
}
`

// Resolvers define the technique for fetching the types defined in the
// schema. This resolver retrieves books from the "books" array above.
const resolvers = {
  Query: {
    streams (root, args, context, info) {
      return getAllStreamsHelper()
    },
    stream (root, { id }, context, info) {
      return getOneStreamHelper(id)
    },
    streamByLastName (root, { lastName }, context, info) {
      return getByLastNameHelper(lastName)
    },
    peopleWatching (root, { country }, context, info) {
      return peopleWatching(country)
    },
    watchTime (root, context, info) {
      return watchTime()
    }
  },
  Mutation: {
    createStream (root, { input }, context, info) {
      return createStreamHelper(input)
    },
    updateStream (root, { id, input }, context, info) {
      return updateStreamHelper(id, input)
    },
    deleteStream (root, { id }, context, info) {
      return deleteStreamHelper(id)
    }
  }
}

// Connecting to NoSQL and create table
const client = createNoSQLClient()
createTable(client)

const server = new ApolloServer({
  typeDefs,
  resolvers

})

const { url } = await startStandaloneServer(server, {
  listen: { port: 3000 }
})

console.log(`🚀  Server ready at: ${url}`)
