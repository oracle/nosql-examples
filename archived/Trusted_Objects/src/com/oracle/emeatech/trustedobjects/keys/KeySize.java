/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.keys;

import java.security.SecureRandom;
import java.util.Random;

/**
 *
 * @author ewan
 */
public enum KeySize
{
    BITS_64(8), BITS_128(16), BITS_192(24), BITS_256(32), BITS_512(64), BITS_1024(128), BITS_2048(256);

    private int sizeInBytes;

    private KeySize(int aSizeInBytes)
    {
        this.sizeInBytes = aSizeInBytes;
    }

    private int getSizeInBytes()
    {
        return this.sizeInBytes;
    }

    public int getSizeInBits()
    {
        return this.getSizeInBytes() * 8;
    }

    public byte[] getByteArray()
    {
        int nBytes = this.getSizeInBytes();
        byte[] specArray = new byte[nBytes];
        Random r = new SecureRandom();
        for (int i = 0; i < nBytes; i++)
        {
            specArray[i] = (byte) r.nextInt();
        }
        return specArray;
    }
}
