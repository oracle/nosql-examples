/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.coherence;

import com.oracle.emeatech.callcentre.paymentcard.DigitField;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import java.io.IOException;

/**
 *
 * @author ewan
 */
public class DigitFieldSerializer implements PofSerializer
{

    private static int DIGITS = 0;

    @Override
    public void serialize(PofWriter writer, Object o) throws IOException
    {
        DigitField df = (DigitField) o;
        writer.writeCharArray(DIGITS, df.getDigits());
        writer.writeRemainder(null);
    }

    @Override
    public Object deserialize(PofReader reader) throws IOException
    {
        DigitField df = new DigitField(reader.readCharArray(DIGITS));
        reader.readRemainder();
        return df;
    }    
}
