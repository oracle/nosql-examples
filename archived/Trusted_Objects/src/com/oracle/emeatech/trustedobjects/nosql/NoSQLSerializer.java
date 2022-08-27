/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.nosql;

import com.oracle.emeatech.trustedobjects.Serializer;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import oracle.kv.Value;
import oracle.kv.avro.RawAvroBinding;
import oracle.kv.avro.RawRecord;

/**
 *
 * @author ewan
 */
class NoSQLSerializer implements Serializer
{

    private static final String SIGNED_WRAPPER = "com.oracle.emeatech.trustedobjects.SignedWrapper",
            SEALED_WRAPPER = "com.oracle.emeatech.trustedobjects.SealedWrapper",
            PUBLIC_KEY_RECORD = "com.oracle.emeatech.trustedobjects.PublicKeyRecord",
            BULLETIN = "com.oracle.emeatech.callcentre.bulletinboard.Bulletin",
            ENCODED_PAYMENT_CARD = "com.oracle.emeatech.callcentre.paymentcard.EncodedPaymentCard",
            DIGIT_FIELD = "com.oracle.emeatech.callcentre.paymentcard.DigitField",
            NOSQL_DOMAIN_HEADER = "com.oracle.emeatech.trustedobjects.nosql.NoSQLDomainHeader",
            ENCODED_BULLETIN_WRAPPER = "com.oracle.emeatech.callcentre.bulletinboard.EncodedBulletinWrapper",
            SECRET_KEY_WRAPPER = "com.oracle.emeatech.trustedobjects.SecretKeyWrapper";

    @Override
    public Object deserialize(byte[] bytes) throws TrustedObjectException
    {
        Object result = null;
//        try
//        {
//            //lets try and work out what the bytes represent
//            Value bytesAsValue = Value.fromByteArray(bytes);
//            RawAvroBinding rawBinding = NoSQLHelper.getInstance().getCatalog().getRawBinding();
//            RawRecord rawRec = rawBinding.toObject(bytesAsValue);
//            String schemaName = rawRec.getSchema().getFullName();
//            System.out.println("Object schema: " + schemaName);
//            switch (schemaName)
//            {
//                case SIGNED_WRAPPER:
//                    break;
//                case SEALED_WRAPPER:
//                    break;
//                case PUBLIC_KEY_RECORD:
//                    break;
//                case BULLETIN:
//                    break;
//                case ENCODED_PAYMENT_CARD:
//                    break;
//                case DIGIT_FIELD:
//                    break;
//                case NOSQL_DOMAIN_HEADER:
//                    result = NoSQLDomainHeaderTranscriber.fromValue(bytesAsValue);
//                    break;
//                case ENCODED_BULLETIN_WRAPPER:
//                    break;
//                case SECRET_KEY_WRAPPER:
//                    break;
//            }
//        } catch (TrustedObjectException | IllegalArgumentException x)
//        {
//            //if we're here, none of the above worked, so let's try this...
//            //(it may even work)
            result = this.deserializeStandard(bytes);
//        }
        return result;
    }

    @Override
    public byte[] serialize(Object object) throws TrustedObjectException
    {
//        if (object instanceof NoSQLDomainHeader)
//        {
//            return NoSQLDomainHeaderTranscriber.toValue(object).toByteArray();
//        } else
//        {
            return this.serializeStandard(object);
//        }
    }

    /**
     * If there is no specific serialization code attempt to serialize using
     * standard Java serialization
     *
     * @param object object
     * @return object as byte array
     */
    private byte[] serializeStandard(Object object) throws TrustedObjectException
    {
        try
        {
            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(boas);
            oos.writeObject(object);
            return boas.toByteArray();
        } catch (IOException iox)
        {
            throw new TrustedObjectException(iox);
        }
    }

    private Object deserializeStandard(byte[] bytes) throws TrustedObjectException
    {
        try
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException x)
        {
            throw new TrustedObjectException(x);
        }

    }
}
