package com.oracle.emeatech.trustedobjects.nosql;

import com.oracle.emeatech.trustedobjects.keys.KeySize;
import com.oracle.emeatech.trustedobjects.Scheme;

/**
 * Implementation of Scheme returning hard coded values for testing purposes.
 * @author ewan
 */
class NoSQLScheme implements Scheme
{
    private static final String ASYMM_CIPHER = "RSA/None/NoPadding";
    private static final String SYMM_CIPHER = "AES/ECB/PKCS7Padding";
    private static final String RSA = "RSA";
    private static final String AES = "AES";

    @Override
    public String getAsymmetricCipherAlgorithm()
    {
        return ASYMM_CIPHER;
    }

    @Override
    public String getSymmetricCipherAlgorithm()
    {
        return SYMM_CIPHER;
    }

    @Override
    public String getSignatureAlgorithm()
    {
        return RSA;
    }

    @Override
    public String getSecretKeyType()
    {
        return AES;
    }

    @Override
    public KeySize getSecretKeySize()
    {
        return KeySize.BITS_192;
    }

    @Override
    public String getAsymmetricKeyType()
    {
        return RSA;
    }

    /**
     * Factory method
     * @return a default implementation of Scheme
     */
    public static Scheme getInstance()
    {
        return new NoSQLScheme();
    }

}
