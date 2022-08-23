package com.oracle.emeatech.trustedobjects.nosql;

import com.oracle.emeatech.trustedobjects.DomainHeader;
import com.oracle.emeatech.trustedobjects.Scheme;
import java.security.PublicKey;

/**
 *
 * @author ewan
 */
class NoSQLDomainHeader implements DomainHeader
{
    
    private String domainName;
    private String domainOwner;
    private Scheme scheme;
    private PublicKey ownerPubKey;
    
    public NoSQLDomainHeader(String domainName, String domainOwner, Scheme scheme, PublicKey ownerPubKey)
    {
        this.domainName = domainName;
        this.domainOwner = domainOwner;
        this.scheme = scheme;
        this.ownerPubKey = ownerPubKey;
    }

    @Override
    public String getDomainName()
    {
        return this.domainName;
    }

    @Override
    public String getDomainOwner()
    {
        return this.domainOwner;
    }

    @Override
    public Scheme getScheme()
    {
        return this.scheme;
    }

    @Override
    public PublicKey getOwnerPublicKey()
    {
        return this.ownerPubKey;
    }

}
