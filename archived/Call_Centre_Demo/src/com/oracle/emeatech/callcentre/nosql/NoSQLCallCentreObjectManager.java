/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.nosql;

import com.oracle.emeatech.callcentre.bulletinboard.EncodedBulletinWrapper;
import com.oracle.emeatech.callcentre.demo.CallCentreObjectManager;
import com.oracle.emeatech.callcentre.paymentcard.CardType;
import com.oracle.emeatech.callcentre.paymentcard.EncodedPaymentCard;
import com.oracle.emeatech.trustedobjects.EncodedObject;
import com.oracle.emeatech.trustedobjects.SealedWrapper;
import com.oracle.emeatech.trustedobjects.SignedWrapper;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import com.oracle.emeatech.trustedobjects.nosql.NoSQLHelper;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;
import oracle.kv.avro.GenericAvroBinding;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

/**
 *
 * @author ewan
 */
public class NoSQLCallCentreObjectManager implements CallCentreObjectManager
{

    private NoSQLHelper helper;
    private KVStore storeHandle;
    private static final String SEQ_ROOT = "sequences";
    private static final String CARD_ID_SEQ = "card_id_seq";
    private static final String MESSAGE_ID_SEQ = "message_id_seq";
    private static final String CARD_ROOT = "payment_cards";
    private static final String CARD_NUMBER = "cardNumber";
    private static final String CVV = "cvv";
    private static final String EXPIRY_DATE = "expiryDate";
    private static final String NAME_ON_CARD = "nameOnCard";
    private static final int DOMAIN_PATH_INDEX = 2, CARD_ID_PATH_INDEX = 1, PUBLIC_BULLETIN_ID_PATH_INDEX = 0,
            PRIVATE_BULLETIN_TIMESTAMP_INDEX = 1, PRIVATE_BULLETIN_SENDER_ID_INDEX = 0;
    private static final String PUBLIC_BULLETIN_ROOT = "public_bulletin_board", PRIVATE_BULLETIN_ROOT = "private_bulletin_board",
            PRIVATE_BULLETIN_INDEX_ROOT = "private_bulletin_index";
    private static final String PAYLOAD = "payload";
    private static final String SIGNATURE = "signature";
    private static final String SIGLEN = "siglen";
    private static final String SENDER_ID = "senderId";
    private static final String TIMESTAMP = "timestamp";
    private static final String SIGNED_SEALED_CONTENT = "signedSealedContent";

    @Override
    public void storeCard(Object id, EncodedObject card) throws TrustedObjectException
    {
        EncodedPaymentCard encCard = (EncodedPaymentCard) card;
        /**
         * the encrypted data is stored under the major key:
         * /cards/[card-type]/[card-id]/ the other fields - date, nameOnCard
         * will be stored as minor keys
         */
        ArrayList<String> majorKeyList = new ArrayList();
        majorKeyList.add(CARD_ROOT);
        majorKeyList.add(id.toString());
        majorKeyList.add(encCard.getCardType().toString());

        GenericRecord rec = getHelper().getRecordForObject(encCard);
        rec.put(CARD_NUMBER, ByteBuffer.wrap(encCard.getCardNumber().getPayload()));
        rec.put(CVV, ByteBuffer.wrap(encCard.getCvv().getPayload()));
        getStoreHandle().put(Key.createKey(majorKeyList), getHelper().getBindingForObject(encCard).toValue(rec));

        Key expiryDateKey = Key.createKey(majorKeyList, EXPIRY_DATE);

        Schema schema = getHelper().getSchemaFromCatalog("EncodedPaymentCard.expiryDate");
        rec = getHelper().getRecordForSchema(schema);
        rec.put(EXPIRY_DATE, encCard.getExpiryDate().getTime());
        GenericAvroBinding binding = getHelper().getBindingForSchema(schema);
        getStoreHandle().put(expiryDateKey, binding.toValue(rec));

        Key nameOnCardKey = Key.createKey(majorKeyList, NAME_ON_CARD);
        schema = getHelper().getSchemaFromCatalog("EncodedPaymentCard.nameOnCard");
        rec = getHelper().getRecordForSchema(schema);
        rec.put(NAME_ON_CARD, encCard.getNameOnCard());
        binding = getHelper().getBindingForSchema(schema);
        getStoreHandle().put(nameOnCardKey, binding.toValue(rec));
    }

