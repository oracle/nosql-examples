/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.coherence;

import com.oracle.emeatech.trustedobjects.SignedWrapper;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import java.io.IOException;

/**
 *
 * @author ewan
 */
public class SignedWrapperSerializer implements PofSerializer
{
    private static final int PAYLOAD = 0, SIGNATURE = 1, SIGLEN = 2, SENDER_ID = 3;

    @Override
    public void serialize(PofWriter writer, Object o) throws IOException
    {
        SignedWrapper sw = (SignedWrapper) o;
        writer.writeObject(PAYLOAD, sw.getPayload());
        writer.writeByteArray(SIGNATURE, sw.getSignature());
        writer.writeInt(SIGLEN, sw.getSiglen());
        writer.writeString(SENDER_ID, sw.getSignatoryId());
        writer.writeRemainder(null);
    }

    @Override
    public Object deserialize(PofReader reader) throws IOException
    {
        SignedWrapper sw = new SignedWrapper(
                reader.readObject(PAYLOAD),
                reader.readByteArray(SIGNATURE),
                reader.readInt(SIGLEN),
                reader.readString(SENDER_ID)
                );
        reader.readRemainder();
        return sw;
    }
}
