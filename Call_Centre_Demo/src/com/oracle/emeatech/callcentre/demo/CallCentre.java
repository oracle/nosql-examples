package com.oracle.emeatech.callcentre.demo;

import com.oracle.emeatech.callcentre.bulletinboard.BulletinWrapper;
import com.oracle.emeatech.callcentre.bulletinboard.Bulletin;
import com.oracle.emeatech.callcentre.coherence.CoherenceCallCentreObjectManager;
import com.oracle.emeatech.callcentre.nosql.NoSQLCallCentreObjectManager;
import com.oracle.emeatech.callcentre.paymentcard.CardFactory;
import com.oracle.emeatech.callcentre.paymentcard.CardType;
import com.oracle.emeatech.callcentre.paymentcard.PaymentCard;
import com.oracle.emeatech.callcentre.paymentcard.PaymentCardException;
import com.oracle.emeatech.coherence.utility.SequenceException;
import com.oracle.emeatech.trustedobjects.Domain;
import com.oracle.emeatech.trustedobjects.EncodedObject;
import com.oracle.emeatech.trustedobjects.PublicKeyExchange;
import com.oracle.emeatech.trustedobjects.Scheme;
import com.oracle.emeatech.trustedobjects.SecretKeyWrapper;
import com.oracle.emeatech.trustedobjects.SignedWrapper;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import com.oracle.emeatech.trustedobjects.TrustedObjectService;
import com.oracle.emeatech.trustedobjects.keys.KeyStoreDriver;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Menu driven "UI" class for the Call Centre demo. Users join or create a
 * domain based upon credit card type (e.g. VISA or AMEX).
 *
 * Users are able to create and view Credit Card data and messages.
 *
 * Credit Card number and CVV fields are encrypted and can only be viewed by
 * members of the same domain that created them. Other fields such as cardholder
 * name can be viewed by anyone.
 *
 * Messages can be public or private ("domain eyes only"). Both types of
 * messages are signed and the signature will be verified if the sender has
 * published their public key.
 *
 * Private message contents can only be decrypted by members of the same domain
 * as the sender.
 *
 * @author ewan
 */
public class CallCentre implements Observer
{

    private String id;
    private String keyStorePath;
    private KeyStoreDriver keyStoreDriver;
    private Scanner scanner = new Scanner(System.in);
    public static String AMEX = "AMEX";
    public static String VISA = "VISA";
    private Domain domain;
    private boolean waitingToJoin = false;
    private TrustedObjectService trustedObjectService;
    private CallCentreObjectManager callCentreObjectManager;

    private String getId()
    {
        if (this.id == null)
        {
            this.id = askUser("Your Name?");
        }
        return this.id;
    }

    private KeyStoreDriver getKeyStoreDriver() throws TrustedObjectException
    {
        if (this.keyStoreDriver == null)
        {
            this.keyStoreDriver = KeyStoreDriver.getInstance(getKeyStorePath(), getKeystorePassword(), getPrivateKeyPassword());
        }
        return this.keyStoreDriver;
    }

    private String getKeyStorePath()
    {
        if (this.keyStorePath == null)
        {
            this.keyStorePath = askUser("Full path of keystore file?");
        }
        return this.keyStorePath;
    }

    private String getKeystorePassword()
    {
        return askUser("Keystore Password");
    }

    private String getPrivateKeyPassword()
    {
        return askUser("Private Key Password?");
    }

    private String askUser(String question)
    {
        System.out.println(question);
        return scanner.nextLine().trim();
    }

    private Integer askUserForInteger(String question)
    {
        String s = this.askUser(question);
        if ((s.length() > 0) && (isOnlyDigits(s)))
        {
            return Integer.parseInt(s);
        } else
        {
            return null;
        }
    }

    private Integer askUserForInteger(String question, Integer defaultValue)
    {
        Integer i = askUserForInteger(question);
        if (i != null)
        {
            return i;
        } else
        {
            return defaultValue;
        }
    }

    private PublicKeyExchange getPublicKeyExchange()
    {
        return this.getTrustedObjectService().getPublicKeyExchange();
    }

