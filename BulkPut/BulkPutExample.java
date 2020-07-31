/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2015 Oracle and/or its affiliates.  All rights reserved.
 *
 *  Oracle NoSQL Database is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation, version 3.
 *
 *  Oracle NoSQL Database is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License in the LICENSE file along with Oracle NoSQL Database.  If not,
 *  see <http://www.gnu.org/licenses/>.
 *
 *  An active Oracle commercial licensing agreement for this product
 *  supercedes this license.
 *
 *  For more information please contact:
 *
 *  Vice President Legal, Development
 *  Oracle America, Inc.
 *  5OP-10
 *  500 Oracle Parkway
 *  Redwood Shores, CA 94065
 *
 *  or
 *
 *  berkeleydb-info_us@oracle.com
 *
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  EOF
 *
 */

package bulkput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import oracle.kv.BulkWriteOptions;
import oracle.kv.EntryStream;
import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyValue;
import oracle.kv.Value;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;

/**
 * This is a simple example that demonstrates the bulk put feature of KV
 * interface KVStore.put(List<EntryStream<KeyValue>>, BulkWriteOptions) and
 * Table interface TableAPI.put(List<EntryStream<Row>>, BulkWriteOptions).
 *
 * usage: bulkput.BulkPutExample
 *       -store <instance name>
 *       -host <host name>
 *       -port <port number>
 *       [-load <# records to load>] (default: 1000)
 *       [-streamParallelism <numr of streams>] (default: 1)
 *       [-perShardParallelism <num of writer threads per shard>] (default: 2)
 *       [-heapPercent <percentage of max memory used for bulk put>] (default: 70)]
 *       [-useTable]
 *
 * If -useTable is specified, then it create table "users" and load records to
 * the table. Otherwise, the key/value records are loaded to store.
 *
 * The format of key/value entry:
 *  - key: /bulk/<index>
 *  - value: a fixed 128 bytes array.
 *
 * The schema of table "users" is like below:
 *   create table if not exists users (
 *      id long,
 *      name string,
 *      data binary,
 *      primary key(id))
 *
 *  - id: the index of row.
 *  - name: "name"<index>
 *  - data: a fixed 128 bytes array.
 *
 *  e.g. Load 10000 KVs with 3 input streams:
 *  java -cp KVHOME/lib/kvclient.jar:<path-to-example-class>
 *       bulkput.BulkPutExample -store kvstore -host localhost -port 5000
 *       -load 10000 -streamParallelism 3 -perShardParallelism 3
 *       -heapPercent 50
 *
 */

public class BulkPutExample {

    private final KVStore store;
    private final BulkWriteOptions bulkWriteOptions;

    private int streamParallelism = 1;
    private int perShardParallelism = 2;
    private int heapPercent = 70;
    private int nLoad = 1000;
    private boolean useTable = false;

    private final static String TABLE_NAME = "users";
    private final static int BINARY_LEN = 128;
    private final static String CREATE_TABLE = "create table if not exists " +
        TABLE_NAME + "(id long, name string, data binary, primary key(id))";
    private Table userTable = null;

