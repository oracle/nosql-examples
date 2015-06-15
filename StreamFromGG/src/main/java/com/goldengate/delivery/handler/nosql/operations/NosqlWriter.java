/*
*
* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
*
*/
package com.goldengate.delivery.handler.nosql.operations;

import com.goldengate.atg.datasource.DsOperation.OpType;
import static com.goldengate.atg.datasource.GGDataSource.Status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import oracle.kv.table.PrimaryKey;
import oracle.kv.table.ReturnRow;
import oracle.kv.table.Row;


import oracle.kv.table.TableAPI;
import oracle.kv.table.TableOperation;

import oracle.kv.table.TableOperationFactory;

import oracle.kv.table.WriteOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encapsulates the data writing interaction with the Hadoop Distributed
 * File System (Nosql).  
 * @author tbcampbe
 */
public class NosqlWriter {
    final private static Logger logger = LoggerFactory.getLogger(NosqlWriter.class);

     private final long defaultFileSize = 1024 * 1024 * 1024; //Default 1GB

    /**
     * Method to open the output file stream for writing to Nosql.  This method must be
     * invoked prior to any write call.  Once the file is open, rolling to a new
     * file when the current file is full will occur on the write call.
     * @throws IOException An exception occurred opening the Nosql file.
     */
    private static final WriteOptions writeOpt = new WriteOptions(null,0,null);

    public void open() throws IOException{
        try{
        //TODO
        }catch(Exception ioe){
            logger.error("Failed to create the Nosql output", ioe);
            throw new IOException("Failed to create the Nosql output ", ioe);
        }
    }
    
    /**
     * Method to close the output file stream for writing to Nosql.  This method should
     * be called on shutdown.  It first syncs the stream and then closes the stream.
     * Once the close method is invoked, the write method should not be called.
     * @throws IOException An exception occurred closing the Nosql file.
     */
    public void close() throws IOException{
        try{
            logger.debug("Closing the Nosql ");
            //This should perform an hflush, pushing data all the way to the device
            //This call could be expensive.

        }catch(Exception ioe){
            logger.error("Failed to close the Nosql", ioe);
            throw new IOException("Failed to close the Nosql", ioe);
        }finally{
        }
    }
    
    /**
     * Write a string to the Nosql file in the native system encoding.  This method
     * will roll to a new file if writing the current string value will exceed the 
     * maximum file size.
     * @param str The string to be written to Nosql.
     * @return Status.OK for success, else Status.ABEND
     */
    private void computeShardAndPk(Row row, PrimaryKey pk, DBOperationData dboperationdata){
        ArrayList<String> pkcols = dboperationdata.getNosqlPKCols();
        ArrayList<String> shardcols = dboperationdata.getNosqlShardCols();
        String pkstr = "";
        String shardstr = "";
        
        if(row!=null){
            
            for(String colval: pkcols){
             String   val =  row.get(colval).asString().get();
             pkstr += val+",";
            }
            
            for(String colval: shardcols){
             String   val =  row.get(colval).asString().get();
             shardstr += val+",";
            }
        }else{ //pk is not null
           for(String colval: pkcols){
             String   val =  pk.get(colval).asString().get();
             pkstr += val+",";
           }
           
           for(String colval: shardcols){
             String   val =  pk.get(colval).asString().get();
             shardstr += val+",";
           }
        }
        //logger.info("computeShardAndPk: shard"+ shardstr+ " pk:"+pkstr);
        dboperationdata.setCurrentpk(pkstr);
        dboperationdata.setCurrentshard(shardstr);
    }
    
    public Status write(Row row, PrimaryKey pk, DBOperationData dboperationdata,OpType ot){
        if(dboperationdata.getCurrentpk()==null&&dboperationdata.getPrevpk()==null){
            //
            computeShardAndPk(row, pk, dboperationdata);            
            dboperationdata.setPrevpk(dboperationdata.getCurrentpk());
            dboperationdata.setPrevshard(dboperationdata.getCurrentshard());
            
        }
        return  write(row, pk, dboperationdata, dboperationdata.getTof(), dboperationdata.getTableAPI(),dboperationdata.getToperl(), dboperationdata.isAbortOperationOption(), ot);
    }

    public Status write(Row row, PrimaryKey pk, DBOperationData dboperationdata, TableOperationFactory tof, TableAPI tabApi,ArrayList<TableOperation> toperl, boolean isAbortOperOpt,OpType ot){
        Status status = Status.OK;
        TableOperation toper=null;
        computeShardAndPk(row, pk, dboperationdata);
        try{ 
             if (ot == OpType.DO_INSERT){
                 toper = tof.createPut(row, ReturnRow.Choice.NONE, isAbortOperOpt);
                 //logger.info("DO_INSERT:"+toper.toString());
                 //tabApi.put(row,null,writeOpt);
             }
             if (ot == OpType.DO_DELETE){
                 //tabApi.delete(pk,null, writeOpt);
                 toper = tof.createDelete(pk, ReturnRow.Choice.NONE, isAbortOperOpt);
                 //logger.info("DO_DELETE:"+toper.toString());

             }
             if ((ot == OpType.DO_UPDATE) || (ot == OpType.DO_UPDATE_FIELDCOMP) ||
                     (ot == OpType.DO_UPDATE_AC)){
                 toper = tof.createPutIfPresent(row, ReturnRow.Choice.NONE, isAbortOperOpt);  
                 logger.info("DO_UPDATE:"+toper.toString());
                 //tabApi.putIfPresent(row,null,writeOpt);
                 
             }
             if (ot == OpType.DO_UPDATE_FIELDCOMP_PK){
                 logger.info("NO oper");
             }
            //logger.info("write: current shard:"+ dboperationdata.getCurrentshard()+ " prev shard:"+dboperationdata.getPrevshard());
            String prevshard = dboperationdata.getPrevshard();
            if(toper != null){
            if(dboperationdata.getCurrentshard().equals(prevshard)){
                toperl.add(toper);
            }else{
                dboperationdata.getTableAPI().execute(toperl, dboperationdata.getTabWOpt());
                toperl.clear();
                toperl.add(toper);
                dboperationdata.setPrevshard(dboperationdata.getCurrentshard());
                
            }
            }
            dboperationdata.setPrevpk(dboperationdata.getCurrentpk());
         }catch(Exception ioe){
            logger.error("The Nosql write operation using native encoding failed.", ioe);
            status = Status.ABEND;
        }
        return status;
    }
    
    
    /**
     * Private method generate the Nosql file name.  Generated file name will be in the 
     * format {prefix}{timestamp}{suffix}
     * Note:  Nosql does not support colons in file names.
     * @return The generated file name.
     */
    private String generateNosqlFileName(){
        StringBuilder sb = new StringBuilder();
         return sb.toString();
    }
    
    /**
     * Private method to generate a current timestamp.
     * @return The current timestamp.
     */
    private String generateTimeStamp(){
        //Note:  Nosql file names do not support colons
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS");
        return sdf.format(new Date());
    }
    
 
    
}
