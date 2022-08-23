package com.oracle.emeatech.trustedobjects;

/**
 *
 * @author ewan
 */
public class TrustedObjectException extends Exception
{
    public TrustedObjectException(String message)
    {
        super(message);
    }

    public TrustedObjectException(Throwable cause)
    {
        super(cause);
    }
    
    public TrustedObjectException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
}
