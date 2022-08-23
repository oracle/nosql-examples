/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.paymentcard;

import com.oracle.emeatech.callcentre.paymentcard.utils.CardHelper;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Factory class for creating PaymentCard objects.
 * @author ewan
 */
public class CardFactory
{

    public static PaymentCard newAMEX() throws PaymentCardException
    {
        return newCard(CardType.AMEX);
    }

    public static PaymentCard newVisa() throws PaymentCardException
    {
        return newCard(CardType.Visa);
    }

    public static PaymentCard newMastercard() throws PaymentCardException
    {
        return newCard(CardType.Mastercard);
    }

    private static PaymentCard newCard(CardType aCardType) throws PaymentCardException
    {
        return PaymentCard.getPaymentCard(getCardNumber(aCardType), getCvv(aCardType), getExpiryDate(), getNameOnCard(), aCardType);
    }

    private static DigitField getCvv(CardType aCardType)
    {
        return DigitField.getInstance(aCardType.getCvvLength());
    }

    private static DigitField getCardNumber(CardType aCardType)
    {
        return DigitField.getInstance(aCardType.getCardNumberLength());
    }

    private static Date getExpiryDate()
    {
        GregorianCalendar gc = new GregorianCalendar();
        gc.roll(Calendar.YEAR, 2);
        return gc.getTime();
    }

    private static String getNameOnCard()
    {
        return CardHelper.getNewCardholder().getNameOnCard();
    }
}
