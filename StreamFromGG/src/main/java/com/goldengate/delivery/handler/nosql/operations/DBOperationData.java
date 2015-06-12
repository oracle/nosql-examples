/*
*
* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
*
*/
package com.goldengate.delivery.handler.nosql.operations;



import java.util.ArrayList;

import java.util.Map;

import oracle.kv.Durability;
import oracle.kv.KVStore;
import oracle.kv.table.TableAPI;
import oracle.kv.table.TableOperation;
import oracle.kv.table.TableOperationFactory;
import oracle.kv.table.WriteOptions;

/**
 * This class servers as a state and object storage class to be used by other
 * classes which do the work.
 * @author Tom Campbell
 */
public class DBOperationData {
    /* Transaction, operation, and operation type metrics */
    private long numOps = 0; //Operations
    private long numTxs = 0; //Transactions
    private long numInserts = 0; //Inserts
    private long numUpdates = 0; //Updates
    private long numDeletes = 0; //Deletes
    
    private NosqlWriter nosqlWriter;
    
    //The operation keys.  These get inserted into the row data in HDFS to
    //indicate the type of the operation.
    private static final Durability durability = Durability.COMMIT_NO_SYNC;
    private String insertOpKey = "I";
    private String updateOpKey = "U";
    private String deleteOpKey = "D";
    private TableAPI tableAPI;
    private ArrayList<TableOperation> toperl;
    private boolean abortOperationOption;
    private TableOperationFactory tof;
    private KVStore kvStore;
    private String host;
    private String port;
    private String storeName;
    private String nosqlTable;
    private ArrayList<String> nosqlCols;
    private ArrayList<String> nosqlPKCols;
    private ArrayList<String> nosqlShardCols;
    private Map<String,String> mappings;
    private String currentpk;
    private String currentshard;
    private String prevpk;
    private String prevshard;
    private WriteOptions tabWOpt = new WriteOptions(durability, 0, null);

    public WriteOptions getTabWOpt() {
        return tabWOpt;
    }

    public void setCurrentpk(String currentpk) {
        this.currentpk = currentpk;
    }

    public String getCurrentpk() {
        return currentpk;
    }

    public void setCurrentshard(String currentshard) {
        this.currentshard = currentshard;
    }

    public String getCurrentshard() {
        return currentshard;
    }

    public void setPrevpk(String prevpk) {
        this.prevpk = prevpk;
    }

    public String getPrevpk() {
        return prevpk;
    }

    public void setPrevshard(String prevshard) {
        this.prevshard = prevshard;
    }

    public String getPrevshard() {
        return prevshard;
    }

    public void setNosqlTable(String nosqlTable) {
        this.nosqlTable = nosqlTable;
    }

    public String getNosqlTable() {
        return nosqlTable;
    }

    public void setNosqlCols(ArrayList<String> nosqlCols) {
        this.nosqlCols = nosqlCols;
    }

    public ArrayList<String> getNosqlCols() {
        return nosqlCols;
    }

    public void setNosqlPKCols(ArrayList<String> nosqlPKCols) {
        this.nosqlPKCols = nosqlPKCols;
    }

    public ArrayList<String> getNosqlPKCols() {
        return nosqlPKCols;
    }

    public void setNosqlShardCols(ArrayList<String> nosqlShardCols) {
        this.nosqlShardCols = nosqlShardCols;
    }

    public ArrayList<String> getNosqlShardCols() {
        return nosqlShardCols;
    }

    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    public Map<String, String> getMappings() {
        return mappings;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void setKvStore(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    public KVStore getKvStore() {
        return kvStore;
    }

    public void setTof(TableOperationFactory tof) {
        this.tof = tof;
    }

    public TableOperationFactory getTof() {
        return tof;
    }

    public void setTableAPI(TableAPI tableAPI) {
        this.tableAPI = tableAPI;
    }

    public TableAPI getTableAPI() {
        return tableAPI;
    }

    public void setToperl(ArrayList<TableOperation> toperl) {
        this.toperl = toperl;
    }

    public ArrayList<TableOperation> getToperl() {
        return toperl;
    }

    public void setAbortOperationOption(boolean abortOperationOption) {
        this.abortOperationOption = abortOperationOption;
    }

    public boolean isAbortOperationOption() {
        return abortOperationOption;
    }

    /**
     * Create a new DBOperation Data instance.
     */
    public DBOperationData(KVStore _kvStore, TableAPI _tableAPI, ArrayList<TableOperation> _toperl,boolean _abortOpOption){
        toperl = _toperl;
        tableAPI = _tableAPI;
        abortOperationOption = _abortOpOption;
        nosqlWriter = new NosqlWriter();
        kvStore = _kvStore;
    }
    
    public DBOperationData(){
        nosqlWriter = new NosqlWriter();
        
    }
    
    /**
     * Method to get the count of operations processed.
     * @return Count of operations.
     */
    public long getNumOps(){
        return numOps;
    }
    
    /**
     * Method to get the count of transactions processed.
     * @return Count of transactions.
     */
    public long getNumTxs(){
        return numTxs;
    }
    
    /**
     * Method to increment the count of transactions processed.
     */
    public void incrementNumTxs(){
        numTxs++;
    }
    
    /**
     * Method to get the count of insert operations processed.
     * @return Count of insert operations.
     */
    public long getNumInserts(){
        return numInserts;
    }
    
    /**
     * Method to increment the count of insert operations processed.
     */
    public void incrementNumInserts(){
        numOps++;
        numInserts++;
    }
    
    /**
     * Method to get the count of update operations processed.
     * @return Count of update operations.
     */
    public long getNumUpdates(){
        return numUpdates;
    }
    
    /**
     * Method to increment the count of update operations processed.
     */
    public void incrementNumUpdates(){
        numOps++;
        numUpdates++;
    }
    
    /**
     * Method to get the count of delete operations processed.
     * @return Count of delete operations.
     */
    public long getNumDeletes(){
        return numDeletes;
    }
    
    /**
     * Method to increment the count of delete operations processed.
     */
    public void incrementNumDeletes(){
        numOps++;
        numDeletes++;
    }
    
    /**
     * Method to get the HDFS Writer object.  This object encapsulates the HDFS
     * writing functionality to HDFS.
     * @return The HDFS writer object.
     */
    public NosqlWriter getNosqlWriter(){
        return nosqlWriter;
    }
    


    
    /**
     * Method to get the operation key for inserts.  The operation key is 
     * inserted into the row data in the HDFS file.
     * @return The insert operation key.
     */
    public String getInsertOpKey(){
        return insertOpKey;
    }
    
    /**
     * Method to get the operation key for updates.  This operation key is
     * inserted into the row data in the HDFS file.
     * @return The update operation key.
     */
    public String getUpdateOpKey(){
        return updateOpKey;
    }
    
    /**
     * Method to get the operation key for deletes.  This operation key is
     * inserted into the row data in the HDFS file.
     * @return The delete operation key.
     */
    public String getDeleteOpKey(){
        return deleteOpKey;
    }
}
