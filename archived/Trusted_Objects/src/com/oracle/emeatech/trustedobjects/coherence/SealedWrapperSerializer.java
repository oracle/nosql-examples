/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.coherence;

import com.oracle.emeatech.trustedobjects.SealedWrapper;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import java.io.IOException;

/**
 *
 * @author ewan
 */
public class SealedWrapperSerializer implements PofSerializer
{
    private static final int PAYLOAD = 0;

    @Override
    public void serialize(PofWriter writer, Object o) throws IOException
    {
        SealedWrapper sw = (SealedWrapper) o;
        writer.writeByteArray(PAYLOAD, sw.getPayload());
        writer.writeRemainder(null);
    }

    @Override
    public Object deserialize(PofReader reader) throws IOException
    {
        SealedWrapper sw = new SealedWrapper(reader.readByteArray(PAYLOAD));
        reader.readRemainder();
        return sw;
    }
    
}
