package com.oracle.emeatech.callcentre.bulletinboard;

import com.oracle.emeatech.trustedobjects.Domain;
import com.oracle.emeatech.trustedobjects.EncodeableObject;
import com.oracle.emeatech.trustedobjects.EncodedObject;
import com.oracle.emeatech.trustedobjects.SignedWrapper;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.util.Date;

/**
 * Encodeable wrapper for a Signed Bulletin
 * @author ewan
 */
public class BulletinWrapper implements EncodeableObject
{
    //payload
    private SignedWrapper signedContent;

    public BulletinWrapper(SignedWrapper signedContent)
    {
        this.signedContent = signedContent;
    }

    public Date getTimestamp()
    {
        return this.getContent().getTimestamp();
    }

    public String getSenderId()
    {
        return this.getContent().getSenderId();
    }

    @Override
    public EncodedObject encode(Domain domain) throws TrustedObjectException
    {
        return new EncodedBulletinWrapper(this.getTimestamp(), this.getSenderId(), domain.sealObject(this.getSignedContent()));
    }

    /**
     * @return the content
     */
    public Bulletin getContent()
    {
        return (Bulletin) this.getSignedContent().getPayload();
    }

    /**
     * @return the signedContent
     */
    public SignedWrapper getSignedContent()
    {
        return signedContent;
    }

}