    @Override
    public EncodedObject retrieveCard(Object id) throws TrustedObjectException
    {
        SealedWrapper cardNumber = null;
        SealedWrapper cvv = null;
        CardType cardType = null;
        Date expiryDate = null;
        String nameOnCard = null;;

        ArrayList<String> majorKeyList = new ArrayList();
        majorKeyList.add(CARD_ROOT);
        majorKeyList.add(id.toString());
        Iterator<KeyValueVersion> kvvi = getStoreHandle().storeIterator(Direction.UNORDERED, 1, Key.createKey(majorKeyList), null, Depth.PARENT_AND_DESCENDANTS);

        while (kvvi.hasNext())
        {
            ByteBuffer cardNumberBuf, cvvBuf;
            Schema schema = getHelper().getSchemaFromCatalog(EncodedPaymentCard.class);
            GenericRecord rec;
            GenericAvroBinding binding = getHelper().getBindingForSchema(schema);

            KeyValueVersion kvv = kvvi.next();
            List<String> major = kvv.getKey().getMajorPath();
            List<String> minor = kvv.getKey().getMinorPath();
            if (minor.isEmpty())
            {
                //then the encrypted stuff is here
                String domain = major.get(DOMAIN_PATH_INDEX);
                cardType = CardType.valueOf(domain);
                rec = binding.toObject(kvv.getValue());
                cardNumberBuf = (ByteBuffer) rec.get(CARD_NUMBER);
                cardNumber = new SealedWrapper(cardNumberBuf.array());
                cvvBuf = (ByteBuffer) rec.get(CVV);
                cvv = new SealedWrapper(cvvBuf.array());
            } else
            {
                String component = minor.get(0);
                if (component.equals(EXPIRY_DATE))
                {
                    schema = getHelper().getSchemaFromCatalog("EncodedPaymentCard.expiryDate");
                    binding = getHelper().getBindingForSchema(schema);
                    rec = binding.toObject(kvv.getValue());
                    expiryDate = new Date((Long) rec.get(EXPIRY_DATE));
                } else if (component.equals(NAME_ON_CARD))
                {
                    schema = getHelper().getSchemaFromCatalog("EncodedPaymentCard.nameOnCard");
                    binding = getHelper().getBindingForSchema(schema);
                    rec = binding.toObject(kvv.getValue());
                    nameOnCard = rec.get(NAME_ON_CARD).toString();
                }
            }
        }
        return new EncodedPaymentCard(cardNumber, cvv, expiryDate, nameOnCard, cardType);
    }

    @Override
    public void storePublicBulletin(Integer bulletinId, SignedWrapper bulletin) throws TrustedObjectException
    {
        GenericRecord rec = getHelper().getRecordForObject(bulletin);
        rec.put(PAYLOAD, ByteBuffer.wrap(bulletin.getPayloadAsBytes()));
        rec.put(SIGNATURE, ByteBuffer.wrap(bulletin.getSignature()));
        rec.put(SIGLEN, bulletin.getSiglen());
        rec.put(SENDER_ID, bulletin.getSignatoryId());
        getStoreHandle().put(Key.createKey(PUBLIC_BULLETIN_ROOT, bulletinId.toString()), getHelper().getBindingForObject(bulletin).toValue(rec));
    }

    @Override
    public SignedWrapper retrievePublicBulletin(Integer bulletinId) throws TrustedObjectException
    {
        GenericRecord result = getHelper().getBindingForClass(SignedWrapper.class).toObject(
                getStoreHandle().get(Key.createKey(PUBLIC_BULLETIN_ROOT, bulletinId.toString())).getValue());

        ByteBuffer payloadBuf = (ByteBuffer) result.get(PAYLOAD);
        ByteBuffer signatureBuf = (ByteBuffer) result.get(SIGNATURE);
        int sigLen = (Integer) result.get(SIGLEN);
        String senderId = result.get(SENDER_ID).toString();
        SignedWrapper sw = SignedWrapper.hydrate(payloadBuf.array(), signatureBuf.array(), senderId);
        if (sw.getSiglen() == sigLen)
        {
            return sw;
        } else
        {
            throw new TrustedObjectException("Expected signature length: " + sigLen + ".  Found:  " + sw.getSiglen());
        }
    }

    @Override
    public Set<Integer> listPublicBulletinIds() throws TrustedObjectException
    {
        Set<Key> keySet = getStoreHandle().multiGetKeys(Key.createKey(PUBLIC_BULLETIN_ROOT), null, Depth.CHILDREN_ONLY);
        HashSet<Integer> idSet = new HashSet();
        for (Key k : keySet)
        {
            idSet.add(new Integer(k.getMinorPath().get(PUBLIC_BULLETIN_ID_PATH_INDEX)));
        }
        return idSet;
    }

    @Override
    public void storePrivateBulletin(String bulletinId, EncodedObject bulletin) throws TrustedObjectException
    {
        EncodedBulletinWrapper ebw = (EncodedBulletinWrapper) bulletin;
        GenericRecord rec = getHelper().getRecordForObject(ebw);
        rec.put(TIMESTAMP, ebw.getTimestamp().getTime());
        rec.put(SENDER_ID, ebw.getSenderId());
        rec.put(SIGNED_SEALED_CONTENT, ByteBuffer.wrap(ebw.getSealedSignedContent().getPayload()));

        Key bulletinKey = Key.createKey(PRIVATE_BULLETIN_ROOT, bulletinId.toString());
        getStoreHandle().put(bulletinKey, getHelper().getBindingForObject(ebw).toValue(rec));

        ArrayList<String> minorList = new ArrayList();
        minorList.add(ebw.getSenderId());
        minorList.add(ebw.getTimestamp().toString());
        Key indexKey = Key.createKey(PRIVATE_BULLETIN_INDEX_ROOT, minorList);
        getStoreHandle().put(indexKey, Value.createValue(bulletinId.getBytes()));
    }

