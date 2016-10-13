//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import android.util.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

public class KeyManager
{
    private static KeyManager instance;
    private KeyPair clientKeyPair;
    private Map<String, SecretKeySpec> sessionKeys;

    private KeyManager() {}

    public KeyPair getClientKeyPair()
    {
        return clientKeyPair;
    }

    public void generateClientKeyPair()
    {
        try
        {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            clientKeyPair = keyGen.generateKeyPair();
        }

        catch(NoSuchAlgorithmException e)
        {
            clientKeyPair   =   null;
            Log.e("KeyManager", e.getMessage());
        }
    }

    public PrivateKey getClientPrivateKey()
    {
        if(clientKeyPair == null) return null;
        else return clientKeyPair.getPrivate();
    }

    public PublicKey getClientPublicKey()
    {
        if(clientKeyPair == null) return null;
        else return clientKeyPair.getPublic();
    }

    public Map<String, SecretKeySpec> getSessionKeys()
    {
        return sessionKeys;
    }

    public SecretKeySpec getSessionKey(String userID)
    {
        return sessionKeys.get(userID);
    }

    public void addSessionKey(String userID, SecretKeySpec key)
    {
        sessionKeys.put(userID, key);
    }

    public static KeyManager getInstance()
    {
        if(instance == null) instance = new KeyManager();
        return instance;
    }
}
