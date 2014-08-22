/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Observer;

/**
 * A Domain is used to provide increased security and integrity at the object
 * level within an Oracle Coherence based system.
 *
 * This is achieved through use of standard Java cryptography APIs to perform
 * encryption/decryption and signature/verification operations.
 *
 * The algorithms, key types and key sizes to be used for a Domain are defined
 * in the Scheme for that Domain.
 *
 * A shared symmetric key is used to encrypt/decrypt sensitive data protected by
 * the domain. The shared key is distributed using asymmetric cryptography.
 *
 * A Domain is a logical construct and imposes no restriction on the structure
 * used to store the objects.
 *
 * @author ewan
 */
public interface Domain
{

    public SignedWrapper signObject(Object forSignature) throws TrustedObjectException;

    public Object getVerifiedObject(SignedWrapper forVerification, PublicKey sendersKey) throws TrustedObjectException;

    public boolean verifySignature(SignedWrapper forVerification, PublicKey sendersKey) throws TrustedObjectException;

    public SealedWrapper sealObject(Object forSealing) throws TrustedObjectException;

    public Object unsealObject(SealedWrapper aParcel) throws TrustedObjectException;

    public String getDomainName();

    public String getMemberName();

    public Scheme getScheme();

    public String getDomainOwner();

    public PublicKey getOwnerPublicKey();

    public List<String> getMemberNames() throws TrustedObjectException;

    public boolean isMember(String aMemberName) throws TrustedObjectException;

    public SecretKeyWrapper getWrappedKeyForMember(String aMemberName) throws TrustedObjectException;

    public void requestMembership(Observer anObserver, String aMemberName) throws TrustedObjectException;

    public void requestMembership(Observer anObserver) throws TrustedObjectException;

    public void addDomainMember(String aMemberName, PublicKey aMemberPublicKey) throws TrustedObjectException;

    public void removeDomainMember(String aMemberName) throws TrustedObjectException;

    public void addKeyForMember(String aMemberName, SecretKeyWrapper aWrappedKey) throws TrustedObjectException;

    public void deleteKeyForMember(String aMemberName) throws TrustedObjectException;

    public void setPrivateKey(PrivateKey privateKey);
}
