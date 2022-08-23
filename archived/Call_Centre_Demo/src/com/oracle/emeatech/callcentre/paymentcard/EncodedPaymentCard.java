package com.oracle.emeatech.callcentre.paymentcard;

import com.oracle.emeatech.trustedobjects.Domain;
import com.oracle.emeatech.trustedobjects.EncodeableObject;
import com.oracle.emeatech.trustedobjects.EncodedObject;
import com.oracle.emeatech.trustedobjects.SealedWrapper;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.io.Serializable;
import java.util.Date;

/**
 * The encoded form of a PaymentCard object.
 *
 * @author ewan
 */
public class EncodedPaymentCard implements EncodedObject, Serializable
{

    public static final int CARD_NUMBER = 0;
    public static final int CVV = 1;
    public static final int EXPIRY_DATE = 2;
    public static final int NAME_ON_CARD = 3;
    public static final int CARD_TYPE = 4;

    private SealedWrapper cardNumber;
    private SealedWrapper cvv;
    private Date expiryDate;
    private String nameOnCard;
    private CardType cardType;

    public EncodedPaymentCard()
    {
    }

    public EncodedPaymentCard(SealedWrapper cardNumber, SealedWrapper cvv, Date expiryDate, String nameOnCard, CardType cardType)
    {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
        this.nameOnCard = nameOnCard;
        this.cardType = cardType;
    }

    /**
     * @return the cardNumber
     */
    public SealedWrapper getCardNumber()
    {
        return cardNumber;
    }

    /**
     * @return the cvv
     */
    public SealedWrapper getCvv()
    {
        return cvv;
    }

    /**
     * @return the expiryDate
     */
    public Date getExpiryDate()
    {
        return expiryDate;
    }

    /**
     * @return the nameOnCard
     */
    public String getNameOnCard()
    {
        return nameOnCard;
    }

    /**
     * @return the cardType
     */
    public CardType getCardType()
    {
        return cardType;
    }

    /**
     * Returns the decoded PaymentCard
     *
     * @param domainClient the DomainClient to use
     * @return the decoded form of the PaymentCard
     * @throws TrustedObjectException
     */
    @Override
    public EncodeableObject decode(Domain domain) throws TrustedObjectException
    {
        DigitField decodedCardNumber = (DigitField) domain.unsealObject(this.getCardNumber());
        DigitField decodedCvv = (DigitField) domain.unsealObject(this.getCvv());
        return new PaymentCard(decodedCardNumber, decodedCvv, this.getExpiryDate(), this.getNameOnCard(), this.getCardType());
    }
}
