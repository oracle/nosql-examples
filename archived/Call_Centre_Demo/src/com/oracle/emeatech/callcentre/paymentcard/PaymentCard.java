/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.paymentcard;

import com.oracle.emeatech.trustedobjects.Domain;
import com.oracle.emeatech.trustedobjects.EncodeableObject;
import com.oracle.emeatech.trustedobjects.EncodedObject;
import com.oracle.emeatech.trustedobjects.SealedWrapper;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.util.Date;

/**
 * This class represents a Payment Card (e.g. AMEX or VISA).
 * @author ewan
 */
public class PaymentCard implements EncodeableObject
{

    private DigitField cardNumber;
    private DigitField cvv;
    private Date expiryDate;
    private String nameOnCard;
    private CardType cardType;

    public char[] getCVV()
    {
        return this.cvv.getDigits();
    }

    public char[] getCardNumber()
    {
        return this.cardNumber.getDigits();
    }

    public CardType getCardType()
    {
        return this.cardType;
    }

    public Date getExpiryDate()
    {
        return this.expiryDate;
    }

    public String getNameOnCard()
    {
        return this.nameOnCard;
    }

    PaymentCard(DigitField cardNumber, DigitField cvv, Date expiryDate, String nameOnCard, CardType cardType)
    {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
        this.nameOnCard = nameOnCard;
        this.cardType = cardType;
    }

    public static PaymentCard getPaymentCard(DigitField cardNumber, DigitField cvv, Date expiryDate, String nameOnCard, CardType cardType)
            throws PaymentCardException
    {
        if (cardNumber.getLength() != cardType.getCardNumberLength())
        {
            throw new PaymentCardException("Incorrect card number length.  Should be "
                    + cardType.getCardNumberLength() + " digits, but found " + cardNumber.getLength());
        }
        if (cvv.getLength() != cardType.getCvvLength())
        {
            throw new PaymentCardException("Incorrect card number length.  Should be "
                    + cardType.getCvvLength() + " digits, but found " + cvv.getLength());
        }
        return new PaymentCard(cardNumber, cvv, expiryDate, nameOnCard, cardType);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Card Type   : ");
        sb.append(getCardType());
        sb.append("\n");
        sb.append("Card Number : ");
        sb.append(getCardNumber());
        sb.append("\n");
        sb.append("CVV         : ");
        sb.append(getCVV());
        sb.append("\n");
        sb.append("Name On Card: ");
        sb.append(getNameOnCard());
        sb.append("\n");
        sb.append("Expiry Date : ");
        sb.append(getExpiryDate());
        return sb.toString();
    }

    @Override
    public EncodedObject encode(Domain domain) throws TrustedObjectException
    {
        SealedWrapper sealedCardNumber = domain.sealObject(this.cardNumber);
        SealedWrapper sealedCvv = domain.sealObject(this.cvv);
        return new EncodedPaymentCard(sealedCardNumber, sealedCvv, getExpiryDate(), getNameOnCard(), getCardType());
    }
}
