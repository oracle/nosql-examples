/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.coherence;

import com.oracle.emeatech.trustedobjects.Domain;
import com.oracle.emeatech.trustedobjects.DomainHeader;
import com.oracle.emeatech.trustedobjects.PublicKeyExchange;
import com.oracle.emeatech.trustedobjects.Scheme;
import com.oracle.emeatech.trustedobjects.SecretKeyWrapper;
import com.oracle.emeatech.trustedobjects.Serializer;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import com.oracle.emeatech.trustedobjects.TrustedObjectService;
import java.security.PublicKey;
import java.util.HashMap;

/**
 *
 * @author ewan
 */
public class CoherenceTrustedObjectService extends TrustedObjectService
{

    private PublicKeyExchange publicKeyExchange;

    @Override
    public PublicKeyExchange getPublicKeyExchange()
    {
        if (this.publicKeyExchange == null)
        {
            this.publicKeyExchange = new CoherencePublicKeyExchange();
        }
        return this.publicKeyExchange;
    }

    @Override
    public Boolean domainExists(String domainName)
    {
        if (CoherenceDomain.getDomainServiceCache(domainName).containsKey(domainName))
        {
            if (CoherenceDomain.getDomainServiceCache(domainName).get(domainName) instanceof DomainHeader)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public DomainHeader getDomainHeader(String domainName) throws TrustedObjectException
    {
        if (domainExists(domainName))
        {
            return (DomainHeader) CoherenceDomain.getDomainServiceCache(domainName).get(domainName);
        } else
        {
            throw new TrustedObjectException("Domain " + domainName + " not found.");
        }
    }

    @Override
    public void createDomain(String domainName, String ownerName, PublicKey ownerPublicKey, Scheme scheme, SecretKeyWrapper wrappedSecretKey)
            throws TrustedObjectException
    {
        if (domainExists(domainName))
        {
            throw new TrustedObjectException("Domain " + domainName + " already exists and is owned by" + getDomainHeader(domainName).getDomainOwner());
        } else
        {
            DomainHeader domHdr = new CoherenceDomainHeader(domainName, ownerName, scheme, ownerPublicKey);
            HashMap hm = new HashMap();            
            hm.put(domainName, domHdr);
            hm.put(ownerName, wrappedSecretKey);
            CoherenceDomain.getDomainServiceCache(domainName).putAll(hm);
        }
    }

    @Override
    public void createDomain(String domainName, String ownerName, PublicKey ownerPublicKey, SecretKeyWrapper wrappedSecretKey) throws TrustedObjectException
    {
        createDomain(domainName, ownerName, ownerPublicKey, CoherenceScheme.getInstance(), wrappedSecretKey);
    }

    @Override
    public Serializer getSerializer()
    {
        return new CoherenceSerializer();
    }

    @Override
    public Domain getDomain(String domainName, String memberName) throws TrustedObjectException
    {
        return new CoherenceDomain(getDomainHeader(domainName), memberName);
    }
    
    @Override
    public boolean isMember(String domainName, String memberName)
    {
        return CoherenceDomain.getDomainServiceCache(domainName).containsKey(memberName);
    }

    @Override
    public Scheme getDefaultScheme()
    {
        return new CoherenceScheme();
    }
}
