/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.coherence;

import com.oracle.emeatech.trustedobjects.SecretKeyWrapper;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import java.io.IOException;

/**
 *
 * @author ewan
 */
public class SecretKeyWrapperSerializer implements PofSerializer
{

    private static final int ENC_KEY = 0, ENC_SIG = 1, SENDER_ID = 2;

    @Override
    public void serialize(PofWriter writer, Object o) throws IOException
    {
        SecretKeyWrapper skw = (SecretKeyWrapper) o;
        writer.writeByteArray(ENC_KEY, skw.getEncryptedKeyBytes());
        writer.writeByteArray(ENC_SIG, skw.getEncryptedSigBytes());
        writer.writeString(SENDER_ID, skw.getSenderId());
        writer.writeRemainder(null);
    }

    @Override
    public Object deserialize(PofReader reader) throws IOException
    {
        SecretKeyWrapper skw = new SecretKeyWrapper(reader.readByteArray(ENC_KEY), reader.readByteArray(ENC_SIG), reader.readString(SENDER_ID));
        reader.readRemainder();
        return skw;
    }
}
