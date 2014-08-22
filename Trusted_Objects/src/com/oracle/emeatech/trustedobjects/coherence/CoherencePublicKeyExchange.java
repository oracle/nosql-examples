package com.oracle.emeatech.trustedobjects.coherence;

import com.oracle.emeatech.trustedobjects.PublicKeyExchange;
import com.oracle.emeatech.trustedobjects.PublicKeyRecord;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic public key exchange for call centre demo.
 * Essentially a DAO
 * @author ewan
 */
class CoherencePublicKeyExchange implements PublicKeyExchange
{

    /**
     * Name of the cache used to hold public keys
     */
    public static String PUBLIC_KEY_EXCHANGE_CACHE_NAME = "public-key-exchange";

    private static NamedCache getCache()
    {
        return CacheFactory.getCache(PUBLIC_KEY_EXCHANGE_CACHE_NAME);
    }

    @Override
    public Set<String> listPublicKeys()
    {
        HashSet<String> keys = new HashSet<>();
        for (Object o : getCache().keySet())
        {
            keys.add((String) o);
        }
        return keys;
    }

    @Override
    public void publishPublicKey(String aKeyOwnerId, PublicKey aPublicKey)
    {
        HashMap tmp = new HashMap();
        tmp.put(aKeyOwnerId, new PublicKeyRecord(aPublicKey));
        getCache().putAll(tmp);
    }

    @Override
    public PublicKey retrievePublicKey(String keyOwnerId) throws TrustedObjectException
    {
        PublicKeyRecord pkr = (PublicKeyRecord) getCache().get(keyOwnerId);
        if (pkr != null)
        {
            try
            {
            KeyFactory kf = KeyFactory.getInstance(pkr.getAlgorithm());
            return kf.generatePublic(new X509EncodedKeySpec(pkr.getEncodedKey()));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException x)
            {
                throw new TrustedObjectException(x);
            }
        } else
        {
            return null;
        }
    }
}
