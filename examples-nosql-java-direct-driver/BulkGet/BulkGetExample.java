import java.util.ArrayList;
import java.util.List;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.StoreIteratorException;
import oracle.kv.table.FieldRange;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;
import oracle.kv.table.TableIterator;
import oracle.kv.table.TableIteratorOptions;
public class BulkGetExample {

 // hard-coded connection parameters in the example, this could be taken as input to the program or can be read from configuration file

    private final String storeName = "kvstore";
    private final String hostName = "localhost";
    private final int port = 5000;
    private final String tableName = "phones";
    final KVStore store;
    public static void main(final String args[]) {
        try {
            BulkGetExample runTest = new BulkGetExample();
            runTest.run();
        } catch (Exception e) {
            System.err.print("BulkGetExample run failed: " + e.getMessage());
        }
    }

    BulkGetExample() {
        store = KVStoreFactory.getStore
            (new KVStoreConfig(storeName, hostName + ":" + port));
    }

    void run() {
        final String[] manufacturers = {"Nokia", "Apple", "Samsung", "Motorola"};
        final List<PrimaryKey> keys =
            new ArrayList<PrimaryKey>(manufacturers.length);
        final TableAPI tableAPI = store.getTableAPI();
        final Table phoneTable = tableAPI.getTable("PhoneTable");
        if (phoneTable == null) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }
        for (String manufacturer : manufacturers) {
            final PrimaryKey pk = phoneTable.createPrimaryKey();
            pk.put("manufacturer", manufacturer);
            keys.add(pk);
        }

       /* Initialize multiRowOption: price range in [200, 500].  */
        final FieldRange range = phoneTable.createFieldRange("price");
        range.setStart(200d, true).setEnd(500d, true);
        final MultiRowOptions mro = new MultiRowOptions(range, null, null);
       /* Initialize TableIteratorOptions */
//  Setting batch size parameter as 200, this number indicates the maximum number of results batches that can be held in the No-SQL database client before its processed by the replication node
        final int batchResultsSize = 200;
//  Setting the maximum number of concurrent threads of executor threads to 9, I ran this on 3x3 shard
        final int parallelism = 9;
        final TableIteratorOptions tio =
           new TableIteratorOptions(Direction.UNORDERED /* Direction */,
                                     null /* Consistency */,
                                     0 /* RequestTimeOut */,
                                     null /*TimeUnit*/,
                                     parallalism,
                                     batchResultsSize);
        TableIterator<Row> itr = null;
        int count = 0;
        try {

            itr = tableAPI.tableIterator(keys.iterator(), mro, tio);
            while (itr.hasNext()) {
                final Row phone = itr.next();
                System.out.println(phone.toJsonString(false));
                count++;
                /* ... */
            }
            System.out.println(count + " rows returned.");
        } catch (StoreIteratorException sie) {
            /* Handle exception.. */
        } finally {
            if (itr != null) {
                itr.close();
            }
        }
    }
}
