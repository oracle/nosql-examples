/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Set;

/**
 *
 * @author ewan
 */
public interface PublicKeyExchange
{

    /**
     * Lists the owner IDs for which public keys have been published.
     * @return a Set of the owner IDs for which public keys have been published.
     */
    public Set<String> listPublicKeys() throws TrustedObjectException;

    /**
     * Publishes a public key for a key owner.
     * @param aKeyOwnerId the ID of the key owner.
     * @param aPublicKey the public key to publish.
     */
    public void publishPublicKey(String aKeyOwnerId, PublicKey aPublicKey) throws TrustedObjectException;


    /**
     * Retrieves the public key for the specified owner, if it exists in the
     * cache.  Otherwise returns null.
     * @param keyOwnerId the ID of the key owner.
     * @return the public key for the owner if it is found in the cache, otherwise null.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public PublicKey retrievePublicKey(String keyOwnerId) throws TrustedObjectException;

}
