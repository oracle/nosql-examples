package com.oracle.emeatech.trustedobjects;

import java.io.Serializable;
import java.security.*;

/**
 *
 * @author ewan
 */
public class SignedWrapper implements Serializable
{

    private Object payload;
    private byte[] signature;
    private int siglen;
    private String signatoryId;
    private static Serializer serializer;

    public SignedWrapper(Object payload, byte[] signature, int siglen, String signatoryId)
    {
        this.payload = payload;
        this.signature = signature;
        this.siglen = siglen;
        this.signatoryId = signatoryId;
    }

    private SignedWrapper(Object payload, String signatoryId)
    {
        this.payload = payload;
        this.signatoryId = signatoryId;
    }

    /**
     * @return the payload as bytes
     */
    public byte[] getPayloadAsBytes() throws TrustedObjectException
    {
        return getSerializer().serialize(getPayload());
    }

    /**
     * @return the signature
     */
    public byte[] getSignature()
    {
        return signature;
    }

    /**
     * @return the siglen
     */
    public int getSiglen()
    {
        return siglen;
    }

    /**
     * @return the signatoryId
     */
    public String getSignatoryId()
    {
        return signatoryId;
    }

    public static SignedWrapper sign(Object forSignature, String signatoryId, PrivateKey signingKey, String algorithm)
            throws TrustedObjectException
    {
        try
        {
            SignedWrapper sw = new SignedWrapper(forSignature, signatoryId);
            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(signingKey);
            sig.update(sw.getPayloadAsBytes());
            byte[] sigArr = sig.sign();
            sw.setSignature(sigArr);
            sw.setSiglen(sigArr.length);
            return sw;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException x)
        {
            throw new TrustedObjectException(x);
        }
    }

    /**
     * Factory method to hydrate a SignedWrapper using the serialized forms of
     * the payload and the signature.
     *
     * @param payloadAsBytes payload as POF serialized bytes
     * @param signatureAsBytes payload as POF serialized bytes
     * @param senderId id of the sender
     * @return
     */
    public static SignedWrapper hydrate(byte[] payloadAsBytes, byte[] signatureAsBytes, String senderId) throws TrustedObjectException
    {
        Object payload = getSerializer().deserialize(payloadAsBytes);
        SignedWrapper sw = new SignedWrapper(payload, senderId);
        sw.setSignature(signatureAsBytes);
        sw.setSiglen(signatureAsBytes.length);
        return sw;
    }

    public boolean verifySignature(PublicKey checkKey, String algorithm)
            throws TrustedObjectException
    {
        if (this.getSiglen() != this.getSignature().length)
        {
            return false;
        }
        try
        {
            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(checkKey);
            sig.update(this.getPayloadAsBytes());
            return sig.verify(this.getSignature());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException x)
        {
            throw new TrustedObjectException(x);
        }
    }

    public Object getVerifiedObject(PublicKey checkKey, String algorithm) throws TrustedObjectException
    {
        if (this.verifySignature(checkKey, algorithm))
        {
            return this.getPayload();
        } else
        {
            throw new TrustedObjectException("Bad signature found");
        }
    }

    /**
     * @return the payload
     */
    public Object getPayload()
    {
        return payload;
    }

    /**
     * @param signature the signature to set
     */
    private void setSignature(byte[] signature)
    {
        this.signature = signature;
    }

    /**
     * @param siglen the siglen to set
     */
    private void setSiglen(int siglen)
    {
        this.siglen = siglen;
    }

    @Override
    public String toString()
    {
        return this.getPayload().toString();
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
