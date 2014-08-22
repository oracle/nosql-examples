package com.oracle.emeatech.callcentre.paymentcard;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

/**
 * A fixed length immutable digit field.  Can be used to represent 
 * @author ewan
 */
public class DigitField implements Serializable
{
    private static Random dice;

    private char[] digits;

    public DigitField(char[] digits)
    {
        this.digits = digits;
    }

    private static Random getDice()
    {
        if (dice == null)
        {
            dice = new Random();
        }
        return dice;
    }

    /**
     * Returns a new DigitField created from the contents of an array of digit
     * chars.
     * @param digits the contents as an array (char[]) of digits.
     * @return the new DigitField
     * @throws NumberFormatException if digits contains non - digit characters.
     */
    public static DigitField getInstance(char[] digits) throws NumberFormatException
    {
        for (int i = 0; i < digits.length; i++)
        {
            if (!Character.isDigit(digits[i]))
                throw new NumberFormatException("Non digit character not permitted in DigitField");
        }
        return new DigitField(digits);
    }

    /**
     * Returns a new randomly populated DigitField of the specified length.
     * @param length length of the DigitField required.
     * @return the new DigitField.
     */
    public static DigitField getInstance(int length)
    {
        char[] newDigits = new char[length];
        for (int i = 0; i < length; i++)
        {
            newDigits[i] = Character.forDigit(getDice().nextInt(9), 10);
        }
        return getInstance(newDigits);
    }

    public DigitField() {}

    /**
     * 
     * @return the length of the field
     */
    public int getLength()
    {
        return digits.length;
    }

    /**
     * @return the digits
     */
    public char[] getDigits()
    {
        return Arrays.copyOf(this.digits, this.getLength());
    }
}
