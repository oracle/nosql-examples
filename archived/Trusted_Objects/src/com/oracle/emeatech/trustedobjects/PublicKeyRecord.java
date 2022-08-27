package com.oracle.emeatech.trustedobjects;

import java.security.PublicKey;

/**
 * Wrapper class for an encoded PublicKey and its algorithm.
 * @author ewan
 */
public class PublicKeyRecord
{

    private byte[] encodedKey;
    private String algorithm;


    public PublicKeyRecord(byte[] encodedKey, String algorithm)
    {
        this.encodedKey = encodedKey;
        this.algorithm = algorithm;        
    }
    
    public PublicKeyRecord(PublicKey aPublicKey)
    {
        this.encodedKey = aPublicKey.getEncoded();
        this.algorithm = aPublicKey.getAlgorithm();
    }

    /**
     * @return the encodedKey
     */
    public byte[] getEncodedKey()
    {
        return encodedKey;
    }

    /**
     * @return the algorithm
     */
    public String getAlgorithm()
    {
        return algorithm;
    }

}
