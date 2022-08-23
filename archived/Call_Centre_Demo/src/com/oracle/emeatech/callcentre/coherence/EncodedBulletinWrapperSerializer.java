/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.coherence;

import com.oracle.emeatech.callcentre.bulletinboard.EncodedBulletinWrapper;
import static com.oracle.emeatech.callcentre.bulletinboard.EncodedBulletinWrapper.CONTENT;
import static com.oracle.emeatech.callcentre.bulletinboard.EncodedBulletinWrapper.DATE;
import static com.oracle.emeatech.callcentre.bulletinboard.EncodedBulletinWrapper.SENDER_ID;
import com.oracle.emeatech.trustedobjects.SealedWrapper;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PofSerializer;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author ewan
 */
public class EncodedBulletinWrapperSerializer implements PofSerializer
{

    @Override
    public void serialize(PofWriter out, Object o) throws IOException
    {
        EncodedBulletinWrapper ebw = (EncodedBulletinWrapper) o;
        out.writeDate(DATE, ebw.getTimestamp());
        out.writeString(SENDER_ID, ebw.getSenderId());
        out.writeObject(CONTENT, ebw.getSealedSignedContent());
        out.writeRemainder(null);
    }

    @Override
    public Object deserialize(PofReader reader) throws IOException
    {
        EncodedBulletinWrapper ebw = new EncodedBulletinWrapper(
                (Date) reader.readDate(DATE),
                reader.readString(SENDER_ID),
                (SealedWrapper) reader.readObject(CONTENT));
        reader.readRemainder();
        return ebw;
    }
}
