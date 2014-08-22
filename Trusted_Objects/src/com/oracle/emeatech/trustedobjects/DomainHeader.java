package com.oracle.emeatech.trustedobjects;

import java.security.PublicKey;

/**
 *
 * @author ewan
 */
public interface DomainHeader
{
    public String getDomainName();

    public String getDomainOwner();

    public Scheme getScheme();

    public PublicKey getOwnerPublicKey();
}
