/*-
 * Copyright (c) 2023 Oracle and/or its affiliates.  All rights reserved.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 *  https://oss.oracle.com/licenses/upl/
 */

import java.util.Set;

import oracle.kv.Direction;
import oracle.kv.KVLocal;
import oracle.kv.KVLocalConfig;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.ReadOptions;
import oracle.kv.table.WriteOptions;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;
import oracle.kv.table.TableIteratorOptions;

import oracle.kv.table.TableIterator;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.FieldRange;
import oracle.kv.table.RecordValue;


import oracle.kv.table.FieldValue;

import oracle.kv.StatementResult;
import oracle.kv.table.TableAPI;
import oracle.kv.FaultException;

public class Quickstart {
    KVLocal local = null;
    TableAPI tableAPI;

    private static void usage() {
        System.out.println("None yet");
        System.exit(-1);
    }

    public static void main(String args[]) {
        Quickstart st = new Quickstart();
        st.run(args);
        System.out.println("All done.");
    }

    private void run(String args[]) {

        String rootDir = "kvroot";
        try {
            final KVLocalConfig config =
                new KVLocalConfig.InetBuilder(rootDir).isSecure(false).build();
            local = KVLocal.start(config);
            tableAPI = local.getStore().getTableAPI();

            System.out.println("In run....");

            defineTables();
            populateTables();
            doGet();
            iterateTables();
			doSQLSelect();
        } finally {
            if (local != null) {
                try {
                    local.stop();
                } catch (Exception e) {}
            }
        }
    }

    private void defineTables() {

        StatementResult result = null;
        String statement = null;

        try {
            //statement = "DROP TABLE IF EXISTS Quickstart";
            //result = store.executeSync(statement);
            //displayResult(result, statement);

            statement =
                "CREATE TABLE IF NOT EXISTS Quickstart ("   +
                "uid INTEGER," +
                "name STRING," +
                "PRIMARY KEY(uid))";
            result = local.getStore().executeSync(statement);
            displayResult(result, statement);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid statement:\n" + e.getMessage());
        } catch (FaultException e) {
            System.out.println
                ("Statement couldn't be executed, please retry: " + e);
        }

    }

    private void populateTables() {
        System.out.println("In populateTables....");

        Table myTable = tableAPI.getTable("Quickstart");

        for (int i = 0; i < 3; i++) {
            Row row = myTable.createRow();
            row.put("uid", i);
            row.put("name", ("name" + i));
            tableAPI.put(row, null, null);
        }
    }

    private void doGet() {
        /* get the "User" table */
        Table table = tableAPI.getTable("Quickstart");
        PrimaryKey pkey = table.createPrimaryKey();
        pkey.put("uid", 1);
        Row row = tableAPI.get(pkey, null);
        System.out.println("Got row: " + row);
    }

    private void iterateTables() {
        System.out.println("######################################################");
        System.out.println("               Now do it with a v1 index   ###########");
        System.out.println("######################################################");

        final TableIteratorOptions options =
            new TableIteratorOptions(Direction.REVERSE, null, 0, null);

        /* get the "User" table, domain is null */
        Table table = tableAPI.getTable("Quickstart");
        PrimaryKey pkey = table.createPrimaryKey();

        TableIterator<Row> iter = tableAPI.tableIterator(pkey, null, options);
        try {
            while (iter.hasNext()) {
                Row row = iter.next();
                System.out.println(row);
            }
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    private void doSQLSelect() {
		// Compile and Execute the SELECT statement
		StatementResult result = local.getStore().executeSync("SELECT uid, name FROM Quickstart ORDER BY uid LIMIT 10");
	
		// Get the results
		System.out.println("######################################################");
		System.out.println("              Now do it with SQL Statement  ###########");
		System.out.println("######################################################");
		for( RecordValue record : result ) {
			System.out.println("JSON format ");
			System.out.println("   " + record.toString());
			System.out.println("Field by field ");
			System.out.println("   uid: " + record.get("uid").asInteger().get());
			System.out.println("   name: " + record.get("name").asString().get());
		}
    }

    private void displayResult(StatementResult result, String statement) {
        System.out.println("===========================");
        if (result.isSuccessful()) {
            System.out.println("Statement was successful:\n\t" + statement);
            System.out.println("Results:\n\t" + result.getInfo());
        } else if (result.isCancelled()) {
            System.out.println("Statement was cancelled:\n\t" + statement);
        } else {
            /*
             * statement wasn't successful: may be in error, or may still be
             * in progress.
             */
            if (result.isDone()) {
                System.out.println("Statement failed:\n\t" + statement);
                System.out.println("Problem:\n\t" + result.getErrorMessage());
            } else {
                System.out.println("Statement in progress:\n\t" + statement);
                System.out.println("Status:\n\t" + result.getInfo());
            }
        }
    }
}
