/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.paymentcard.utils;

import java.util.Random;

/**
 * Generates random card holders
 * @author ewan
 */
public class CardHelper
{
    private static Random dice;

    public static Random getDice()
    {
        if (dice == null)
        {
            dice = new Random();
        }
        return dice;
    }
    public static String[] maleNames =
    {
        "Harry", "Jack", "Oliver", "Charlie",
        "James", "George", "Thomas", "Ethan",
        "Jacob", "William", "Daniel", "Joshua",
        "Max", "Noah", "Alfie", "Samuel",
        "Dylan", "Oscar", "Lucas", "Aiden"
    };
    public static String[] femaleNames =
    {
        "Amelia", "Lily", "Emily", "Sophia",
        "Isabelle", "Sophie", "Olivia", "Jessica",
        "Chloe", "Mia", "Isla", "Isabella",
        "Ava", "Charlotte", "Grace", "Evie",
        "Poppy", "Lucy", "Ella", "Holly"
    };
    public static String[] lastNames =
    {
        "Smith", "Jones", "Taylor", "Williams",
        "Brown", "Davies", "Evans", "Wilson",
        "Thomas", "Roberts", "Johnson", "Lewis",
        "Walker", "Robinson", "Wood", "Thompson",
        "White", "Watson", "Jackson", "Wright",
        "Green", "Harris", "Cooper", "King",
        "Lee", "Martin", "Clarke", "James",
        "Morgan", "Hughes", "Edwards", "Hill",
        "Moore", "Clark", "Harrison", "Scott",
        "Young", "Morris", "Hall", "Ward"
    };

    private static String getRandomValue(String[] values)
    {
        return values[getDice().nextInt(values.length - 1)];
    }

    private static String getRandomValue(String[] values, String valueToExclude)
    {
        String newVal;
        do
        {
            newVal = getRandomValue(values);
        } while (newVal.equals(valueToExclude));
        return newVal;
    }

    private static CardholderSex getRandomCardholderSex()
    {
        if (getDice().nextBoolean())
        {
            return CardholderSex.FEMALE;
        } else
        {
            return CardholderSex.MALE;
        }
    }

    public enum CardholderSex
    {
        FEMALE, MALE;
    }

    /**
     * Returns a new, randomly generated CardholderInfo
     * @return the new CardholderInfo
     */
    public static CardholderInfo getNewCardholder()
    {
        String lastName = getRandomValue(lastNames);
        String mumsLastName = getRandomValue(lastNames, lastName);
        CardholderSex chSex = getRandomCardholderSex();
        String firstName;
        if (chSex == CardholderSex.FEMALE)
        {
            firstName = getRandomValue(femaleNames);
        }
        else
        {
            firstName = getRandomValue(maleNames);
        }

        return new CardholderInfo(firstName, lastName, mumsLastName, chSex);
    }
}
