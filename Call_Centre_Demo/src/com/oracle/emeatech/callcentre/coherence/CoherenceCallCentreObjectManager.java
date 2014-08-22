/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.coherence;

import com.oracle.emeatech.callcentre.bulletinboard.EncodedBulletinWrapper;
import com.oracle.emeatech.callcentre.demo.CallCentreObjectManager;
import com.oracle.emeatech.callcentre.paymentcard.CardType;
import com.oracle.emeatech.callcentre.paymentcard.EncodedPaymentCard;
import com.oracle.emeatech.coherence.utility.IntegerSequence;
import com.oracle.emeatech.coherence.utility.SequenceException;
import com.oracle.emeatech.trustedobjects.EncodedObject;
import com.oracle.emeatech.trustedobjects.SignedWrapper;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.MultiExtractor;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.processor.ExtractorProcessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author ewan
 */
public class CoherenceCallCentreObjectManager implements CallCentreObjectManager
{

    public static String CARD_CACHE = "card-cache";
    public static String PUBLIC_MESSAGE_CACHE = "public-message-cache";
    public static String PRIVATE_MESSAGE_CACHE = "private-message-cache";
    public static String SEQ_CACHE = "seq-cache";
    public static String CARD_ID_SEQ = "card-id-seq";
    public static String MESSAGE_ID_SEQ = "message-id-seq";
    public static String AMEX = "AMEX";
    public static String VISA = "VISA";

    private NamedCache getCardCache()
    {
        return CacheFactory.getCache(CARD_CACHE);
    }

    private NamedCache getPrivateMessageCache()
    {
        return CacheFactory.getCache(PRIVATE_MESSAGE_CACHE);
    }

    private NamedCache getPublicMessageCache()
    {
        return CacheFactory.getCache(PUBLIC_MESSAGE_CACHE);
    }

    @Override
    public void storeCard(Object key, EncodedObject card)
    {
        this.putEncoded(this.getCardCache(), key, card);
    }

    private void putEncoded(NamedCache aCache, Object key, EncodedObject enc)
    {
        Map map = new HashMap();
        map.put(key, enc);
        aCache.putAll(map);
    }

    private EncodedObject getEncoded(NamedCache aCache, Object key)
    {
        return (EncodedObject) aCache.get(key);
    }

    @Override
    public Map<Object, List> listUnencryptedCardData()
    {
        ArrayList<PofExtractor> extractorList = new ArrayList<PofExtractor>();
        extractorList.add(new PofExtractor(CardType.class, EncodedPaymentCard.CARD_TYPE));
        extractorList.add(new PofExtractor(String.class, EncodedPaymentCard.NAME_ON_CARD));
        extractorList.add(new PofExtractor(Date.class, EncodedPaymentCard.EXPIRY_DATE));
        PofExtractor[] extractorArray = new PofExtractor[extractorList.size()];
        extractorArray = extractorList.toArray(extractorArray);
        ExtractorProcessor ep = new ExtractorProcessor(new MultiExtractor(extractorArray));
        return this.runExtractorProcessor(ep, this.getCardCache());
    }

    @Override
    public Map<Object, List> listUnencryptedPrivateMessageData()
    {
        ArrayList<PofExtractor> extractorList = new ArrayList<PofExtractor>();
        extractorList.add(new PofExtractor(Date.class, EncodedBulletinWrapper.DATE));
        extractorList.add(new PofExtractor(null, EncodedBulletinWrapper.SENDER_ID));
        PofExtractor[] extractorArray = new PofExtractor[extractorList.size()];
        extractorArray = extractorList.toArray(extractorArray);
        ExtractorProcessor ep = new ExtractorProcessor(new MultiExtractor(extractorArray));
        return this.runExtractorProcessor(ep, this.getPrivateMessageCache());
    }
    
    

    private Map<Object, List> runExtractorProcessor(ExtractorProcessor ep, NamedCache cache)
    {
        return new TreeMap<Object, List>(cache.invokeAll((Filter) null, ep));
    }

    @Override
    public EncodedObject retrieveCard(Object key)
    {
        return (EncodedPaymentCard) this.getEncoded(this.getCardCache(), key);
    }

    @Override
    public void storePublicBulletin(Integer bulletinId, SignedWrapper bulletin)
    {
        HashMap<Integer, SignedWrapper> hm = new HashMap();
        hm.put(bulletinId, bulletin);
        this.getPublicMessageCache().putAll(hm);
    }

    @Override
    public SignedWrapper retrievePublicBulletin(Integer bulletinId)
    {
        return (SignedWrapper) this.getPublicMessageCache().get(bulletinId);
    }

    @Override
    public void storePrivateBulletin(String bulletinId, EncodedObject bulletin)
    {
        this.putEncoded(this.getPrivateMessageCache(), bulletinId, bulletin);
    }

    @Override
    public EncodedObject retrievePrivateBulletin(String bulletinId)
    {
        return this.getEncoded(this.getPrivateMessageCache(), bulletinId);
    }

    @Override
    public Set<Integer> listPublicBulletinIds()
    {
        return (Set<Integer>) getPublicMessageCache().keySet();
    }

    @Override
    public Integer getNextCardSequenceNumber() throws TrustedObjectException
    {
        return getNextSequenceNumber(CARD_ID_SEQ);
    }

    @Override
    public Integer getNextBulletinSequenceNumber() throws TrustedObjectException
    {
        return getNextSequenceNumber(MESSAGE_ID_SEQ);
    }

    private Integer getNextSequenceNumber(String sequenceName) throws TrustedObjectException
    {
        try
        {
        if (!IntegerSequence.sequenceExists(SEQ_CACHE, sequenceName))
        {
            IntegerSequence.makeSequence(SEQ_CACHE, sequenceName, 0);
        }
        return IntegerSequence.getNextSequenceNumber(SEQ_CACHE, sequenceName);
        } catch (SequenceException x)
        {
            throw new TrustedObjectException(x);
        }
    }
}
