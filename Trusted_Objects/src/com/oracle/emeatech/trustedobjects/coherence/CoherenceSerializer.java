/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.coherence;

import com.oracle.emeatech.trustedobjects.Serializer;
import com.tangosol.util.ExternalizableHelper;

/**
 *
 * @author ewan
 */
public class CoherenceSerializer implements Serializer
{

    @Override
    public Object deserialize(byte[] bytes)
    {
        return ExternalizableHelper.fromByteArray(bytes);
    }

    @Override
    public byte[] serialize(Object object)
    {
        return ExternalizableHelper.toByteArray(object);
    }
    
}
