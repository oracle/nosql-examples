/*
*
* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
*
*/
package com.goldengate.delivery.handler.nosql.operations;

import static com.goldengate.atg.datasource.GGDataSource.Status;
import com.goldengate.atg.datasource.adapt.Op;
import com.goldengate.atg.datasource.adapt.Tx;
import com.goldengate.atg.datasource.meta.TableMetaData;
import com.goldengate.atg.datasource.meta.TableName;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract base class to handle operations for whose data will be streamed to
 * HDFS.  Implementations for the specific type of transactions such as inserts,
 * updates, deletes, and truncates will extend this class.
 * @author Tom Campbell
 */
public abstract class AbstractDBOperation {
    protected DBOperationData operationData;
    
    /**
     * Method to set the Operation Data object.  The operation data object holds
     * all the state data and HDFS connectivity objects to streaming data to
     * HDFS.
     * @param od The operation data object.
     */
    public void setOperationData(DBOperationData od){
        operationData = od;
    }

    /**
     * Abstract method to process the current operation.  Method formats and output
     * string and then persists the string to HDFS.
     * @param currentTx The current transaction.
     * @param op The current operation.
     * @param tMeta The table metadata of the current operation.
     * @return Status.OK if successful, else Status.ABEND
     */
    public abstract Status processOp(Tx currentTx, Op op, TableMetaData tMeta);
    
    /**
     * Method to generate a current timestamp to be inserted into the record.
     * @return Current timestamp
     */
    protected String generateTimeStamp(){
        //Note:  HDFS file names do not support colons
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
        return sdf.format(new Date());
    }
    
    /**
     * This is a protected method that the derived classes will call to insert
     * the schema name and the table name into stream to be written to HDFS.
     * @param sb The string builder object.
     * @param tname The table name object.
     */
    protected void processTableName(StringBuilder sb, TableName tname){
        //In some odd case it might be possible for the schema name
        //too be null, make sure that case is handled
       /* if (tname.getSchemaName() != null){
            //Switch to lower case, some BigData apps do not do well with upper
            //case (Hive)
            sb.append(tname.getSchemaName().toLowerCase());
        } */
        //Insert a column delimiter

        /*if (tname.getShortName() != null){
            sb.append(tname.getShortName());
        }*/

    }
    
}
