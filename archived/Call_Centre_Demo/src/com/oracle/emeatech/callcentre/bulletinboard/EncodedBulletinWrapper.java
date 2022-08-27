package com.oracle.emeatech.callcentre.bulletinboard;

import com.oracle.emeatech.trustedobjects.Domain;
import com.oracle.emeatech.trustedobjects.EncodeableObject;
import com.oracle.emeatech.trustedobjects.EncodedObject;
import com.oracle.emeatech.trustedobjects.SealedWrapper;
import com.oracle.emeatech.trustedobjects.SignedWrapper;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.io.Serializable;
import java.util.Date;

/**
 * Encoded form of bulletin wrapper. Contains the signed, sealed (encrypted)
 * form of the Bulletin.
 *
 * @author ewan
 */
public class EncodedBulletinWrapper implements EncodedObject, Serializable
{
    public static int DATE = 0, SENDER_ID = 1, CONTENT = 2;
    private Date timestamp;
    private String senderId;
    private SealedWrapper sealedSignedContent;

    public EncodedBulletinWrapper(Date timestamp, String senderId, SealedWrapper sealedSignedContent)
    {
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.sealedSignedContent = sealedSignedContent;
    }

    @Override
    public EncodeableObject decode(Domain domain) throws TrustedObjectException
    {
        return new BulletinWrapper((SignedWrapper) domain.unsealObject(this.getSealedSignedContent()));
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp()
    {
        return timestamp;
    }

    /**
     * @return the senderId
     */
    public String getSenderId()
    {
        return senderId;
    }

    /**
     * @return the sealedSignedContent
     */
    public SealedWrapper getSealedSignedContent()
    {
        return sealedSignedContent;
    }
}
