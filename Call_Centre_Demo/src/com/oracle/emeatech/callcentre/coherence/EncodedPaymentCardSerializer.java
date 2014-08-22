/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.coherence;

import com.oracle.emeatech.callcentre.paymentcard.CardType;
import com.oracle.emeatech.callcentre.paymentcard.EncodedPaymentCard;
import com.oracle.emeatech.trustedobjects.SealedWrapper;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author ewan
 */
public class EncodedPaymentCardSerializer implements PofSerializer
{
    
    @Override
    public void serialize(PofWriter writer, Object o) throws IOException
    {
        EncodedPaymentCard epc = (EncodedPaymentCard) o;
        writer.writeObject(EncodedPaymentCard.CARD_NUMBER, epc.getCardNumber());
        writer.writeObject(EncodedPaymentCard.CVV, epc.getCvv());
        writer.writeDate(EncodedPaymentCard.EXPIRY_DATE, epc.getExpiryDate());
        writer.writeString(EncodedPaymentCard.NAME_ON_CARD, epc.getNameOnCard());
        writer.writeObject(EncodedPaymentCard.CARD_TYPE, epc.getCardType());
        writer.writeRemainder(null);
    }

    @Override
    public Object deserialize(PofReader reader) throws IOException
    {
        SealedWrapper cardNumber = (SealedWrapper) reader.readObject(EncodedPaymentCard.CARD_NUMBER);
        SealedWrapper cvv = (SealedWrapper) reader.readObject(EncodedPaymentCard.CVV);
        Date expiryDate =  reader.readDate(EncodedPaymentCard.EXPIRY_DATE);
        String nameOnCard = reader.readString(EncodedPaymentCard.NAME_ON_CARD);
        CardType cardType = (CardType) reader.readObject(EncodedPaymentCard.CARD_TYPE);
        reader.readRemainder();
        return new EncodedPaymentCard(cardNumber, cvv, expiryDate, nameOnCard, cardType);
    }
}
