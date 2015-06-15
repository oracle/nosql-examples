/*
*
* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
*
*/
package com.goldengate.delivery.handler.nosql;

import static com.goldengate.atg.datasource.GGDataSource.Status;
import com.goldengate.atg.datasource.TxOpMode;
import com.goldengate.atg.datasource.adapt.Op;
import com.goldengate.atg.datasource.adapt.Tx;
import com.goldengate.atg.datasource.meta.DsMetaData;
import com.goldengate.atg.datasource.meta.TableMetaData;
import com.goldengate.atg.datasource.meta.TableName;
import com.goldengate.delivery.handler.nosql.operations.AbstractDBOperation;
import com.goldengate.delivery.handler.nosql.operations.DBOperationFactory;
import com.goldengate.atg.datasource.AbstractHandler;
import com.goldengate.atg.datasource.DsConfiguration;
import com.goldengate.atg.datasource.DsEvent;
import com.goldengate.atg.datasource.DsOperation;
import com.goldengate.atg.datasource.DsTransaction;
import com.goldengate.delivery.handler.nosql.operations.DBOperationData;
import com.goldengate.atg.util.ConfigException;

import java.io.IOException;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.HashMap;
import java.util.Map;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.table.TableAPI;

import oracle.kv.table.TableOperation;

import oracle.kv.table.TableOperationFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Example of a GoldenGate Java adapter for loading data into Oracle Nosql.
 * The Hadoop website:  http://hadoop.apache.org/
 * 
 * This example was written to provide customers with a functioning template from which 
 * they can modify and extend to fulfill their specific needs.  This Nosql example
 * provides a lot of functionality in a relatively compact code base.  This example was
 * developed to provide customers ready-made functionality, while at time maintaining
 * simplicity so that the example is easily understood by users.  There are several known
 * limitations of this example which are detailed below.
 * 
 * Overview:
 * The Nosql handler takes unfiltered operations from the source trail file and insert them
 * Nosql.   
 * Configuration:
 * The following items can be configured in the extract properties file:
 * 1.  gg.handler.{handler name}.NosqlStore
 * The default is "kvstore".  
 * 2.  gg.handler.{handler name}.NosqlUrl
 * <host>:<port>, The default is "bigdatalite:5000".  
 * 3.  gg.handler.{handler name}.NosqlTable
 * The Nosql Table name.
 * 4.  gg.handler.{handler name}.NosqlCols
 * comma separated columns, no default
 * 5.  gg.handler.{handler name}.NosqlColMappings
 * tab separated pairs <source col>,<nosqlcol> no default
 * 6.  gg.handler.{handler name}.NosqlTablePkCols
 * comma separated pk columns, no default
 * 7.  gg.handler.{handler name}.NosqlTableShardCols
 * comma separated shard columns, default is the pkcols
 * 
 * 
 * @author G Arango
 */
public class NosqlHandler  extends AbstractHandler {
    final private static Logger logger = LoggerFactory.getLogger(NosqlHandler.class);

    public static String KVSTORE_NAME = "kvstore";
    public static String KVSTORE_URL = "bigdatalite:5000";
    private static final boolean abortOperationOption = false;

    private DBOperationData operationData;
    private DBOperationFactory operationFactory;
    private TableAPI tableAPI;
    private KVStore kvStore;
    private ArrayList<TableOperation> toperl =
        new ArrayList<TableOperation>();
    private TableOperationFactory tof = null;

    /**
     * Create an instance of the Nosql handler.
     */
    public NosqlHandler() {
        super(TxOpMode.op); // define default mode for handler
        logger.info("Created Nosql handler: default mode=" + getMode() + ".");
        operationData = new DBOperationData();
    }

