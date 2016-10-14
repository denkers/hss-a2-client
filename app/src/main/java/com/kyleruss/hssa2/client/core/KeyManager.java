//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import android.util.Base64;
import android.util.Log;

import com.kyleruss.hssa2.commons.CryptoUtils;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

public class KeyManager
{
    private static KeyManager instance;
    private KeyPair clientKeyPair;
    private Map<String, SecretKeySpec> sessionKeys;
    private Map<String, PublicKey> publicKeys;
    private PublicKey serverPublicKey;

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

    public void loadClientKeyPair()
    {

    }

    public void saveClientKeyPair()
    {

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

    public PublicKey getServerPublicKey()
    {
        return serverPublicKey;
    }

    public void setServerPublicKey(PublicKey serverPublicKey)
    {
        this.serverPublicKey    =   serverPublicKey;
    }

    public void setServerPublicKey(String publicKeyStr)
    throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        byte[] keyBytes =   Base64.decode(publicKeyStr.getBytes("UTF-8"), Base64.DEFAULT);
        serverPublicKey =   (PublicKey) CryptoUtils.stringToAsymKey(keyBytes, true);
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

    public void removeSessionKey(String userID)
    {
        sessionKeys.remove(userID);
    }

    public Map<String, PublicKey> getPublicKeys()
    {
        return publicKeys;
    }

    public PublicKey getPublicKey(String userID)
    {
        return publicKeys.get(userID);
    }

    public void removePublicKey(String userID)
    {
        publicKeys.remove(userID);
    }

    public static Key stringToAsymKey(String keyValue, boolean decode, boolean publicKey)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        byte[] keyBytes             =   decode? Base64.decode(keyValue.getBytes("UTF-8"), Base64.DEFAULT) : keyValue.getBytes("UTF-8");
        KeySpec keySpec             =   publicKey? new X509EncodedKeySpec(keyBytes) : new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory       =   KeyFactory.getInstance("RSA");
        return publicKey? keyFactory.generatePublic(keySpec) : keyFactory.generatePrivate(keySpec);
    }

    public static KeyManager getInstance()
    {
        if(instance == null) instance = new KeyManager();
        return instance;
    }
}
