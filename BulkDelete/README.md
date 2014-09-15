Efficient Deletion of Key Ranges

The example shows how to get an iterator over a stores keyspace using a partial path and 
then efficiently deleting all keys-values or a range under the major key.

1) Download a zip of these examples and import the project BulkDelete into your Eclipse workspace.
2) Adjust the properties of the project so libraries point to your local Oracle NoSQL Store libraries.

3) Create a Oracle NoSQL Database called kvstore
4) Run the java class KeySpaceTool and choose the menu option to populate a sample key space
5) Examine and Run the java class BulkDelete

  sample arguments:  kvstore localhost:5000 /home/family COMMIT_NO_SYNC lassy nancy    ...will delete half the keys
