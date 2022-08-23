/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects;

import com.oracle.emeatech.trustedobjects.keys.KeyStoreDriver;
import java.security.PublicKey;
import java.util.Properties;

/**
 *
 * @author ewan
 */
public abstract class TrustedObjectService
{

    private static TrustedObjectService instance;
    /**
     * Name of the system property used to specify the implementation of
     * TrustedObjectService that is to be loaded
     */
    public static final String SERVICE_CLASS_PROPERTY = "trustedobjects.service.class";

    /**
     * @return the instance
     */
    public static TrustedObjectService getInstance() throws TrustedObjectException
    {
        if (instance == null)
        {
            Properties sysProps = System.getProperties();
            if (sysProps.containsKey(SERVICE_CLASS_PROPERTY))
            {
                try
                {
                    String className = sysProps.getProperty(SERVICE_CLASS_PROPERTY);
                    instance = TrustedObjectService.class.cast(Class.forName(className).newInstance());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
                {
                    throw new TrustedObjectException("Could not load " + SERVICE_CLASS_PROPERTY, e);
                }
            } else
            {
                throw new TrustedObjectException("System property " + SERVICE_CLASS_PROPERTY
                        + " was not specified.");
            }
        }
        return instance;
    }

    public abstract PublicKeyExchange getPublicKeyExchange();

    public abstract Boolean domainExists(String domainName) throws TrustedObjectException;

    public abstract DomainHeader getDomainHeader(String domainName) throws TrustedObjectException;

    public abstract Domain getDomain(String domainName, String memberName) throws TrustedObjectException;

    public abstract void createDomain(String domainName, String ownerName, PublicKey ownerPublicKey, SecretKeyWrapper wrappedSecretKey)
            throws TrustedObjectException;

    public abstract void createDomain(String domainName, String ownerName, PublicKey ownerPublicKey, Scheme scheme, SecretKeyWrapper wrappedSecretKey)
            throws TrustedObjectException;

    public abstract Scheme getDefaultScheme();

    public byte[] getNewEncodedSecretKey()
    {
        return this.getNewEncodedSecretKey(this.getDefaultScheme());
    }

    public byte[] getNewEncodedSecretKey(Scheme s)
    {
        return KeyStoreDriver.makeNewSecretKey(s.getSecretKeySize(), s.getSecretKeyType()).getEncoded();
    }

    public abstract Serializer getSerializer();

    public abstract boolean isMember(String domainName, String memberName) throws TrustedObjectException;
}
