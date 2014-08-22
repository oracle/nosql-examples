/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.coherence;

import com.oracle.emeatech.callcentre.bulletinboard.Bulletin;
import static com.oracle.emeatech.callcentre.bulletinboard.Bulletin.CONTENTS;
import static com.oracle.emeatech.callcentre.bulletinboard.Bulletin.SENDER_ID;
import static com.oracle.emeatech.callcentre.bulletinboard.Bulletin.TIMESTAMP;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import java.io.IOException;

/**
 *
 * @author ewan
 */
public class BulletinSerializer implements PofSerializer
{

    @Override
    public void serialize(PofWriter out, Object o) throws IOException
    {
        Bulletin b = (Bulletin) o;
        out.writeDate(TIMESTAMP, b.getTimestamp());
        out.writeString(SENDER_ID, b.getSenderId());
        out.writeString(CONTENTS, b.getContents());
        out.writeRemainder(null);
    }

    @Override
    public Object deserialize(PofReader in) throws IOException
    {
        Bulletin b = new Bulletin(in.readDate(TIMESTAMP), in.readString(SENDER_ID), in.readString(CONTENTS));
        in.readRemainder();
        return b;
    }
}
