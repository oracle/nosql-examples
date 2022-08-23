/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.nosql;

import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.util.Map;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.avro.AvroCatalog;
import oracle.kv.avro.GenericAvroBinding;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

/**
 *
 * @author ewan
 */
public class NoSQLHelper
{

    public static final String LIST_OF_HOSTS = "trustedobjects.service.nosql.hosts";
    public static final String STORE_NAME = "trustedobjects.service.nosql.store";
    private static String[] hosts;
    private static String storeName;
    private static NoSQLHelper instance;
    private KVStore storeHandle;

    private NoSQLHelper()
    {
    }

    public static NoSQLHelper getInstance() throws TrustedObjectException
    {
        if (instance == null)
        {
            if (hosts == null)
            {
                readHostsProperty();
            }
            if (storeName == null)
            {
                storeName = readProperty(STORE_NAME);
            }
            instance = new NoSQLHelper();
        }
        return instance;
    }

    public KVStore getStoreHandle()
    {
        if (storeHandle == null)
        {
            KVStoreConfig kvConf = new KVStoreConfig(getStoreName(), getHosts());
            storeHandle = KVStoreFactory.getStore(kvConf);
        }
        return storeHandle;
    }

    /**
     * @return the hosts
     */
    private static String[] getHosts()
    {
        return hosts;
    }

    private static void readHostsProperty() throws TrustedObjectException
    {
        String[] hostArray = readProperty(LIST_OF_HOSTS).split(",");
        for (int i = 0; i < hostArray.length; i++)
        {
            hostArray[i] = hostArray[i].trim();
        }
        hosts = hostArray;
    }

    private static String readProperty(String propertyName) throws TrustedObjectException
    {
        String propStr = System.getProperty(propertyName);
        if (propStr == null)
        {
            throw new TrustedObjectException("System property " + propertyName + " was not specified");
        }
        return propStr;
    }

    /**
     * @return the storeName
     */
    private String getStoreName()
    {
        return storeName;
    }

    public Schema getSchemaFromCatalog(Object o)
    {
        return this.getSchemaFromCatalog(o.getClass());
    }

    public Schema getSchemaFromCatalog(Class c)
    {
        return this.getSchemaFromCatalog(c.getName());
    }

    public Schema getSchemaFromCatalog(String className)
    {
        //get the avro schema from the store
        Map<String, Schema> schemaMap = getCatalog().getCurrentSchemas();
        return schemaMap.get(className);
    }

    public GenericAvroBinding getBindingForSchema(Schema spcSchema)
    {
        return getCatalog().getGenericBinding(spcSchema);
    }

    public GenericRecord getRecordForSchema(Schema spcSchema)
    {
        return new GenericData.Record(spcSchema);
    }

    public GenericRecord getRecordForObject(Object o)
    {
        return this.getRecordForSchema(this.getSchemaFromCatalog(o));
    }

    public GenericAvroBinding getBindingForObject(Object o)
    {
        return this.getBindingForSchema(this.getSchemaFromCatalog(o));
    }

    public GenericAvroBinding getBindingForClass(Class c)
    {
        return this.getBindingForSchema(this.getSchemaFromCatalog(c));
    }

    public AvroCatalog getCatalog()
    {
        return this.getStoreHandle().getAvroCatalog();
    }
}
