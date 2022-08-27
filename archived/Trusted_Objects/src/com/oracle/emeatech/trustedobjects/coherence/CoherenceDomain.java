package com.oracle.emeatech.trustedobjects.coherence;

import com.oracle.emeatech.trustedobjects.Domain;
import com.oracle.emeatech.trustedobjects.DomainHeader;
import com.oracle.emeatech.trustedobjects.Scheme;
import com.oracle.emeatech.trustedobjects.SealedWrapper;
import com.oracle.emeatech.trustedobjects.SecretKeyWrapper;
import com.oracle.emeatech.trustedobjects.SignedWrapper;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.AbstractMapListener;
import com.tangosol.util.MapEvent;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observer;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author ewan
 */
public class CoherenceDomain implements Domain
{

    private DomainHeader domainHeader;
    private String memberName;
    private SecretKey secretKey;
    private PrivateKey privateKey;
    private NamedCache domainServiceCache;
    public static final String SERVICE_CACHE_SUFFIX = "-service-cache";

    CoherenceDomain(DomainHeader domainHeader, String memberName)
    {
        this.domainHeader = domainHeader;
        this.memberName = memberName;
    }

    @Override
    public String getDomainName()
    {
        return this.getDomainHeader().getDomainName();
    }

    @Override
    public String getMemberName()
    {
        return this.memberName;
    }

    private PrivateKey getPrivateKey()
    {
        return privateKey;
    }

    private String getSignatureAlgorithm()
    {
        return this.getScheme().getSignatureAlgorithm();
    }

    private String getEncryptionAlgorithm()
    {
        return this.getScheme().getSymmetricCipherAlgorithm();
    }

    private void setSecretKey(SecretKey secretKey)
    {
        this.secretKey = secretKey;
    }

    private SecretKey getSecretKey() throws TrustedObjectException
    {
        if (this.secretKey == null)
        {
            String cipherAlgo = this.getScheme().getAsymmetricCipherAlgorithm();
            SecretKeyWrapper skw = this.getWrappedKeyForMember(this.getMemberName());
            SignedWrapper sw = skw.getSignedSecretKey(cipherAlgo, this.getPrivateKey());
            byte[] keyBytes = (byte[]) sw.getVerifiedObject(this.getOwnerPublicKey(), this.getSignatureAlgorithm());
            this.setSecretKey(new SecretKeySpec(keyBytes, this.getEncryptionAlgorithm()));
        }
        return secretKey;
    }

    @Override
    public SignedWrapper signObject(Object forSignature) throws TrustedObjectException
    {
        return SignedWrapper.sign(forSignature, this.getMemberName(), this.getPrivateKey(), this.getSignatureAlgorithm());
    }

    @Override
    public Object getVerifiedObject(SignedWrapper forVerification, PublicKey sendersKey) throws TrustedObjectException
    {
        return forVerification.getVerifiedObject(sendersKey, this.getSignatureAlgorithm());
    }

    @Override
    public boolean verifySignature(SignedWrapper forVerification, PublicKey sendersKey) throws TrustedObjectException
    {
        return forVerification.verifySignature(sendersKey, this.getSignatureAlgorithm());
    }

    @Override
    public SealedWrapper sealObject(Object forSealing) throws TrustedObjectException
    {
        return SealedWrapper.seal(forSealing, this.getSecretKey(), this.getEncryptionAlgorithm());
    }

    @Override
    public Object unsealObject(SealedWrapper aParcel) throws TrustedObjectException
    {
        return SealedWrapper.unseal(aParcel, this.getSecretKey(), this.getEncryptionAlgorithm());
    }

    @Override
    public Scheme getScheme()
    {
        return this.getDomainHeader().getScheme();
    }

    @Override
    public void addDomainMember(String aMemberName, PublicKey aMemberPublicKey) throws TrustedObjectException
    {
        SecretKeyWrapper skw = SecretKeyWrapper.wrapSignedSecretKey(this.getSignedEncodedSecretKey(), this.getScheme().getAsymmetricCipherAlgorithm(), aMemberPublicKey);
        this.addKeyForMember(aMemberName, skw);
    }

    private SignedWrapper getSignedEncodedSecretKey() throws TrustedObjectException
    {
        return this.signObject(this.getEncodedSecretKey());
    }

    private byte[] getEncodedSecretKey() throws TrustedObjectException
    {
        return this.getSecretKey().getEncoded();
    }

    @Override
    public void removeDomainMember(String aMemberName) throws TrustedObjectException
    {
        this.deleteKeyForMember(aMemberName);
    }

    @Override
    public void requestMembership(Observer anObserver) throws TrustedObjectException
    {
        //otherwise we need to listen to see if we get added
        this.requestMembership(anObserver, getMemberName());
    }

    @Override
    public PublicKey getOwnerPublicKey()
    {
        return this.getDomainHeader().getOwnerPublicKey();
    }

    @Override
    public List<String> getMemberNames()
    {
        Set<String> keySet = (Set<String>) this.getDomainServiceCache().keySet();
        return new ArrayList<>(keySet);
    }

    @Override
    public boolean isMember(String aMemberName)
    {
        return this.getDomainServiceCache().containsKey(aMemberName);
    }

    @Override
    public SecretKeyWrapper getWrappedKeyForMember(String aMemberName) throws TrustedObjectException
    {
        if (this.isMember(aMemberName))
        {
            return (SecretKeyWrapper) this.getDomainServiceCache().get(aMemberName);
        } else
        {
            throw new TrustedObjectException(aMemberName + " is not a member of domain " + this.getDomainName());
        }
    }

    @Override
    public void requestMembership(Observer anObserver, String aMemberName)
    {
        MemberAddedListener mal = new MemberAddedListener(anObserver, aMemberName);
        this.getDomainServiceCache().addMapListener(mal, aMemberName, false);
    }

    @Override
    public void setPrivateKey(PrivateKey privateKey)
    {
        this.privateKey = privateKey;
    }

    private class MemberAddedListener extends AbstractMapListener
    {

        private Observer observer;
        private String memberName;

        private MemberAddedListener(Observer observer, String memberName)
        {
            this.observer = observer;
            this.memberName = memberName;
        }

        @Override
        public void entryInserted(MapEvent event)
        {
            observer.update(null, "Notification: " + this.memberName 
                    + " is now a member of the domain " + getDomainName());
        }
    }

    @Override
    public String getDomainOwner()
    {
        return this.getDomainHeader().getDomainOwner();
    }

    @Override
    public void addKeyForMember(String aMemberName, SecretKeyWrapper aWrappedKey)
    {
        HashMap hm = new HashMap();
        hm.put(aMemberName, aWrappedKey);
        this.getDomainServiceCache().putAll(hm);

    }

    @Override
    public void deleteKeyForMember(String aMemberName)
    {
        this.getDomainServiceCache().remove(aMemberName);
    }

    /**
     * @return the serviceCache
     */
    NamedCache getDomainServiceCache()
    {
        if (domainServiceCache == null)
        {
            domainServiceCache = CoherenceDomain.getDomainServiceCache(this.getDomainName());
        }
        return domainServiceCache;
    }

    static NamedCache getDomainServiceCache(String domainName)
    {
        return CacheFactory.getCache(domainName.concat(SERVICE_CACHE_SUFFIX));
    }

    /**
     * @return the domainHeader
     */
    private DomainHeader getDomainHeader()
    {
        return domainHeader;
    }
}
