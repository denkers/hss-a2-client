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

    //The current requests that have been sent
    //Key: The request ID
    //Value: A nonce value attached to the request
    private Map<String, String> requests;

    private RequestManager()
    {
        requests    =   new HashMap<>();
    }

    //Generates a request-nonce pair for the request
    //The request ID is used to identify the request later
    //The nonce is used to authenticate the message ensuring it is not tampered
    public Map.Entry<String, String> generateRequest()
    {
        //Generate a random, 4 length alphanumeric request ID
        String requestID;
        do requestID    =   CryptoUtils.generateRandomString(4, CryptoUtils.ALPHA_NUMERIC);
        while(hasRequest(requestID));

        //Generate a random, 6 length alphanumeric nonce value
        String nonce    =   CryptoUtils.generateRandomString(6, CryptoUtils.ALPHA_NUMERIC);
        requests.put(requestID, nonce);
        return new AbstractMap.SimpleEntry<>(requestID, nonce);
    }

    //Checks if the request ID exists and also if the nonce matches
    //Returns true if the nonce values match; false otherwise
    //Removes the request from the request list once it's finished verifying
    public boolean verifyAndDestroy(String requestID, String nonce)
    {
        if(!hasRequest(requestID)) return false;
        boolean result  =   verifyNonce(requestID, nonce);
        removeRequest(requestID);

        return result;
    }

    //Does the same as RequestManager@verifiyAndDestroy but it
    //does not remove the nonce once it has been verified
    public boolean verifyNonce(String requestID, String nonce)
    {
        if(!hasRequest(requestID)) return false;

        String storedNonce  =   requests.get(requestID);
        return storedNonce.equals(nonce);
    }

    public boolean hasRequest(String requestID)
    {
        return requests.containsKey(requestID);
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

    public static RequestManager getInstance()
    {
        if(instance == null) instance   =   new RequestManager();
        return instance;
    }
}
