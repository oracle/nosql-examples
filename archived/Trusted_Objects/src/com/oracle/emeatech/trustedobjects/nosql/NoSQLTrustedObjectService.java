/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.nosql;

import com.oracle.emeatech.trustedobjects.Domain;
import com.oracle.emeatech.trustedobjects.DomainHeader;
import com.oracle.emeatech.trustedobjects.PublicKeyExchange;
import com.oracle.emeatech.trustedobjects.Scheme;
import com.oracle.emeatech.trustedobjects.SecretKeyWrapper;
import com.oracle.emeatech.trustedobjects.Serializer;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import com.oracle.emeatech.trustedobjects.TrustedObjectService;
import java.security.PublicKey;

/**
 *
 * @author ewan
 */
public class NoSQLTrustedObjectService extends TrustedObjectService
{

    private PublicKeyExchange publicKeyExchange;

    @Override
    public PublicKeyExchange getPublicKeyExchange()
    {
        if (this.publicKeyExchange == null)
        {
            this.publicKeyExchange = new NoSQLPublicKeyExchange();
        }
        return this.publicKeyExchange;
    }

    @Override
    public Boolean domainExists(String domainName) throws TrustedObjectException
    {
        return NoSQLDomain.domainExists(domainName);
    }

    @Override
    public DomainHeader getDomainHeader(String domainName) throws TrustedObjectException
    {
        if (domainExists(domainName))
        {
            return NoSQLDomain.getDomainHeader(domainName);
        } else
        {
            throw new TrustedObjectException("Domain " + domainName + " not found.");
        }
    }

    @Override
    public Domain getDomain(String domainName, String memberName) throws TrustedObjectException
    {
        return new NoSQLDomain(getDomainHeader(domainName), memberName);
    }

    @Override
    public void createDomain(String domainName, String ownerName, PublicKey ownerPublicKey, SecretKeyWrapper wrappedSecretKey) throws TrustedObjectException
    {
        createDomain(domainName, ownerName, ownerPublicKey, NoSQLScheme.getInstance(), wrappedSecretKey);
    }

    @Override
    public void createDomain(String domainName, String ownerName, PublicKey ownerPublicKey, Scheme scheme, SecretKeyWrapper wrappedSecretKey) throws TrustedObjectException
    {
        if (domainExists(domainName))
        {
            throw new TrustedObjectException("Domain " + domainName + " already exists and is owned by" + getDomainHeader(domainName).getDomainOwner());
        } else
        {
            DomainHeader domHdr = new NoSQLDomainHeader(domainName, ownerName, scheme, ownerPublicKey);
            NoSQLDomain domain = new NoSQLDomain(domHdr, ownerName);
            domain.writeDomainHeader();
            domain.addKeyForMember(ownerName, wrappedSecretKey);
        }
    }

    @Override
    public Scheme getDefaultScheme()
    {
        return new NoSQLScheme();
    }

    @Override
    public Serializer getSerializer()
    {
        return new NoSQLSerializer();
    }

    @Override
    public boolean isMember(String domainName, String memberName) throws TrustedObjectException
    {
        return NoSQLDomain.isMember(domainName, memberName);
    }
}
