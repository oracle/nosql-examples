package com.oracle.emeatech.trustedobjects;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.*;

/**
 *
 * @author ewan
 */
public class SealedWrapper implements Serializable
{

    private byte[] payload;
    private static Serializer serializer;

    public SealedWrapper(byte[] payload)
    {
        this.payload = payload;
    }

    private static byte[] getCipherText(byte[] clearText, SecretKey cipherKey, String cipherAlgorithm)
            throws TrustedObjectException
    {
        try
        {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            //initialise the cipher
            cipher.init(Cipher.ENCRYPT_MODE, cipherKey);

            //get the length for the cipherText array
            int ctl = cipher.getOutputSize(clearText.length);

            //create the cipherText array
            byte[] cipherText = new byte[ctl];
            int ctLength = cipher.update(clearText, 0, clearText.length, cipherText, 0);
            cipher.doFinal(cipherText, ctLength);
            return cipherText;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | ShortBufferException | IllegalBlockSizeException | BadPaddingException x)
        {
            throw new TrustedObjectException(x);
        }
    }

    private static byte[] getClearText(byte[] cipherText, SecretKey cipherKey, String cipherAlgorithm)
            throws TrustedObjectException
    {
        try
        {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, cipherKey);
            byte[] clearText = new byte[cipher.getOutputSize(cipherText.length)];
            int ctLength = cipher.update(cipherText, 0, cipherText.length, clearText, 0);
            cipher.doFinal(clearText, ctLength);
            return clearText;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | ShortBufferException | IllegalBlockSizeException | BadPaddingException x)
        {
            throw new TrustedObjectException(x);
        }

    }

    public static Object unseal(SealedWrapper parcel, SecretKey cipherKey, String cipherAlgorithm)
            throws TrustedObjectException
    {
        byte[] cipherText = parcel.getPayload();
        byte[] clearText = getClearText(cipherText, cipherKey, cipherAlgorithm);
        return getSerializer().deserialize(clearText);
    }

    public static SealedWrapper seal(Object forEncryption, SecretKey cipherKey, String cipherAlgorithm)
            throws TrustedObjectException
    {
        byte[] clearText = getSerializer().serialize(forEncryption);
        byte[] cipherText = getCipherText(clearText, cipherKey, cipherAlgorithm);
        SealedWrapper sw = new SealedWrapper(cipherText);
        return sw;
    }

    /**
     * @return the payload
     */
    public byte[] getPayload()
    {
        return payload;
    }

    /**
     * @return the serializer
     */
    private static Serializer getSerializer() throws TrustedObjectException
    {
        if (serializer == null)
        {
            serializer = TrustedObjectService.getInstance().getSerializer();
        }
        return serializer;
    }
}