    @Override
    public void init(DsConfiguration conf, DsMetaData metaData) {
        super.init(conf, metaData);  // always call 'super'
        //logger.info("Initializing Nosql handler: mode=" + getMode()+ ".");

        // Initialize NOSQL
        try {
           // kvStore = KVStoreFactory.getStore(new KVStoreConfig(KVSTORE_NAME, KVSTORE_URL));                    
            kvStore =
                KVStoreFactory.getStore(new KVStoreConfig(operationData.getStoreName(), operationData.getHost()+":"+operationData.getPort()));
            //logger.info("KVStore " + kvStore.getStats(true).toString()+ ".");
            tableAPI = kvStore.getTableAPI();
            tof = tableAPI.getTableOperationFactory();
            operationData.getNosqlWriter().open();
            operationFactory = new DBOperationFactory();
            operationFactory.init(operationData);
            operationData.setTableAPI(tableAPI);
            operationData.setToperl(new ArrayList<TableOperation>());
            operationData.setTof(tof);
            operationData.setAbortOperationOption(abortOperationOption);
            operationData.setKvStore(kvStore);
            operationData.setCurrentpk(null);
            operationData.setCurrentshard(null);
            operationData.setPrevpk(null);
            operationData.setPrevshard(null);
            } 
            catch (Exception ioe) {
            logger.error("Failed to initialize the Nosql handler.", ioe);
            throw new ConfigException("Failed to initialize the Nosql handler.", ioe);
        }

    }


    /* BEGIN Property setter methods. */

    
    public void setNosqlUrl(String cd){
        //logger.info("Setting the nosql url to [" + cd + "].");
        String [] splits = cd.split(":");
        operationData.setPort(splits[1]);
        operationData.setHost(splits[0]);
    }

    public void setNosqlStore(String cd){
        //logger.info("Setting the nosql store name to [" + cd + "].");
        operationData.setStoreName(cd);
    }
    
    public void setNosqlTable(String tab){
        //logger.info("Setting the nosql table name to [" + tab + "].");
        operationData.setNosqlTable(tab);
    }
    
    public void setNosqlCols(String cvscols){
        //logger.info("Setting the nosql column names to [" + cvscols + "].");
        String [] splits = cvscols.split(",");
         ArrayList<String> allWords = new ArrayList<String>();
         allWords.addAll(Arrays.asList(splits));
        operationData.setNosqlCols(allWords);
    }
     
    public void setNosqlPKCols(String cvspkcols){
        //logger.info("Setting the nosql primary key column names to [" + cvspkcols + "].");
        String [] splits = cvspkcols.split(",");
         ArrayList<String> allWords = new ArrayList<String>();
         allWords.addAll(Arrays.asList(splits));
        operationData.setNosqlPKCols(allWords);
    }
    
    public void setNosqlShardCols(String cvsshardcols){
        //logger.info("Setting the nosql shard column names to [" + cvsshardcols + "].");
        String [] splits = cvsshardcols.split(",");
         ArrayList<String> allWords = new ArrayList<String>();
         allWords.addAll(Arrays.asList(splits));
        operationData.setNosqlShardCols(allWords);
    }
    
    public void setNosqlMappings(String cvsmappings){
        //logger.info("Setting the nosql column mappings to [" + cvsmappings + "].");
        String [] pairs = cvsmappings.split(";");
        Map<String,String> mappings = new HashMap<String,String>();
        for(String pair: pairs){
         String [] maprel = pair.split(",");
         mappings.put(maprel[0],maprel[1]);
        }
        operationData.setMappings(mappings);
        //logger.info("Setting the nosql column mappings to [" + mappings.toString() + "].");
    }
    /* END Property setter methods. */
    
    @Override
    public Status transactionBegin(DsEvent e, DsTransaction tx) {
        logger.debug("Invoked Nosql Handler method \"transactionBegin\".");
        super.transactionBegin(e, tx);
        if (logger.isDebugEnabled()){
            String eventFormat;
            eventFormat = String.format("Received begin tx event, numTx=%d : position=%s", 
                    operationData.getNumTxs() , tx.getTranID());
            logger.debug("Received being transaction event, numTx=");
        }
        return Status.OK;
    }

    @Override
    public Status operationAdded(DsEvent e, DsTransaction transaction, DsOperation operation) {
        logger.debug("Invoked Nosql Handler method \"operationAdded\".");
        Status status = Status.OK;
        super.operationAdded(e, transaction, operation);

        if(isOperationMode()) {
            // Tx/Op/Col adapters wrap metadata & values behind a single, simple
            // interface if using the DataSourceListener API (via AbstractHandler).
            final Tx tx = new Tx(transaction, getMetaData(), getConfig());
            final TableMetaData tMeta = getMetaData().getTableMetaData(operation.getTableName());
            final Op op = new Op(operation, tMeta, getConfig());
            status = processOp(tx, op); // process data...
        }

        return status;
    }

