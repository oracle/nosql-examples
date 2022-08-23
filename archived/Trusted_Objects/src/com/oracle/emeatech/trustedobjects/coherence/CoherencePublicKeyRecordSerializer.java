/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.coherence;

import com.oracle.emeatech.trustedobjects.PublicKeyRecord;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import java.io.IOException;

/**
 *
 * @author ewan
 */
public class CoherencePublicKeyRecordSerializer implements PofSerializer
{
    private static final int ENCODED_KEY = 1, ALGORITHM = 2;
    @Override
    public void serialize(PofWriter writer, Object o) throws IOException
    {
        PublicKeyRecord pkr = (PublicKeyRecord) o;
        writer.writeByteArray(ENCODED_KEY, pkr.getEncodedKey());
        writer.writeString(ALGORITHM, pkr.getAlgorithm());
        writer.writeRemainder(null);
    }

    @Override
    public Object deserialize(PofReader reader) throws IOException
    {
        byte[] encodedKey = reader.readByteArray(ENCODED_KEY);
        String algorithm = reader.readString(ALGORITHM);
        reader.readRemainder();
        return new PublicKeyRecord(encodedKey, algorithm);
    }
    
}
