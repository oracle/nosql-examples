/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.paymentcard;

/**
 *
 * @author ewan
 */
public enum CardType
{
    Mastercard(16,3), Visa(16,3), AMEX(15,4);

    private int cardNumberLength;
    private int cvvLength;

    private CardType(int aCardNumberLength, int aCvvLength)
    {
        this.cardNumberLength = aCardNumberLength;
        this.cvvLength = aCvvLength;
    }

    /**
     * @return the cardNumberLength
     */
    public int getCardNumberLength()
    {
        return cardNumberLength;
    }

    /**
     * @return the cvvLength
     */
    public int getCvvLength()
    {
        return cvvLength;
    }
    
}
