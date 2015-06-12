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

import oracle.kv.table.Row;
import oracle.kv.table.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The update operation handler prepares a update operation row and then calls
 * the HDFS writer to write it to HDFS.
 * @author tbcampbe
 */
public class UpdateDBOperation extends AbstractDBOperation{
    final private static Logger logger = LoggerFactory.getLogger(UpdateDBOperation.class);

    @Override
    public Status processOp(Tx currentTx, Op op, TableMetaData tMeta) {
        //Increment the operation counters
        operationData.incrementNumUpdates();
        StringBuilder sb = new StringBuilder();
        //Write the schema and table name
        TableName tname = op.getTableName();
        processTableName(sb, tname);
        //Insert the Operation type key, "U" for insert
        //sb.append(operationData.getUpdateOpKey());
        //Insert the timestamp
        //sb.append(op.getTimestamp());
        String tableName = operationData.getNosqlTable();
        Table table = operationData.getTableAPI().getTable(tableName);
        Row row =table.createRow();
        Map<String,String> mappings = operationData.getMappings();
        for(Col c: op) {
            sb.append(tMeta.getColumnName(c.getIndex()));

            //Assumes supplemental logging is on, otherwise out of sync
           // sb.append("Col: " + tMeta.getColumnName(i) + "=" + c + "\n");
            sb.append(c.getAfterValue());
            String columnName = mappings.get(tMeta.getColumnName(c.getIndex()));
            row.put(columnName,c.getAfterValue());
            
        }
        logger.info("Update OPer" + sb.toString());
        //That is all the data, add the row delimiter
        return operationData.getNosqlWriter().write(row, null, operationData,op.getOperationType()); 

    }
    
}
