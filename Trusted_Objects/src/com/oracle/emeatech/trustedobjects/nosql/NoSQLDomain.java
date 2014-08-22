/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.trustedobjects.nosql;

import com.oracle.emeatech.trustedobjects.Domain;
import com.oracle.emeatech.trustedobjects.DomainHeader;
import com.oracle.emeatech.trustedobjects.Scheme;
import com.oracle.emeatech.trustedobjects.SealedWrapper;
import com.oracle.emeatech.trustedobjects.SecretKeyWrapper;
import com.oracle.emeatech.trustedobjects.SignedWrapper;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observer;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.ValueVersion;
import org.apache.avro.generic.GenericRecord;

/**
 *
 * @author ewan
 */
public class NoSQLDomain implements Domain
{

    public static final String DOMAIN_ROOT = "domain_record";
    private DomainHeader domainHeader;
    private String memberName;
    private SecretKey secretKey;
    private PrivateKey privateKey;
    private static final String ENCRYPTED_KEY_BYTES = "encryptedKeyBytes",
            ENCRYPTED_SIG_BYTES = "encryptedSigBytes", SENDER_ID = "senderId";
    private static final int MEMBER_NAME_INDEX = 0;
    private Thread watcherThread;

    NoSQLDomain(DomainHeader domainHeader, String memberName)
    {
        this.domainHeader = domainHeader;
        this.memberName = memberName;
    }

    static boolean domainExists(String domainName) throws TrustedObjectException
    {
        if (getDomainHeader(domainName) == null)
        {
            return false;
        } else
        {
            return true;
        }
    }

    void writeDomainHeader() throws TrustedObjectException
    {
        Key domKey = getDomainKey(getDomainHeader().getDomainName());
        getStoreHandle().put(domKey, NoSQLDomainHeaderTranscriber.toValue(getDomainHeader()));
    }

    static DomainHeader getDomainHeader(String domainName) throws TrustedObjectException
    {
        ValueVersion headerVV = getStoreHandle().get(getDomainKey(domainName));
        if (headerVV == null)
        {
            return null;
        } else
        {
            return NoSQLDomainHeaderTranscriber.fromValue(headerVV.getValue());
        }
    }

    private static NoSQLHelper getHelper() throws TrustedObjectException
    {
        return NoSQLHelper.getInstance();
    }

    private static KVStore getStoreHandle() throws TrustedObjectException
    {
        return getHelper().getStoreHandle();
    }

    static Key getDomainKey(String aDomainName)
    {
        return Key.createKey(getDomainKeyList(aDomainName));
    }

    private static List<String> getDomainKeyList(String aDomainName)
    {
        List<String> keyComps = getRootKeyList();
        keyComps.add(aDomainName);
        return keyComps;
    }

    private static Key getRootKey()
    {
        return Key.createKey(getRootKeyList());
    }

    private Key getMemberKey(String aMemberName)
    {
        List<String> list = new ArrayList<>();
        list.add(aMemberName);
        return Key.createKey(this.getDomainKeyList(), list);
    }

    private Key getDomainKey()
    {
        return getDomainKey(this.getDomainName());
    }

    private List getDomainKeyList()
    {
        return getDomainKeyList(this.getDomainName());
    }

