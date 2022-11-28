# Copyright (c) 2022 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
#

cat collection-copy.json | jq '.item[]| select(.name=="Query Streams Name") | .request.body.graphql' | jq '{query: .query, variables: .variables|fromjson}' >  query.json
cat query.json | jq

curl --location --request POST 'http://localhost:3000/' \
--header 'Content-Type: application/json' \
--data @query.json 2>/dev/null | jq
