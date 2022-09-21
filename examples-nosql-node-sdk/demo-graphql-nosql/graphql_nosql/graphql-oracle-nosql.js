// 
// Copyright (c) 2022 Oracle, Inc.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
// 

const express = require('express');
const graphql = require('express-graphql');
const graphqlTools = require('graphql-tools');
const NoSQLClient = require('oracle-nosqldb').NoSQLClient;
const Region = require('oracle-nosqldb').Region;
const ServiceType = require('oracle-nosqldb').ServiceType;
const app = express();

let port = process.env.PORT || 3000;

// Sample Data
const TABLE_NAME = 'blogtable';
const blogsData = [{id: 1, blog: 'Hello World by Dario'}];

process
.on('SIGTERM', function() {
  console.log("\nTerminating");
  if (client) {
     console.log("\close client SIGTERM");
     client.close();
  }
  process.exit(0);
})
.on('SIGINT', function() {
  console.log("\nTerminating");
  if (client) {
     console.log("\close client SIGINT");
     client.close();
  }
  process.exit(0);
});


//
// NoSQL Access Helpers
//

async function getAllBlogsHelper() {
  let statement = `SELECT * FROM ${TABLE_NAME} LIMIT 100`;
  const rows = [];
  let cnt ;
  let res;
  do {
     res = await client.query(statement, { continuationKey:cnt});
     rows.push.apply(rows, res.rows);
     cnt = res.continuationKey;
  } while(res.continuationKey != null);
  return rows;
}

async function getOneBlogHelper(id) {
  res = await client.get(TABLE_NAME, { id: id });
  return res.row;
}

async function createBlogHelper(input) {
  res = await client.putIfAbsent(TABLE_NAME, {
        blog: input.blog
  });
  let newBlog = {id :res.generatedValue , blog: input.blog};
  return newBlog;
}

async function updateBlogHelper(id, input) {
  // Because we are using GENERATED ALWAYS AS IDENTITY I am using UPDATE command
  // We can use the command putIfPresent in other cases - see updateBlogHelperWithoutSeq
  //  https://docs.oracle.com/en/database/other-databases/nosql-database/19.5/java-driver-table/inserting-identity-values-programmatically.html
  const preparedStmt = Object.assign({ __proto__: globalPreparedStmt.__proto__ }, globalPreparedStmt);
  preparedStmt.bindings = {
    $v_id: id,
    $v_blog : input.blog
  };
  console.log(preparedStmt)
  console.log(globalPreparedStmt)
  res = await client.query(preparedStmt);
  let updBlog = {id : id , blog: input.blog};
  return updBlog;
}

async function updateBlogHelperWithoutSeq(id, input) {
  res = await client.putIfPresent(TABLE_NAME, {
        id: id,
        blog: input.blog
  });
  let updBlog = {id : id , blog: input.blog};
  return updBlog;
}

async function deleteBlogHelper(id) {
  res = await client.delete(TABLE_NAME, {
        id: id
  });
  delBlog = {id: id, blog : 'Deleted'};
  return delBlog;
}

// Simple Blog schema with ID, blog fields
const typeDefs = `
type Blog {
  id: Int!,
  blog: String!
}
type Query {
  blogs: [Blog],
  blog(id: Int): Blog
}
input BlogEntry {
  blog: String!,
}
type Mutation {
  createBlog(input: BlogEntry): Blog!,
  updateBlog(id: Int, input: BlogEntry): Blog!,
  deleteBlog(id: Int): Blog!
}`;



// Resolver to match the GraphQL query and return data
const resolvers = {
  Query: {
    blogs(root, args, context, info) {
      return getAllBlogsHelper();
    },
    blog(root, {id}, context, info) {
      return getOneBlogHelper(id);
    }
  },
  Mutation: {
	createBlog(root, {input}, context, info) {
      return createBlogHelper(input);
    },
    updateBlog(root, {id, input}, context, info) {
      return updateBlogHelper(id, input);
    },
    deleteBlog(root, {id}, context, info) {
      return deleteBlogHelper(id);
    }
  }
};

// Build the schema with Type Definitions and Resolvers
const schema = graphqlTools.makeExecutableSchema({typeDefs, resolvers});


// Start the webserver
async function ws() {
  app.use('/graphql', graphql({
    graphiql: true,
    schema
  }));

  app.listen(port, function() {
    console.log('Listening on http://localhost:' + port + '/graphql');
  })
}

// Do it
let client;
let globalPreparedStmt;
async function run() {
  client = createClient();
  statement = `DECLARE $v_blog STRING; $v_id INTEGER; UPDATE ${TABLE_NAME} SET blog = $v_blog WHERE id=$v_id`;
  globalPreparedStmt = await client.prepare(statement);	
  await ws();
}

function createClient() {
       return new NoSQLClient({
            region: process.env.NOSQL_REGION ,
            compartment:process.env.NOSQL_COMPID,
            auth: {
              iam: {
                  useInstancePrincipal: true
              }
            }
        });
}


run();
