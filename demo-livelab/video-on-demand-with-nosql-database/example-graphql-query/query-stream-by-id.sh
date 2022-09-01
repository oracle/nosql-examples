# Copyright (c) 2022 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
#

NOSQL_EXAMPLE_STREAMID=$(cat _createStreamId.txt)
cp collection.json collection-copy.json
sed -i "s/NOSQL_EXAMPLE_STREAMID/$NOSQL_EXAMPLE_STREAMID/g"  collection-copy.json
cat collection-copy.json | jq '.item[]| select(.name=="Query Streams by Id") | .request.body.graphql' >  query.json
cat query.json | jq

curl --location --request POST 'http://localhost:3000/' \
--header 'Content-Type: application/json' \
--data @query.json 2>/dev/null | jq
