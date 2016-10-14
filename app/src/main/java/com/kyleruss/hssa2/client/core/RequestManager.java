//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import com.kyleruss.hssa2.commons.CryptoUtils;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RequestManager
{
    private static RequestManager instance;
    private Map<String, String> requests;

    private RequestManager()
    {
        requests    =   new HashMap<>();
    }

    public Map<String, String> getRequests()
    {
        return requests;
    }

    public void addRequest(String requestID, String nonce)
    {
        requests.put(requestID, nonce);
    }

    public void removeRequest(String requestID)
    {
        requests.remove(requestID);
    }

    public String getRequestNonce(String requestID)
    {
        return requests.get(requestID);
    }

    public Collection<String> getRequestIDList()
    {
        return requests.keySet();
    }

    public Map.Entry<String, String> generateRequest()
    {
        String requestID;
        do requestID    =   CryptoUtils.generateRandomString(4, CryptoUtils.ALPHA_NUMERIC);
        while(hasRequest(requestID));

        String nonce    =   CryptoUtils.generateRandomString(6, CryptoUtils.ALPHA_NUMERIC);
        requests.put(requestID, nonce);
        return new AbstractMap.SimpleEntry<>(requestID, nonce);
    }

    public boolean hasRequest(String requestID)
    {
        return requests.containsKey(requestID);
    }

    public boolean verifyAndDestroy(String requestID, String nonce)
    {
        if(!hasRequest(requestID)) return false;
        boolean result  =   verifyNonce(requestID, nonce);
        removeRequest(requestID);

        return result;
    }

    public boolean verifyNonce(String requestID, String nonce)
    {
        if(!hasRequest(requestID)) return false;

        String storedNonce  =   requests.get(requestID);
        return storedNonce.equals(nonce);
    }

    public static RequestManager getInstance()
    {
        if(instance == null) instance   =   new RequestManager();
        return instance;
    }
}
