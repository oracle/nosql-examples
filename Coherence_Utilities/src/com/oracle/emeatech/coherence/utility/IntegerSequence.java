/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.coherence.utility;

import com.tangosol.io.pof.annotation.Portable;
import com.tangosol.io.pof.annotation.PortableProperty;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.processor.NumberIncrementor;

/**
 *
 * @author ewan
 */
@Portable
public class IntegerSequence
{

    public static final int VALUE = 0;
    public static final int INCREMENT = 1;
    @PortableProperty(VALUE)
    private Integer value;
    @PortableProperty(INCREMENT)
    private Integer increment;

    //need a public zero argument constructor apparently!!!
    public IntegerSequence()
    {
    }

    private IntegerSequence(Integer startValue, Integer anIncrement)
    {
        this.increment = anIncrement;
        this.value = startValue;
    }

    public void setValue(Integer aValue)
    {
        this.value = aValue;
    }

    public Integer getValue()
    {
        return this.value;
    }

    public static boolean sequenceExists(String aCacheName, String aSequenceName)
    {
        CacheFactory.ensureCluster();
        NamedCache seqCache = CacheFactory.getCache(aCacheName);
        return IntegerSequence.sequenceExists(seqCache, aSequenceName);
    }

    private static boolean sequenceExists(NamedCache aCache, String aSequenceName)
    {
        if (aCache.containsKey(aSequenceName))
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static void makeSequence(String aCacheName, String aSequenceName, Integer aStartValue, Integer anIncrement) throws SequenceException
    {
        CacheFactory.ensureCluster();
        NamedCache seqCache = CacheFactory.getCache(aCacheName);
        if (IntegerSequence.sequenceExists(seqCache, aSequenceName))
        {
            throw new SequenceException("Could not create Sequence " + aSequenceName + " in cache " + aCacheName + " as it already exists");
        }
        seqCache.put(aSequenceName, new IntegerSequence(aStartValue, anIncrement));
    }

    public static void makeSequence(String aCacheName, String aSequenceName, Integer aStartValue) throws SequenceException
    {
        IntegerSequence.makeSequence(aCacheName, aSequenceName, aStartValue, 1);
    }

    public static Integer getNextSequenceNumber(String aCacheName, String aSequenceName) throws SequenceException
    {
        CacheFactory.ensureCluster();
        NamedCache seqCache = CacheFactory.getCache(aCacheName);

        if (!IntegerSequence.sequenceExists(seqCache, aSequenceName))
        {
            throw new SequenceException("Sequence " + aSequenceName + " not found in cache " + aCacheName);
        }

        //get the increment from the cache - no need to worry about locking
        IntegerSequence intSeq = (IntegerSequence) seqCache.get(aSequenceName);

        //send a NumberIncrementor to the cache
        Integer nextId = (Integer) seqCache.invoke(aSequenceName, new NumberIncrementor("Value", intSeq.getIncrement(), true));
        return nextId;
    }

    /**
     * @return the increment
     */
    public Integer getIncrement()
    {
        return increment;
    }
}