    private void listPublicKeys() throws TrustedObjectException
    {
        Set keys = this.getPublicKeyExchange().listPublicKeys();
        for (Object o : keys)
        {
            System.out.println(o);
        }
    }

    private void publishMyPublicKey() throws TrustedObjectException
    {
        this.getPublicKeyExchange().publishPublicKey(getId(), getPublicKey());
    }

    private PublicKey getPublicKey() throws TrustedObjectException
    {
        return getKeyStoreDriver().getPublicKey(getId());
    }

    private PrivateKey getPrivateKey() throws TrustedObjectException
    {
        return getKeyStoreDriver().getPrivateKey(getId());
    }

    private Domain getDomain() throws TrustedObjectException
    {
        if (domain == null)
        {
            domain = this.getTrustedObjectService().getDomain(this.getClientDomainName(), this.getId());
            domain.setPrivateKey(this.getPrivateKey());
        }
        return domain;
    }

    private String getClientDomainName()
    {
        return System.getProperty("trustedobjects.domainname");
    }

    private boolean hasAlreadyJoined() throws TrustedObjectException
    {
        return getTrustedObjectService().isMember(this.getClientDomainName(), this.getId());
    }

    private void attemptToJoinDomain() throws TrustedObjectException
    {
        if (getTrustedObjectService().getPublicKeyExchange().retrievePublicKey(getId()) == null)
        {
            this.publishMyPublicKey();
        }

        if (this.getTrustedObjectService().domainExists(getClientDomainName()))
        {
            if (hasAlreadyJoined())
            {
                System.out.println("Already a member of the domain " + getClientDomainName());
            } else
            {
                if (!isWaitingToJoin())
                {
                    applyToJoin();
                    System.out.println("Applied to join domain " + getClientDomainName());
                } else
                {
                    System.out.println("Still waiting to join domain " + getClientDomainName());
                }
            }
        } else
        {
            Scheme s = getTrustedObjectService().getDefaultScheme();
            byte[] encodedSecretKey = KeyStoreDriver.makeNewSecretKey(s.getSecretKeySize(), s.getSecretKeyType()).getEncoded();
            SignedWrapper sw = SignedWrapper.sign(encodedSecretKey, this.getId(), this.getPrivateKey(), s.getSignatureAlgorithm());
            SecretKeyWrapper skw = SecretKeyWrapper.wrapSignedSecretKey(sw, s.getAsymmetricCipherAlgorithm(), this.getPublicKey());
            this.getTrustedObjectService().createDomain(this.getClientDomainName(), this.getId(), this.getPublicKey(), skw);
            System.out.println("Created domain " + getClientDomainName());
        }
    }

    @Override
    public void update(Observable o, Object o1)
    {
        System.out.println(o1);
    }

    private void applyToJoin() throws TrustedObjectException
    {
        this.getDomain().requestMembership(this, this.getId());
        this.setWaitingToJoin(true);
    }

    /**
     * @return the trustedObjectService
     */
    private TrustedObjectService getTrustedObjectService()
    {
        return trustedObjectService;
    }

    /**
     * @param trustedObjectService the trustedObjectService to set
     */
    private void setTrustedObjectService(TrustedObjectService trustedObjectService)
    {
        this.trustedObjectService = trustedObjectService;
    }

