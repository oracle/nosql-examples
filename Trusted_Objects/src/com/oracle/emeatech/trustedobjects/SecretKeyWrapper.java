package com.oracle.emeatech.trustedobjects;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Wrapper for a SecretKey that has been 1. Signed with the sender's private key
 * 2. encrypted with the intended recipient's public key
 *
 * This wrapper is used to distribute a SecretKey securely.
 *
 * @author ewan
 */
public class SecretKeyWrapper
{

    private SignedWrapper signedSecretKey;
    private byte[] encryptedKeyBytes;
    private byte[] encryptedSigBytes;
    private String senderId;

    public SecretKeyWrapper(byte[] eKeyBytes, byte[] eSigBytes, String aSenderId)
    {
        this.encryptedKeyBytes = eKeyBytes;
        this.encryptedSigBytes = eSigBytes;
        this.senderId = aSenderId;
    }

    /**
     * Returns the decrypted SignedWrapper containing the SecretKey and the
     * sender's signature
     *
     * @param anAlgorithm name of the algorithm to be used to decrypt the
     * payload
     * @param aPrivateKey private key to be used for the decryption operation
     * @return a SignedWrapper containing the SecretKey and the Signature
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public SignedWrapper getSignedSecretKey(String anAlgorithm, PrivateKey aPrivateKey)
            throws TrustedObjectException
    {
        if (this.signedSecretKey == null)
        {
            byte[] clearKeyBytes = decrypt(this.getEncryptedKeyBytes(), anAlgorithm, aPrivateKey);
            byte[] clearSigBytes = decrypt(this.getEncryptedSigBytes(), anAlgorithm, aPrivateKey);
            this.signedSecretKey = SignedWrapper.hydrate(clearKeyBytes, clearSigBytes, this.getSenderId());
        }
        return this.signedSecretKey;
    }

    /**
     * Encrypts a signed secret key into a SecretKeyWrapper
     *
     * @param signedSecretKey
     * @param algorithmName
     * @param aPublicKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static SecretKeyWrapper wrapSignedSecretKey(SignedWrapper signedSecretKey, String algorithmName, PublicKey aPublicKey)
            throws TrustedObjectException
    {
        byte[] eKeyBytes = encrypt(signedSecretKey.getPayloadAsBytes(), algorithmName, aPublicKey);
        byte[] eSigBytes = encrypt(signedSecretKey.getSignature(), algorithmName, aPublicKey);
        return new SecretKeyWrapper(eKeyBytes, eSigBytes, signedSecretKey.getSignatoryId());
    }

    /**
     * Encrypts an array of bytes using the specified algorithm and public key.
     *
     * @param bytesToEncrypt cleartext as byte array
     * @param algorithmName algorithm to use for encryption
     * @param aPublicKey public key of the recipient
     * @return ciphertext as byte array
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private static byte[] encrypt(byte[] bytesToEncrypt, String algorithmName, PublicKey aPublicKey)
            throws TrustedObjectException
    {
        try
        {
            Cipher cipher = Cipher.getInstance(algorithmName);
            cipher.init(Cipher.ENCRYPT_MODE, aPublicKey);
            return cipher.doFinal(bytesToEncrypt);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException x)
        {
            throw new TrustedObjectException(x);
        }
    }

    /**
     * Decrypts an array of bytes using the specified algorithm and private key.
     *
     * @param bytesToDecrypt
     * @param algorithmName
     * @param aPrivateKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private static byte[] decrypt(byte[] bytesToDecrypt, String algorithmName, PrivateKey aPrivateKey)
            throws TrustedObjectException
    {
        try
        {
            Cipher cipher = Cipher.getInstance(algorithmName);
            cipher.init(Cipher.DECRYPT_MODE, aPrivateKey);
            return cipher.doFinal(bytesToDecrypt);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException x)
        {
            throw new TrustedObjectException(x);
        }
    }

    /**
     * @return the encryptedKeyBytes
     */
    public byte[] getEncryptedKeyBytes()
    {
        return encryptedKeyBytes;
    }

    /**
     * @return the encryptedSigBytes
     */
    public byte[] getEncryptedSigBytes()
    {
        return encryptedSigBytes;
    }

    /**
     * @return the senderId
     */
    public String getSenderId()
    {
        return senderId;
    }
}
