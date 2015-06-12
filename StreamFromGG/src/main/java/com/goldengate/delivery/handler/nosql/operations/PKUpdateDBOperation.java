/*
*
* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
*
*/
package com.goldengate.delivery.handler.nosql.operations;

import static com.goldengate.atg.datasource.GGDataSource.Status;
import com.goldengate.atg.datasource.adapt.Col;
import com.goldengate.atg.datasource.adapt.Op;
import com.goldengate.atg.datasource.adapt.Tx;
import com.goldengate.atg.datasource.meta.TableMetaData;
import com.goldengate.atg.datasource.meta.TableName;
import com.goldengate.atg.datasource.DsOperation.OpType;

import java.util.Map;

import oracle.kv.Consistency;
import oracle.kv.Direction;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableIteratorOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This update operation handler handles the special case when an update also
 * updates a primary key or keys.  Since rows are only added into HDFS this operation
 * gets treated as a delete and then an insert.  To successfully handle this type
 * of operation it is highly recommended that supplemental logging be turned on
 * on the data source.  Supplemental logging ensures that all columns are included
 * in an update transaction and not just the columns that changed.
 * @author tbcampbe
 */
public class PKUpdateDBOperation extends AbstractDBOperation{
    final private static Logger logger = LoggerFactory.getLogger(PKUpdateDBOperation.class);
   
    @Override
    public Status processOp(Tx currentTx, Op op, TableMetaData tMeta) {
        //Increment the operation counters
        operationData.incrementNumUpdates();
        //First process the delete
        Status status = processDelete(currentTx, op, tMeta);
        if (status != Status.OK){
            return status;
        }
        return processInsert(currentTx, op, tMeta);
    }
    
    private Status processDelete(Tx currentTx, Op op, TableMetaData tMeta){
        StringBuilder sb = new StringBuilder();
        //Write the table and schema name
        TableName tname = op.getTableName();
        processTableName(sb, tname);
        //Insert the Operation type key, "D" for delete
        sb.append(operationData.getDeleteOpKey());
        //Insert a timestamp
        sb.append(op.getTimestamp());
        final Consistency consistency = Consistency.NONE_REQUIRED;
        TableIteratorOptions tio =  new TableIteratorOptions( Direction.UNORDERED, consistency, 0, null);

        String tableName = operationData.getNosqlTable();

        Table table = operationData.getTableAPI().getTable(tableName);
        PrimaryKey pk = table.createPrimaryKey();
        Map<String,String> mappings = operationData.getMappings();

        for(Col c: op) {
                //sb.append(tMeta.getColumnName(c.getIndex())); //column name
            
            //Assumes supplemental logging is on, otherwise out of sync
            //sb.append("Col: " + tMeta.getColumnName(i) + "=" + c + "\n");
            //sb.append(c.getBeforeValue());
            String columnName = mappings.get(tMeta.getColumnName(c.getIndex()));
            
            if (operationData.getNosqlPKCols().contains(columnName)){

              //logger.info("Delete PK OPer" + c.getBeforeValue());
              pk.put(columnName,c.getBeforeValue());
            }

        }
        //That is all the data, add the row delimiter
        return operationData.getNosqlWriter().write(null, pk, operationData,OpType.DO_DELETE); 
    }
    
    private Status processInsert(Tx currentTx, Op op, TableMetaData tMeta){
        StringBuilder sb = new StringBuilder();
        //Write the table and schema name
        TableName tname = op.getTableName();
        processTableName(sb, tname);
        //Insert the Operation type key, "I" for insert
        sb.append(operationData.getInsertOpKey());
        //Insert a timestamp
        sb.append(op.getTimestamp());
        String tableName = operationData.getNosqlTable();
        Table table = operationData.getTableAPI().getTable(tableName);
        Row row =table.createRow();
        Map<String,String> mappings = operationData.getMappings();


        for(Col c: op) {
                 //sb.append(tMeta.getColumnName(c.getIndex())); //column name
                 String columnName = mappings.get(tMeta.getColumnName(c.getIndex()));
                 row.put(columnName,c.getAfterValue());
        
            //Assumes supplemental logging is on, otherwise out of sync
            //sb.append("Col: " + tMeta.getColumnName(i) + "=" + c + "\n");
            //sb.append(c.getAfterValue());

        }
        //That is all the data, add the row delimiter
        return operationData.getNosqlWriter().write(row, null, operationData, OpType.DO_INSERT);     
    }
    
    
    
}
