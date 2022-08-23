/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects;

/**
 *
 * @author ewan
 */
public interface EncodedObject
{
    public EncodeableObject decode(Domain domain) throws TrustedObjectException;
}
