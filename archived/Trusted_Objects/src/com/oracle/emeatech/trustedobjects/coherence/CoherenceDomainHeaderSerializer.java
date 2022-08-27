package com.oracle.emeatech.trustedobjects.coherence;

import com.oracle.emeatech.trustedobjects.Scheme;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 *
 * @author ewan
 */
public class CoherenceDomainHeaderSerializer implements PofSerializer
{

    private static final int DOMAIN_NAME = 0, DOMAIN_OWNER = 1, SCHEME = 2, OWNER_PUBLIC_KEY = 3;

    @Override
    public void serialize(PofWriter writer, Object o) throws IOException
    {
        CoherenceDomainHeader tdhi = (CoherenceDomainHeader) o;
        writer.writeString(DOMAIN_NAME, tdhi.getDomainName());
        writer.writeString(DOMAIN_OWNER, tdhi.getDomainOwner());

        //code to handle the hardcoded scheme (and avoid serializing hard - coded values
        if (tdhi.getScheme() instanceof CoherenceScheme)
        {
            writer.writeObject(SCHEME, null);
        } else
        {
            writer.writeObject(SCHEME, tdhi.getScheme());
        }

        writer.writeByteArray(OWNER_PUBLIC_KEY, tdhi.getOwnerPublicKey().getEncoded());
        writer.writeRemainder(null);
    }

    @Override
    public Object deserialize(PofReader reader) throws IOException
    {
        String domainName = reader.readString(DOMAIN_NAME);
        String domainOwner = reader.readString(DOMAIN_OWNER);
        Scheme scheme = (Scheme) reader.readObject(SCHEME);
        byte[] pubKeyBytes = reader.readByteArray(OWNER_PUBLIC_KEY);
        reader.readRemainder();

        if (scheme == null)
        {
            scheme = new CoherenceScheme();
        }

        PublicKey ownerPubKey;

        try
        {
            ownerPubKey = decodePublicKey(pubKeyBytes, scheme.getAsymmetricKeyType());
        } catch (Exception x)
        {
            throw new IOException("An error occurred decoding a PublicKey.", x);
        }

        return new CoherenceDomainHeader(domainName, domainOwner, scheme, ownerPubKey);
    }

    private static PublicKey decodePublicKey(byte[] pubKeyBytes, String keyType)
            throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        KeyFactory kf = KeyFactory.getInstance(keyType);
        return kf.generatePublic(new X509EncodedKeySpec(pubKeyBytes));
    }
}
