/*
*
* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
*
*/
package com.goldengate.delivery.handler.nosql.operations;

import com.goldengate.atg.datasource.DsOperation.OpType;
import com.goldengate.delivery.handler.nosql.operations.InsertDBOperation;

/**
 * Factory class to get the operation handler based on the specific operation type.
 * @author Tom Campbell
 */
public class DBOperationFactory {
    private InsertDBOperation insertOperation;
    private DeleteDBOperation deleteOperation;
    private UpdateDBOperation updateOperation;
    private PKUpdateDBOperation pkupdateOperation;

    /**
     * Initialization method.  Used to initialize all of the supported handlers.
     * @param od The operation data object.
     */
    public void init(DBOperationData od){
        insertOperation = new InsertDBOperation();
        insertOperation.setOperationData(od);
        
        deleteOperation = new DeleteDBOperation();
        deleteOperation.setOperationData(od);
        
        updateOperation = new UpdateDBOperation();
        updateOperation.setOperationData(od);
        
        pkupdateOperation = new PKUpdateDBOperation();
        pkupdateOperation.setOperationData(od);
    }
    
    /**
     * Method to get the operation handler based on the input operation type.
     * @param ot The operation type.
     * @return The operation handler for the input type.  Null if the type is
     * not supported.
     */
    public AbstractDBOperation getDBOperation(OpType ot){
        if (ot == OpType.DO_INSERT){
            return insertOperation;
        }
        if (ot == OpType.DO_DELETE){
            return deleteOperation;
        }
        if ((ot == OpType.DO_UPDATE) || (ot == OpType.DO_UPDATE_FIELDCOMP) ||
                (ot == OpType.DO_UPDATE_AC)){
            return updateOperation;
        }
        if (ot == OpType.DO_UPDATE_FIELDCOMP_PK){
            return pkupdateOperation;
        }
        
        return null;
    }
}
