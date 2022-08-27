/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.paymentcard.utils;

import com.oracle.emeatech.callcentre.paymentcard.utils.CardHelper.CardholderSex;

/**
 * Represents basic information for a card holder.
 * @author ewan
 */
public class CardholderInfo
{

    private String firstName;
    private String lastName;
    private String mumsLastName;
    private CardholderSex sex;

    public CardholderInfo(String aFirstName, String aLastName, String aMumsLastName, CardholderSex aSex)
    {
        this.firstName = aFirstName;
        this.lastName = aLastName;
        this.mumsLastName = aMumsLastName;
        this.sex = aSex;
    }

    public CardholderSex getCardholderSex()
    {
        return this.sex;
    }

    public String getFirstName()
    {
        return this.firstName;
    }

    public String getInitial()
    {
        return this.firstName.substring(0, 1);
    }

    public String getLastName()
    {
        return this.lastName;
    }

    public String getNameOnCard()
    {
        return getFirstName() + " " + getLastName();
    }

    public String getMumsLastName()
    {
        return this.mumsLastName;
    }
}
