/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.nosql;

import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import oracle.kv.Value;
import org.apache.avro.generic.GenericRecord;

/**
 *
 * @author ewan
 */
public class NoSQLDomainHeaderTranscriber
{

    private static final String DOMAIN_NAME = "domainName", DOMAIN_OWNER = "domainOwner",
            SCHEME = "scheme", OWNER_PUBLIC_KEY_BYTES = "ownerPubKeyBytes",
            OWNER_PUBLIC_KEY_ALGORITHM = "ownerPubKeyAlgorithm";

    private static NoSQLHelper getHelper() throws TrustedObjectException
    {
        return NoSQLHelper.getInstance();
    }

    static NoSQLDomainHeader fromValue(Value value) throws TrustedObjectException
    {
        GenericRecord rec = getHelper().getBindingForClass(NoSQLDomainHeader.class).toObject(value);
        ByteBuffer opkBuf = (ByteBuffer) rec.get(OWNER_PUBLIC_KEY_BYTES);
        String opkAlgo = rec.get(OWNER_PUBLIC_KEY_ALGORITHM).toString();
        PublicKey opk = NoSQLPublicKeyExchange.decodePublicKey(opkBuf.array(), opkAlgo);
        return new NoSQLDomainHeader(
                rec.get(DOMAIN_NAME).toString(),
                rec.get(DOMAIN_OWNER).toString(),
                NoSQLScheme.getInstance(),
                opk);
    }

    static Value toValue(Object object) throws TrustedObjectException
    {
        NoSQLDomainHeader domHdr = (NoSQLDomainHeader) object;
        GenericRecord rec = getHelper().getRecordForObject(domHdr);
        rec.put(DOMAIN_NAME, domHdr.getDomainName());
        rec.put(DOMAIN_OWNER, domHdr.getDomainOwner());

        //note - the Scheme isn't being sent to the database since the values are
        //currently hardcoded anyway

        rec.put(OWNER_PUBLIC_KEY_BYTES, ByteBuffer.wrap(domHdr.getOwnerPublicKey().getEncoded()));
        rec.put(OWNER_PUBLIC_KEY_ALGORITHM, domHdr.getOwnerPublicKey().getAlgorithm());
        return getHelper().getBindingForObject(domHdr).toValue(rec);
    }
}
