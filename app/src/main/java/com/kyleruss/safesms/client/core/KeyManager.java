//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.safesms.client.core;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.kyleruss.hssa2.commons.CryptoCommons;
import com.kyleruss.hssa2.commons.CryptoUtils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class KeyManager
{
    private static KeyManager instance;

    //The clients public-private key pair
    private KeyPair clientKeyPair;

    //The current user session keys that the client has
    //Key: The users phone number/id
    //Value: The session key (AES key)
    private Map<String, SecretKeySpec> sessionKeys;

    //The current user public keys the client has
    //Key: The users phone number/id
    //Value: The users public key (RSA default)
    private Map<String, PublicKey> publicKeys;

    //The RSA public key of the server
    private PublicKey serverPublicKey;

    private KeyManager()
    {
        sessionKeys =   new HashMap<>();
        publicKeys  =   new HashMap<>();
    }

    //Creates and returns a public-private key pair for the client
    //Generates a 1024bit RSA key
    //Returns null on exception
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
            Log.e("[KEY_GENERATE_FAIL] ", e.getMessage());
            return null;
        }
    }

    //Generates 128 bit (16 byte) random bytes
    //which can be used as a session or AES key
    public byte[] generateSessionKey()
    {
        SecureRandom rGen   =   new SecureRandom();
        byte[] sessionKey   =   new byte[16];
        rGen.nextBytes(sessionKey);

        return sessionKey;
    }

    //Encrypts the passed session key with the passed receivers public key
    //Encryption is done using RSA
    //Returns null if there is no public key found for the passed user
    public byte[] wrapSessionKey(String receiver, byte[] sessionKey)
    throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException,
    BadPaddingException, NoSuchAlgorithmException, InvalidKeyException
    {
        PublicKey publicKey =   publicKeys.get(receiver);

        if(publicKey == null) return null;

        else
        {
            byte[] encryptedKey         =   CryptoCommons.publicEncrypt(sessionKey, publicKey);
            return encryptedKey;
        }
    }

    //Clears the session and public keys
    public void resetKeys()
    {
        sessionKeys.clear();
        publicKeys.clear();
    }

    //Loads the clients private-public key pair from storage
    //If keys are found in storage, the clientKeyPair is initialized
    //See ClientConfig.STORED_PUKEY_NAME and ClientConfig.STORED_PRKEY_NAME
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
            //Keys are base 64 encoded on storing, decode them
            byte[] publicKeyBytes   =   Base64.decode(storedPublicKey, Base64.DEFAULT);
            byte[] privateKeyBytes  =   Base64.decode(storedPrivateKey, Base64.DEFAULT);

            //Generate public, private keys from the stored bytes and initialize the client key pair
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

    //Stores the clients key pair in storage
    //Keys can be loaded again with KeyManager@loadClientKeyPair
    //See ClientConfig.STORED_PUKEY_NAME and ClientConfig.STORED_PRKEY_NAME
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

    //Sets the servers public key from a public key string
    //The string is base64 decoded and transformed into a RSA public key
    public void setServerPublicKey(String publicKeyStr)
    throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        byte[] keyBytes =   Base64.decode(publicKeyStr.getBytes("UTF-8"), Base64.DEFAULT);
        serverPublicKey =   (PublicKey) CryptoUtils.stringToAsymKey(keyBytes, true);
    }

    //Sets the passed users public key from the passed key bytes
    //Key bytes are transformed into a RSA public key
    //See CryptoUtils.stringToAsymKey
    public void setUserPublicKey(String userID, byte[] keyData)
    throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        PublicKey publicKey     =   (PublicKey) CryptoUtils.stringToAsymKey(keyData, true);
        publicKeys.put(userID, publicKey);
    }

    //Sets the passed users session key from the passed key bytes
    //Key bytes are transformed into an AES key
    public void setUserSessionKey(String userID, byte[] keyData)
    {
        SecretKeySpec keySpec   =   new SecretKeySpec(keyData, "AES");
        sessionKeys.put(userID, keySpec);
    }

    public KeyPair getClientKeyPair()
    {
        return clientKeyPair;
    }

    public void setClientKeyPair(KeyPair clientKeyPair)
    {
        this.clientKeyPair  =   clientKeyPair;
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
