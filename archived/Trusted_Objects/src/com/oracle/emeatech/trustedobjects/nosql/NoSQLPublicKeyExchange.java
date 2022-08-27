/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.nosql;

import com.oracle.emeatech.trustedobjects.PublicKeyExchange;
import com.oracle.emeatech.trustedobjects.PublicKeyRecord;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.ValueVersion;
import org.apache.avro.generic.GenericRecord;

/**
 *
 * @author ewan
 */
public class NoSQLPublicKeyExchange implements PublicKeyExchange
{

    private static final String PUBLIC_KEY_RECORD_ROOT = "public_key";
    private static final String ENCODED_KEY = "encodedKey", ALGORITHM = "algorithm";
    private static final int OWNER_ID_INDEX = 1;

    @Override
    public Set<String> listPublicKeys() throws TrustedObjectException
    {
        HashSet<String> keySet = new HashSet();
        Iterator iter = getStoreHandle().storeKeysIterator(Direction.UNORDERED, 0, getRootKey(), null, Depth.CHILDREN_ONLY);

        while (iter.hasNext())
        {
            Key key = (Key) iter.next();
            keySet.add(key.getMajorPath().get(OWNER_ID_INDEX));
        }
        return keySet;
    }

    @Override
    public void publishPublicKey(String aPublicKeyOwnerId, PublicKey aPublicKey) throws TrustedObjectException
    {
        PublicKeyRecord pkr = new PublicKeyRecord(aPublicKey);
        GenericRecord rec = getHelper().getRecordForObject(pkr);

        /*
         * Note: you need to wrap the byte[] into a ByteBuffer to serialize
         * it for NoSQL.  See the UserImage example.
         */
        rec.put(ENCODED_KEY, ByteBuffer.wrap(pkr.getEncodedKey()));
        rec.put(ALGORITHM, pkr.getAlgorithm());
        getStoreHandle().put(getOwnerKey(aPublicKeyOwnerId), getHelper().getBindingForObject(pkr).toValue(rec));
    }

    private static Key getOwnerKey(String aPublicKeyOwnerId)
    {
        return Key.createKey(getOwnerKeyList(aPublicKeyOwnerId));
    }

    private static List<String> getOwnerKeyList(String aPublicKeyOwnerId)
    {
        List<String> keyComps = getRootKeyList();
        keyComps.add(aPublicKeyOwnerId);
        return keyComps;
    }

    private static Key getRootKey()
    {
        return Key.createKey(getRootKeyList());
    }

    private static List<String> getRootKeyList()
    {
        ArrayList<String> keyComps = new ArrayList<>();
        keyComps.add(PUBLIC_KEY_RECORD_ROOT);
        return keyComps;
    }

    @Override
    public PublicKey retrievePublicKey(String keyOwnerId) throws TrustedObjectException
    {
        ValueVersion valVer = getStoreHandle().get(getOwnerKey(keyOwnerId));
        if (valVer != null)
        {
            GenericRecord rec = getHelper().getBindingForClass(PublicKeyRecord.class).toObject(valVer.getValue());
            ByteBuffer encodedKeyBuf = (ByteBuffer) rec.get(ENCODED_KEY);
            byte[] encodedKey = encodedKeyBuf.array();
            String algorithm = rec.get(ALGORITHM).toString();
            return decodePublicKey(encodedKey, algorithm);
        } else
        {
            return null;
        }
    }

    private NoSQLHelper getHelper() throws TrustedObjectException
    {
        return NoSQLHelper.getInstance();
    }
    
    private KVStore getStoreHandle() throws TrustedObjectException
    {
        return getHelper().getStoreHandle();
    }
    
    static PublicKey decodePublicKey(byte[] encodedKey, String algorithm) throws TrustedObjectException
    {
            try
            {
                KeyFactory kf = KeyFactory.getInstance(algorithm);
                return kf.generatePublic(new X509EncodedKeySpec(encodedKey));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException x)
            {
                throw new TrustedObjectException(x);
            }        
    }
}