    public static void main(final String args[]) {
        try {
            BulkPutExample runTest =
                new BulkPutExample(args);
            runTest.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BulkPutExample(final String[] argv) {

        String storeName = null;
        String hostName = null;
        int port = -1;

        final int nArgs = argv.length;
        int argc = 0;

        if (nArgs == 0) {
            usage(null);
        }

        while (argc < nArgs) {
            final String thisArg = argv[argc++];

            if (thisArg.equals("-store")) {
                if (argc < nArgs) {
                    storeName = argv[argc++];
                } else {
                    usage("-store requires an argument");
                }
            } else if (thisArg.equals("-host")) {
                if (argc < nArgs) {
                    hostName = argv[argc++];
                } else {
                    usage("-host requires an argument");
                }
            } else if (thisArg.equals("-port")) {
                if (argc < nArgs) {
                    port = Integer.parseInt(argv[argc++]);
                } else {
                    usage("-port requires an integer argument");
                }
            } else if (thisArg.equals("-streamParallelism")) {
                if (argc < nArgs) {
                    streamParallelism = Integer.parseInt(argv[argc++]);
                } else {
                    usage("-streamParallelism requires an argument");
                }
            } else if (thisArg.equals("-perShardParallelism")) {
                if (argc < nArgs) {
                    perShardParallelism = Integer.parseInt(argv[argc++]);
                } else {
                    usage("-perShardParallelism requires an argument");
                }
            } else if (thisArg.equals("-heapPercent")) {
                if (argc < nArgs) {
                    heapPercent = Integer.parseInt(argv[argc++]);
                } else {
                    usage("-heapPercent requires an argument");
                }
            } else if (thisArg.equals("-load")) {
                if (argc < nArgs) {
                    nLoad = Integer.parseInt(argv[argc++]);
                } else {
                    usage("-load requires an argument");
                }
            } else if (thisArg.equals("-useTable")) {
                useTable = true;
            } else {
                usage("Unknown argument: " + thisArg);
            }
        }

        if (hostName == null) {
            usage("Missing argument: -host");
        }
        if (port == -1) {
            usage("Missing argument: -port");
        }
        if (storeName == null) {
            usage("Missing argument: -store");
        }

        final String fmt = "\thost:%s\n\tport:%d\n\tstore:%s\n" +
        		           "\tnumToload:%,d\n\tuseTable:%s\n" +
        		           "\tbulkWriteOptions:\n" +
        		           "\t\tstreamParallelism:%d\n" +
        		           "\t\tperShardParallelism:%d\n" +
        		           "\t\theapPercent:%d\n";
        System.err.println(String.format(fmt, hostName, port, storeName, nLoad,
                                         String.valueOf(useTable),
                                         streamParallelism, perShardParallelism,
                                         heapPercent));

        bulkWriteOptions = new BulkWriteOptions(null, 0, null);
        bulkWriteOptions.setStreamParallelism(streamParallelism);
        bulkWriteOptions.setPerShardParallelism(perShardParallelism);
        bulkWriteOptions.setBulkHeapPercent(heapPercent);

        store = KVStoreFactory.getStore
                (new KVStoreConfig(storeName, hostName + ":" + port));
    }

    private void usage(final String message) {
        if (message != null) {
            System.err.println("\n" + message + "\n");
        }

        System.err.println("usage: " + getClass().getName());
        System.err.println
            ("\t-store <instance name>\n" +
             "\t-host <host name>\n" +
             "\t-port <port number>\n" +
             "\t[-load <# records to load>] (default: 1000)\n" +
             "\t[-streamParallelism <the number of streams>]" +
             " (default: 1)\n" +
             "\t[-perShardParallelism <the number of writer threads per shard>]" +
             " (default: 2)\n" +
             "\t[-heapPercent <percentage of max memory used for bulk put>]" +
             " (default: 70)]\n" +
             "\t[-useTable]");
        System.exit(1);
    }

    private void run() {
        try {
            if (useTable) {
                createTable();
                doLoadRows();
            } else {
                doLoadKVs();
            }
        } finally {
            store.close();
        }
    }

    private void createTable() {
        final TableAPI tableImpl = store.getTableAPI();
        try {
            store.executeSync(CREATE_TABLE);
        } catch (FaultException fe) {
            System.out.println("Create table failed: " + fe.getMessage());
            throw fe;
        }
        userTable = tableImpl.getTable(TABLE_NAME);
        System.err.println("Created table " + TABLE_NAME + ".");
    }

    private void doLoadKVs() {
        System.err.println("Loading KVs...");
        final List<EntryStream<KeyValue>> streams =
            new ArrayList<EntryStream<KeyValue>>(streamParallelism);
        final int num = (nLoad + (streamParallelism - 1)) /streamParallelism;
        for (int i = 0; i < streamParallelism; i++) {
            final int min = num * i;
            final int max = Math.min((min + num) , nLoad);
            streams.add(new LoadKVStream(i, min, max));
        }

        store.put(streams, bulkWriteOptions);

        long total = 0;
        long keyExists = 0;
        for (EntryStream<KeyValue> stream: streams) {
            total += ((LoadKVStream)stream).getCount();
            keyExists += ((LoadKVStream)stream).getKeyExistsCount();
        }
        final String fmt = "Loaded %,d records, %,d pre-existing.";
        System.err.println(String.format(fmt, total, keyExists));
    }

    private void doLoadRows() {
        System.err.println("Loading rows to " + TABLE_NAME + "...");

        final List<EntryStream<Row>> streams =
            new ArrayList<EntryStream<Row>>(streamParallelism);
        final int num = (nLoad + (streamParallelism - 1)) / streamParallelism;
        for (int i = 0; i < streamParallelism; i++) {
            final int min = num * i;
            final int max = Math.min((min + num) , nLoad);
            streams.add(new LoadRowStream(i, min, max));
        }

        final TableAPI tableImpl = store.getTableAPI();
        tableImpl.put(streams, bulkWriteOptions);

        long total = 0;
        long keyExists = 0;
        for (EntryStream<Row> stream: streams) {
            total += ((LoadRowStream)stream).getCount();
            keyExists += ((LoadRowStream)stream).getKeyExistsCount();
        }
        final String fmt = "Loaded %,d rows to %s, %,d pre-existing.";
        System.err.println(String.format(fmt, total, TABLE_NAME, keyExists));
    }

    private class LoadKVStream extends LoadStream<KeyValue> {

        LoadKVStream(long index, long min, long max) {
            super("LoadKVStream", index, min, max);
        }

        @Override
        KeyValue createEntry(long idx) {
            final Key key = Key.fromString("/bulk/" + idx);
            final Value value = Value.createValue(getBinaryData(BINARY_LEN));
            return new KeyValue(key, value);
        }

        @Override
        String entryToString(KeyValue entry) {
            return entry.toString();
        }
    }

    private class LoadRowStream extends LoadStream<Row> {
        LoadRowStream(long index, long min, long max) {
            super("LoadRowStream", index, min, max);
        }

        @Override
        Row createEntry(long idx) {
            return createUserRow(idx);
        }

        @Override
        String entryToString(Row entry) {
            return entry.toJsonString(false);
        }

        private Row createUserRow(long id) {
            final Row row = userTable.createRow();
            final String name = "name" + id;
            row.put("id", id);
            row.put("name", name);
            row.put("data", getBinaryData(BINARY_LEN));
            return row;
        }
    }

    private byte[] buffer = null;

    private byte[] getBinaryData(int len) {
        if (buffer != null) {
            return buffer;
        }
        buffer = new byte[len];
        for (int i = 0; i < len; i++) {
            buffer[i] = (byte)('A' + i % 26);
        }
        return buffer;
    }

    private static abstract class LoadStream<E> implements EntryStream<E> {

        private final String name;
        private final long index;
        private final long max;
        private final long min;
        private long id;
        private long count;
        private final AtomicLong keyExistsCount;

        LoadStream(String name, long index, long min, long max) {
            this.index = index;
            this.max = max;
            this.min = min;
            this.name = name;
            id = min;
            count = 0;
            keyExistsCount = new AtomicLong();
        }

        abstract E createEntry(long idx);
        abstract String entryToString(E entry);

        @Override
        public String name() {
            return name + index + "- [" + min + ", " + max + ")";
        }

        @Override
        public E getNext() {
            if (id++ == max) {
                return null;
            }
            count++;
            return createEntry(id);
        }

        @Override
        public void completed() {
            System.err.println(name() + " completed, loaded: " + count);
        }

        @Override
        public void keyExists(E entry) {
            keyExistsCount.incrementAndGet();
        }

        @Override
        public void catchException(RuntimeException exception, E entry) {
            System.err.println(name() + " catch exception: " +
                exception.getMessage() + " for entry: " +
                entryToString(entry));
            throw exception;
        }

        public long getCount() {
            return count;
        }

        public long getKeyExistsCount() {
            return keyExistsCount.get();
        }
    }
}
