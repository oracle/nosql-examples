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
 * The delete operation handler prepares a delete operation row and then calls
 * the HDFS writer to write it to HDFS.
 * @author Tom Campbell
 */
public class DeleteDBOperation extends AbstractDBOperation{
    final private static Logger logger = LoggerFactory.getLogger(DeleteDBOperation.class);
    
    @Override
    public Status processOp(Tx currentTx, Op op, TableMetaData tMeta) {
        //Increment the operation counters
        operationData.incrementNumDeletes();
        
        StringBuilder sb = new StringBuilder();
        //Write the table and schema name
        TableName tname = op.getTableName();
        processTableName(sb, tname);
        //Insert the operation type key, "D" for delete
        sb.append(operationData.getDeleteOpKey());
        final Consistency consistency = Consistency.NONE_REQUIRED;
        TableIteratorOptions tio =  new TableIteratorOptions( Direction.UNORDERED, consistency, 0, null);
 
        String tableName = operationData.getNosqlTable();
        Table table = operationData.getTableAPI().getTable(tableName);
        PrimaryKey pk = table.createPrimaryKey();
        Map<String,String> mappings = operationData.getMappings();
 
        //Insert a timestamp
        sb.append(op.getTimestamp());

        for(Col c: op) {
                 sb.append(tMeta.getColumnName(c.getIndex())); //column name
 
            //Assumes supplemental logging is on, otherwise out of sync
            //sb.append("Col: " + tMeta.getColumnName(i) + "=" + c + "\n");
            sb.append(c.getBeforeValue());
            String columnName = mappings.get(tMeta.getColumnName(c.getIndex()));
            if (c.getBeforeValue()!=null&& c.getBeforeValue().length()>0){

               // logger.info("Delete OPer" + c.getBeforeValue());
              pk.put(columnName,c.getBeforeValue());
            }
        }
        //logger.info("Delete OPer" + sb.toString());
        //That is all the data, add the row delimiter
        return operationData.getNosqlWriter().write(null, pk, operationData,op.getOperationType()); 
    }
    
}
