//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
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
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

public class KeyManager
{
    private static KeyManager instance;
    private KeyPair clientKeyPair;
    private Map<String, SecretKeySpec> sessionKeys;
    private Map<String, PublicKey> publicKeys;
    private PublicKey serverPublicKey;

    private KeyManager()
    {
        sessionKeys =   new HashMap<>();
        publicKeys  =   new HashMap<>();
    }

    public KeyPair getClientKeyPair()
    {
        return clientKeyPair;
    }

    public void setClientKeyPair(KeyPair clientKeyPair)
    {
        this.clientKeyPair  =   clientKeyPair;
    }

    public KeyPair generateClientKeyPair()
    {
        try
        {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            return keyGen.generateKeyPair();
        }

        catch(NoSuchAlgorithmException e)
        {
            Log.e("KeyManager", e.getMessage());
            return null;
        }
    }

    public void resetKeys()
    {
        sessionKeys.clear();;
        publicKeys.clear();
    }

    public void loadClientKeyPair(Activity initActivity)
    {
        SharedPreferences sharedPreferences =   PreferenceManager.getDefaultSharedPreferences(initActivity);
        String storedPublicKey              =   sharedPreferences.getString(ClientConfig.STORED_PUKEY_NAME, null);
        String storedPrivateKey             =   sharedPreferences.getString(ClientConfig.STORED_PRKEY_NAME, null);

        if(storedPublicKey == null || storedPrivateKey == null)
        {
            clientKeyPair   =   null;
            return;
        }

        try
        {
            byte[] publicKeyBytes   =   Base64.decode(storedPublicKey, Base64.DEFAULT);
            byte[] privateKeyBytes  =   Base64.decode(storedPrivateKey, Base64.DEFAULT);
            PublicKey pubKey        =   (PublicKey) CryptoUtils.stringToAsymKey(publicKeyBytes, true);
            PrivateKey privKey      =   (PrivateKey) CryptoUtils.stringToAsymKey(privateKeyBytes, false);
            clientKeyPair           =   new KeyPair(pubKey, privKey);
        }

        catch(Exception e)
        {
            Log.d("LOAD_KEYPAIR_FAIL", e.getMessage());
            clientKeyPair   =   null;
        }
    }

    public void saveClientKeyPair(Activity initActivity)
    {
        if(clientKeyPair == null) return;

        SharedPreferences sharedPreferences =   PreferenceManager.getDefaultSharedPreferences(initActivity);
        SharedPreferences.Editor prefEditor =   sharedPreferences.edit();
        String privateKeyStr                =   Base64.encodeToString(clientKeyPair.getPrivate().getEncoded(), Base64.NO_WRAP);
        String publicKeyStr                 =   Base64.encodeToString(clientKeyPair.getPublic().getEncoded(), Base64.NO_WRAP);

        prefEditor.putString(ClientConfig.STORED_PRKEY_NAME, privateKeyStr);
        prefEditor.putString(ClientConfig.STORED_PUKEY_NAME, publicKeyStr);
        prefEditor.commit();
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
        Log.d("SERVER_PUBLIC_KEY", Base64.encodeToString(serverPublicKey.getEncoded(), Base64.NO_WRAP));
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

    public static KeyManager getInstance()
    {
        if(instance == null) instance = new KeyManager();
        return instance;
    }
}
