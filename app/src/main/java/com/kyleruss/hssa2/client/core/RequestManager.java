//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import java.util.Collection;
import java.util.Map;

public class RequestManager
{
    private Map<String, String> requests;

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

    public boolean hasRequest(String requestID)
    {
        return requests.containsKey(requestID);
    }

    public boolean verifyNonce(String requestID, String nonce)
    {
        if(!hasRequest(requestID)) return false;

        String storedNonce  =   requests.get(requestID);
        return storedNonce.equals(nonce);
    }
}