    private static List<String> getRootKeyList()
    {
        ArrayList<String> keyComps = new ArrayList<>();
        keyComps.add(DOMAIN_ROOT);
        return keyComps;
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

    private SignedWrapper getSignedEncodedSecretKey() throws TrustedObjectException
    {
        return this.signObject(this.getEncodedSecretKey());
    }

    private byte[] getEncodedSecretKey() throws TrustedObjectException
    {
        return this.getSecretKey().getEncoded();
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

    @Override
    public String getDomainOwner()
    {
        return this.getDomainHeader().getDomainOwner();
    }

    @Override
    public PublicKey getOwnerPublicKey()
    {
        return this.getDomainHeader().getOwnerPublicKey();
    }

    @Override
    public List<String> getMemberNames() throws TrustedObjectException
    {
        //so basically we need a list of the minor keys that are the member names
        ArrayList<String> memberNames = new ArrayList<>();
        Iterator iter = getStoreHandle().storeKeysIterator(Direction.UNORDERED, 0, getDomainKey(), null, Depth.CHILDREN_ONLY);

        while (iter.hasNext())
        {
            Key key = (Key) iter.next();
            memberNames.add(key.getMinorPath().get(MEMBER_NAME_INDEX));
        }
        return memberNames;
    }

    @Override
    public boolean isMember(String aMemberName) throws TrustedObjectException
    {
        return NoSQLDomain.isMember(this.getDomainName(), aMemberName);
    }

    static boolean isMember(String aDomainName, String aMemberName) throws TrustedObjectException
    {
        if (getStoreHandle().get(getMemberKey(aDomainName, aMemberName)) == null)
        {
            return false;
        } else
        {
            return true;
        }
    }

    private static Key getMemberKey(String aDomainName, String aMemberName)
    {
        List<String> majorPath = getDomainKeyList(aDomainName);
        List<String> minorPath = new ArrayList<>();
        minorPath.add(aMemberName);
        return Key.createKey(majorPath, minorPath);
    }

    @Override
    public SecretKeyWrapper getWrappedKeyForMember(String aMemberName) throws TrustedObjectException
    {
        if (this.isMember(aMemberName))
        {
            ValueVersion vv = getStoreHandle().get(getMemberKey(aMemberName));
            GenericRecord rec = getHelper().getBindingForClass(SecretKeyWrapper.class).toObject(vv.getValue());
            ByteBuffer eKeyByteBuf = (ByteBuffer) rec.get(ENCRYPTED_KEY_BYTES);
            ByteBuffer eSigByteBuf = (ByteBuffer) rec.get(ENCRYPTED_SIG_BYTES);
            String senderId = rec.get(SENDER_ID).toString();
            return new SecretKeyWrapper(eKeyByteBuf.array(), eSigByteBuf.array(), senderId);
        } else
        {
            throw new TrustedObjectException(aMemberName + " is not a member of domain " + this.getDomainName());
        }
    }

    /**
     * @return the watchers
     */
    private Thread getWatcherThread()
    {
        return watcherThread;
    }

    private void setWatcherThread(Thread watcherThread)
    {
        this.watcherThread = watcherThread;
    }

    private class MembershipWatcher implements Runnable
    {

        private String memberName;
        private Observer observer;
        private Exception lastException = null;

        private MembershipWatcher(Observer anObserver, String aMemberName)
        {
            this.memberName = aMemberName;
            this.observer = anObserver;
        }

        @Override
        public void run()
        {
            try
            {
                while (!isMember(memberName))
                {
                    Thread.sleep(60000);
                }
                this.observer.update(null, "Notification: " + this.memberName
                        + " is now a member of the domain " + getDomainName());

            } catch (TrustedObjectException | InterruptedException x)
            {
                this.lastException = x;
            }
        }
    }

    @Override
    public void requestMembership(Observer anObserver, String aMemberName) throws TrustedObjectException
    {
        if (!this.isMember(aMemberName) && this.getWatcherThread() == null)
        {
            this.setWatcherThread(new Thread(new MembershipWatcher(anObserver, aMemberName)));
            this.getWatcherThread().start();
        }
    }

    @Override
    public void requestMembership(Observer anObserver) throws TrustedObjectException
    {
        this.requestMembership(anObserver, this.getMemberName());
    }

    @Override
    public void removeDomainMember(String aMemberName) throws TrustedObjectException
    {
        getStoreHandle().delete(this.getMemberKey(aMemberName));
    }

    @Override
    public void addKeyForMember(String aMemberName, SecretKeyWrapper aWrappedKey) throws TrustedObjectException
    {
        GenericRecord rec = getHelper().getRecordForObject(aWrappedKey);
        rec.put(ENCRYPTED_KEY_BYTES, ByteBuffer.wrap(aWrappedKey.getEncryptedKeyBytes()));
        rec.put(ENCRYPTED_SIG_BYTES, ByteBuffer.wrap(aWrappedKey.getEncryptedSigBytes()));
        rec.put(SENDER_ID, aWrappedKey.getSenderId());
        getStoreHandle().put(getMemberKey(aMemberName), getHelper().getBindingForObject(aWrappedKey).toValue(rec));
    }

    @Override
    public void deleteKeyForMember(String aMemberName) throws TrustedObjectException
    {
        getStoreHandle().delete(getMemberKey(aMemberName));
    }

    @Override
    public void setPrivateKey(PrivateKey privateKey)
    {
        this.privateKey = privateKey;
    }

    /**
     * @return the privateKey
     */
    private PrivateKey getPrivateKey()
    {
        return privateKey;
    }

    /**
     * @return the domainHeader
     */
    private DomainHeader getDomainHeader()
    {
        return domainHeader;
    }
}