    @Override
    public EncodedObject retrievePrivateBulletin(String bulletinId) throws TrustedObjectException
    {
        Key bulletinKey = Key.createKey(PRIVATE_BULLETIN_ROOT, bulletinId.toString());
        GenericRecord result = getHelper().getBindingForClass(EncodedBulletinWrapper.class).toObject(
                getStoreHandle().get(bulletinKey).getValue());
        long dateAsLong = (Long) result.get(TIMESTAMP);
        String senderId = result.get(SENDER_ID).toString();
        ByteBuffer payloadBuf = (ByteBuffer) result.get(SIGNED_SEALED_CONTENT);
        return new EncodedBulletinWrapper(new Date(dateAsLong), senderId, new SealedWrapper(payloadBuf.array()));
    }

    @Override
    public Integer getNextCardSequenceNumber() throws TrustedObjectException
    {
        return this.getNextSequenceNumber(CARD_ID_SEQ);
    }

    @Override
    public Integer getNextBulletinSequenceNumber() throws TrustedObjectException
    {
        return this.getNextSequenceNumber(MESSAGE_ID_SEQ);
    }

    private Integer getNextSequenceNumber(String sequenceName) throws TrustedObjectException
    {
        Integer seq = null;
        Version placed = null;
        ArrayList<String> keyList = new ArrayList();
        keyList.add(SEQ_ROOT);
        keyList.add(sequenceName);
        Key SequenceKey = Key.createKey(keyList);
        while (placed == null)
        {
            ValueVersion vv = getStoreHandle().get(SequenceKey);
            if (vv == null)
            {
                seq = new Integer(0);
                placed = getStoreHandle().putIfAbsent(SequenceKey,
                        Value.createValue(seq.toString().getBytes()));
            } else
            {
                seq = new Integer(new String(vv.getValue().getValue()));
                seq++;
                placed = getStoreHandle().putIfVersion(SequenceKey,
                        Value.createValue(seq.toString().getBytes()), vv.getVersion());
            }
        }
        return seq;
    }

    @Override
    public Map<Object, List> listUnencryptedCardData() throws TrustedObjectException
    {
        Iterator<KeyValueVersion> kvvi = getStoreHandle().storeIterator(Direction.UNORDERED, 1,
                Key.createKey(CARD_ROOT), null, Depth.PARENT_AND_DESCENDANTS);
        HashMap<Object, List> cards = new HashMap();
        Integer id = -1;
        List<String> cardFields = null;
        while (kvvi.hasNext())
        {
            KeyValueVersion kvv = kvvi.next();
            Integer i = new Integer(kvv.getKey().getMajorPath().get(CARD_ID_PATH_INDEX));
            if (i != id)
            {
                //new card
                if (cardFields != null)
                {
                    cards.put(id, cardFields);
                }
                id = i;
                cardFields = new ArrayList<String>();
                cardFields.add(kvv.getKey().getMajorPath().get(DOMAIN_PATH_INDEX));
            }
            Schema schema;
            GenericRecord rec;
            GenericAvroBinding binding;
            if (!kvv.getKey().getMinorPath().isEmpty())
            {
                String component = kvv.getKey().getMinorPath().get(0);
                if (component.equals(EXPIRY_DATE))
                {
                    schema = getHelper().getSchemaFromCatalog("EncodedPaymentCard.expiryDate");
                    binding = getHelper().getBindingForSchema(schema);
                    rec = binding.toObject(kvv.getValue());
                    cardFields.add("Expiry Date: " + new Date((Long) rec.get(EXPIRY_DATE)));
                } else if (component.equals(NAME_ON_CARD))
                {
                    schema = getHelper().getSchemaFromCatalog("EncodedPaymentCard.nameOnCard");
                    binding = getHelper().getBindingForSchema(schema);
                    rec = binding.toObject(kvv.getValue());
                    cardFields.add("Cardholder: " + rec.get(NAME_ON_CARD).toString());
                }
            }
        }
        return cards;
    }

    @Override
    public Map<Object, List> listUnencryptedPrivateMessageData() throws TrustedObjectException
    {
        HashMap<Object, List> result = new HashMap();
        Iterator<KeyValueVersion> kvvi =
                getStoreHandle().multiGetIterator(Direction.FORWARD, 0, Key.createKey(PRIVATE_BULLETIN_INDEX_ROOT), null, Depth.DESCENDANTS_ONLY);
        while (kvvi.hasNext())
        {
            KeyValueVersion kvv = kvvi.next();
            result.put(new String(kvv.getValue().getValue()), kvv.getKey().getMinorPath());
        }
        return result;
    }

    /**
     * @return the helper
     */
    private NoSQLHelper getHelper() throws TrustedObjectException
    {
        if (helper == null)
        {
            helper = NoSQLHelper.getInstance();
        }
        return helper;
    }

    /**
     * @return the storeHandle
     */
    private KVStore getStoreHandle() throws TrustedObjectException
    {
        if (storeHandle == null)
        {
            storeHandle = getHelper().getStoreHandle();
        }
        return storeHandle;
    }
}
