/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects;

/**
 *
 * @author ewan
 */
public interface Serializer
{

    public Object deserialize(byte[] bytes) throws TrustedObjectException;

    public byte[] serialize(Object object) throws TrustedObjectException;
}