    @Override
    public Status transactionCommit(DsEvent e, DsTransaction transaction) {
        logger.debug("Invoked Nosql Handler method \"transactionCommit\".");
        super.transactionCommit(e, transaction);
        Status status = Status.OK;
        //Increment the number of transactions
        operationData.incrementNumTxs();

        Tx tx = new Tx(transaction, getMetaData(), getConfig());

        // In 'operation mode', all the operations would have been processed when
        // 'operationAdded' is called. In 'transaction mode', they are processed
        // when the commit event is received.
        if(!isOperationMode()) {
            for(Op op: tx) {
                status = processOp(tx, op); // process data...
                if (status != Status.OK){
                    //Break out of this loop
                    break;
                }
            }
        }
        
        if (status == Status.OK){
            logger.debug("Calling Nosql hflush for transaction commit pos=" + tx.getTranID());
            // Transaction is complete.  Flush the data.  Probably safer to hsync but the 
            // performance is terrible.
        }

        if (logger.isDebugEnabled()){
            logger.debug("  Received transaction commit event, transaction count=" 
                    + operationData.getNumTxs()
                    + ", pos=" + tx.getTranID()
                    + " (total_ops= "+ tx.getTotalOps()
                    + ", buffered="+ tx.getSize() + ")"
                    + ", ts=" + tx.getTimestamp());
        }

        return status;
    }

    /**
     * Private method to distribute the current operation to a handler and write the
     * operation data to an Nosql file.
     * @param currentTx The current transaction.
     * @param op The current operation.
     * @return Status.OK on success, else Status.ABEND
     */
    private Status processOp(Tx currentTx, Op op) {
        if(logger.isDebugEnabled()){ 
            logger.debug("Process operation: table=[" + op.getTableName() + "]"
                + ", op pos=" + op.getPosition()
                + ", tx pos=" + currentTx.getTranID()
                + ", op ts=" + op.getTimestamp());
        }
        
        TableName  tname = op.getTableName();
        TableMetaData tMeta = getMetaData().getTableMetaData(tname);
        AbstractDBOperation operation = operationFactory.getDBOperation(op.getOpType());
        if (operation == null){
            logger.error("The operation type " + op.getOpType()
                + " on  operation: table=[" + op.getTableName() + "]"
                + ", op pos=" + op.getPosition()
                + ", tx pos=" + currentTx.getTranID()
                + ", op ts=" + op.getTimestamp() 
                + " resulted in a null operation handler.  The operation is not supported by the Nosql Handler.");
            return Status.ABEND;
        }

        Status status =  operation.processOp(currentTx, op, tMeta);
        if (status != Status.OK){
            logger.error("Failed to Process operation: table=[" + op.getTableName() + "]"
                + ", op pos=" + op.getPosition()
                + ", tx pos=" + currentTx.getTranID()
                + ", op ts=" + op.getTimestamp());
        }
        return status;
    }

    @Override
    public Status metaDataChanged(DsEvent e, DsMetaData meta) {
        logger.debug("Invoked Nosql Handler method \"metaDataChanged\".");
        super.metaDataChanged(e, meta);
        logger.debug("Received metadata event: " + e + "; current tables: " + meta.getTableNames().size());
        return Status.OK;
    }

    @Override
    public void destroy() {
        logger.debug("Invoked Nosql Handler method \"destroy\".");
        //Clean up
        try {
            //operationData.getTof().
            ArrayList<TableOperation> toperl = operationData.getToperl();
            if (toperl.size()>0) {
                operationData.getTableAPI().execute(toperl, operationData.getTabWOpt());
            }
                                     
            operationData.getToperl().clear();
            operationData.getKvStore().close();
            operationData.getNosqlWriter().close();
            logger.info("NosqlHandler close"+reportStatus());
        } catch (Exception ioe) {
            logger.error("A failure occurred closing Nosql DB.", ioe);
        }
        super.destroy();
    }

    @Override
    public String reportStatus() {
        logger.debug("Invoked Nosql Handler method \"reportStatus\".");
        StringBuilder sb = new StringBuilder();
        sb.append("Status report: mode=").append(getMode());
        sb.append(", transactions=").append(operationData.getNumTxs());
        sb.append(", operations=").append(operationData.getNumOps());
        sb.append(", inserts=").append(operationData.getNumInserts());
        sb.append(", updates=").append(operationData.getNumUpdates());
        sb.append(", deletes=").append(operationData.getNumDeletes());
        return sb.toString();
    }
    
}


