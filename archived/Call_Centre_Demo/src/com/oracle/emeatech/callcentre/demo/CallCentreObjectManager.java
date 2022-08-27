/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.emeatech.callcentre.demo;

import com.oracle.emeatech.trustedobjects.EncodedObject;
import com.oracle.emeatech.trustedobjects.SignedWrapper;
import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface provides object management services for the CallCentre demo
 * application
 *
 * @author ewan
 */
public interface CallCentreObjectManager
{

    public void storeCard(Object key, EncodedObject card) throws TrustedObjectException;

    public EncodedObject retrieveCard(Object key) throws TrustedObjectException;

    public void storePublicBulletin(Integer bulletinId, SignedWrapper bulletin) throws TrustedObjectException;

    public SignedWrapper retrievePublicBulletin(Integer bulletinId) throws TrustedObjectException;

    public Set<Integer> listPublicBulletinIds() throws TrustedObjectException;

    public void storePrivateBulletin(String bulletinId, EncodedObject bulletin) throws TrustedObjectException;

    public EncodedObject retrievePrivateBulletin(String bulletinId) throws TrustedObjectException;

    public Integer getNextCardSequenceNumber() throws TrustedObjectException;

    public Integer getNextBulletinSequenceNumber() throws TrustedObjectException;

    public Map<Object, List> listUnencryptedCardData() throws TrustedObjectException;

    public Map<Object, List> listUnencryptedPrivateMessageData()  throws TrustedObjectException;
}
