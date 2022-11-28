# Copyright (c) 2022 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
#

# CreateStream
#

cp user.json user-copy.json
sed -z 's/\n/\\r\\n/g' -i user-copy.json
sed -i "s/\"/\\\\\"/g"  user-copy.json
cp collection.json collection-copy.json
sed -e "s/NOSQL_EXAMPLE_USER/$(<user-copy.json sed -e 's/[\&/]/\\&/g')/g" -i collection-copy.json
cat collection-copy.json | jq '.item[]| select(.name=="CreateStream") | .request.body.graphql' | jq '{query: .query, variables: .variables|fromjson}' >  query.json

cat query.json | jq

NOSQL_EXAMPLE_STREAMID=$(curl --location --request POST 'http://localhost:3000/' \
--header 'Content-Type: application/json' \
--data @query.json 2>/dev/null | jq '.data.createStream.id')
echo $NOSQL_EXAMPLE_STREAMID > _createStreamId.txt
echo "createStream.id=$NOSQL_EXAMPLE_STREAMID"
echo 
