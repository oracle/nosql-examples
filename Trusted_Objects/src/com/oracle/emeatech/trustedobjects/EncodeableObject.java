package com.oracle.emeatech.trustedobjects;

/**
 *
 * @author ewan
 */
public interface EncodeableObject
{
    public EncodedObject encode(Domain domain) throws TrustedObjectException;
}