    private void addMember() throws TrustedObjectException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        String memberName = this.askUser("Name of the member to add?");
        PublicKey memberPublicKey = this.getPublicKeyExchange().retrievePublicKey(memberName);
        this.getDomain().addDomainMember(memberName, memberPublicKey);
    }

    private void addNewCardToStore() throws PaymentCardException, SequenceException,
            TrustedObjectException, KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException, UnrecoverableEntryException
    {
        PaymentCard card;
        if (this.getClientDomainName().equalsIgnoreCase(AMEX))
        {
            card = CardFactory.newAMEX();
        } else if (this.getClientDomainName().equalsIgnoreCase(VISA))
        {
            card = CardFactory.newVisa();
        } else
        {
            throw new PaymentCardException("Unknown card type " + this.getClientDomainName());
        }
        System.out.println(card);
        Integer nextSeqNo = getCallCentreObjectManager().getNextCardSequenceNumber();
        getCallCentreObjectManager().storeCard(nextSeqNo, card.encode(this.getDomain()));
    }

    private CallCentreObjectManager getCallCentreObjectManager() throws TrustedObjectException
    {
        if (this.callCentreObjectManager == null)
        {
            String objectManagerClassName =
                    System.getProperty("com.oracle.emeatech.callcentre.objectManager");
            try
            {
                this.callCentreObjectManager = (CallCentreObjectManager) Class.forName(objectManagerClassName).newInstance();
            } catch (Exception ex)
            {
                throw new TrustedObjectException("Could not create CallCentreObjectManager.  "
                        + "Tried to create an instance of " + objectManagerClassName, ex);
            }
        }
        return this.callCentreObjectManager;
    }

    private static boolean isOnlyDigits(String s)
    {
        char[] cArray = s.toCharArray();
        for (char c : cArray)
        {
            if (!Character.isDigit(c))
            {
                return false;
            }
        }
        return true;
    }

    private void retrieveCardById() throws KeyStoreException, TrustedObjectException,
            NoSuchAlgorithmException, UnrecoverableKeyException, UnrecoverableEntryException
    {
        Integer key;

        do
        {
            key = this.askUserForInteger("id of card to retrieve?");
        } while (key == null);

        EncodedObject enc = this.getCallCentreObjectManager().retrieveCard(key);
        PaymentCard card = (PaymentCard) enc.decode(this.getDomain());
        System.out.println(card);
    }

    private Bulletin newBulletin()
    {
        return new Bulletin(new Date(), this.id, this.askUser("message?"));
    }

    private void sendPublicMessage() throws TrustedObjectException, SequenceException
    {
        Integer newBulletinId = this.getCallCentreObjectManager().getNextBulletinSequenceNumber();
        this.getCallCentreObjectManager().storePublicBulletin(newBulletinId, this.getDomain().signObject(newBulletin()));
    }

    private void sendPrivateMessage() throws TrustedObjectException, KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException, UnrecoverableEntryException, SequenceException
    {
        BulletinWrapper bw = new BulletinWrapper(this.getDomain().signObject(newBulletin()));
        EncodedObject bwEnc = bw.encode(this.getDomain());
        Integer bwId = this.getCallCentreObjectManager().getNextBulletinSequenceNumber();
        String key = this.getClientDomainName() + bwId;
        this.getCallCentreObjectManager().storePrivateBulletin(key, bwEnc);

    }

    private void readPrivateMessage() throws KeyStoreException, TrustedObjectException,
            NoSuchAlgorithmException, UnrecoverableKeyException, UnrecoverableEntryException, InvalidKeySpecException
    {
        String msgId = askUser("message id?");
        EncodedObject encObj = this.getCallCentreObjectManager().retrievePrivateBulletin(msgId);
        BulletinWrapper bw = (BulletinWrapper) encObj.decode(this.getDomain());
        SignedWrapper sw = bw.getSignedContent();
        System.out.println("\n<message id=" + msgId + ">");
        System.out.println(sw);
        System.out.println(this.signatureStatus(sw));
        System.out.println("</message>\n");

    }

    private String signatureStatus(SignedWrapper sw) throws NoSuchAlgorithmException, InvalidKeySpecException,
            KeyStoreException, UnrecoverableKeyException, UnrecoverableEntryException, TrustedObjectException
    {
        PublicKey sendersKey;
        String sigFlag;
        if (sw.getSignatoryId() == null ? this.getId() == null : sw.getSignatoryId().equals(this.getId()))
        {
            sendersKey = this.getPublicKey();
        } else
        {
            sendersKey = this.getPublicKeyExchange().retrievePublicKey(sw.getSignatoryId().toString());
        }
        if (sendersKey != null)
        {
            if (this.getDomain().verifySignature(sw, sendersKey))
            {
                sigFlag = "SUCCESS:  Verified signature found for " + sw.getSignatoryId();
            } else
            {
                sigFlag = "WARNING: Invalid signature found for " + sw.getSignatoryId();
            }
        } else
        {
            sigFlag = "WARNING:  Could not verify signature as no public key found for " + sw.getSignatoryId();
        }
        return sigFlag;
    }

    private void listPublicMessages() throws NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException,
            UnrecoverableKeyException, UnrecoverableEntryException, TrustedObjectException
    {
        Set<Integer> bulletinKeys = getCallCentreObjectManager().listPublicBulletinIds();
        for (Integer bulletinKey : bulletinKeys)
        {
            SignedWrapper sw = this.getCallCentreObjectManager().retrievePublicBulletin(bulletinKey);
            System.out.println("\n<message id=" + bulletinKey + ">");
            System.out.println(sw);
            System.out.println(this.signatureStatus(sw));
            System.out.println("</message>\n");
        }
    }

    private void menu() throws Exception
    {
        boolean keepGoing = true;
        int option;
        String menu = "Menu:\n"
                + "1.  Publish public key\n"
                + "2.  Join Domain " + getClientDomainName() + "\n"
                + "3.  List public keys\n"
                + "4.  Add member to Domain " + getClientDomainName() + "\n"
                + "5.  Add new " + getClientDomainName() + " card to grid\n"
                + "6.  List all cards by card id, card type, cardholder name, expiry date\n"
                + "7.  Retrieve card by card id\n"
                + "8.  Send a public message\n"
                + "9.  List public messages\n"
                + "10. Send a private message\n"
                + "11. List private messages\n"
                + "12. Read a private message\n"
                + "Anything else to quit";
        do
        {
            System.out.println(menu);
            option = this.askUserForInteger("Choose an option", 0);

            switch (option)
            {
                case 1:
                    this.publishMyPublicKey();
                    break;
                case 2:
                    this.attemptToJoinDomain();
                    break;
                case 3:
                    this.listPublicKeys();
                    break;
                case 4:
                    this.addMember();
                    break;
                case 5:
                    this.addNewCardToStore();
                    break;
                case 6:
                    this.listUnencryptedCardData();
                    break;
                case 7:
                    this.retrieveCardById();
                    break;
                case 8:
                    this.sendPublicMessage();
                    break;
                case 9:
                    this.listPublicMessages();
                    break;
                case 10:
                    this.sendPrivateMessage();
                    break;
                case 11:
                    this.listUnencryptedPrivateMessageData();
                    break;
                case 12:
                    this.readPrivateMessage();
                    break;
                default:
                    keepGoing = false;
            }
        } while (keepGoing);
    }

    public static void main(String[] args)
    {
        try
        {
            CallCentre cc = new CallCentre();
            cc.setTrustedObjectService(TrustedObjectService.getInstance());
            cc.menu();
        } catch (Exception x)
        {
            x.printStackTrace();
        }
    }

    public void spike() throws TrustedObjectException
    {
        CardType amex = CardType.AMEX;
        System.out.println(amex.toString());
    }

    /**
     * @return the waitingToJoin
     */
    private boolean isWaitingToJoin()
    {
        return waitingToJoin;
    }

    /**
     * @param waitingToJoin the waitingToJoin to set
     */
    private void setWaitingToJoin(boolean waitingToJoin)
    {
        this.waitingToJoin = waitingToJoin;
    }

    private void listResults(Map<Object, List> sortedResults)
    {
        for (Map.Entry<Object, List> entry : sortedResults.entrySet())
        {
            StringBuilder sb = new StringBuilder();
            sb.append(entry.getKey());
            Object[] oArr = entry.getValue().toArray();
            for (int i = 0; i < oArr.length; i++)
            {
                sb.append(", ").append(oArr[i]);
            }
            System.out.println(sb.toString());
        }
    }

    private void listUnencryptedCardData() throws TrustedObjectException
    {
        this.listResults(this.getCallCentreObjectManager().listUnencryptedCardData());
    }

    private void listUnencryptedPrivateMessageData() throws TrustedObjectException
    {
        this.listResults(this.getCallCentreObjectManager().listUnencryptedPrivateMessageData());
    }
}
