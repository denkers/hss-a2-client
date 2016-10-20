//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.communication;

import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kyleruss.hssa2.commons.CryptoCommons;
import com.kyleruss.hssa2.commons.EncryptedSession;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CommUtils
{
    //Takes an encrypted session message and prepares a request
    //The request has two parameters: key, data
    //The session key is encrypted using the sessions asymmetric key (public/private)
    //Then the message is encrypted using the session key with AES
    public static ServiceRequest prepareEncryptedSessionRequest(EncryptedSession session)
    throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException
    {
        session.initCipher(Cipher.ENCRYPT_MODE);

        //Encrypt data
        String data =   URLEncoder.encode(Base64.encodeToString(session.processData(), Base64.NO_WRAP), "UTF-8");
        //Encrypt session key
        String key  =   URLEncoder.encode(Base64.encodeToString(session.encryptKey(), Base64.NO_WRAP), "UTF-8");

        ServiceRequest serviceRequest   =   new ServiceRequest();
        serviceRequest.addParam("key", key);
        serviceRequest.addParam("data", data);
        return serviceRequest;
    }

    //Parses a string response to generate a json object and reads the key & data from it
    //The key is then decrypted using the passed asymKey then the data is decrypted with AES using the key
    public static EncryptedSession decryptSessionResponse(String response, Key asymKey)
    throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
    {
        JsonObject responseObj = CommUtils.parseJsonInput(response);
        byte[] key = Base64.decode(responseObj.get("key").getAsString(), Base64.DEFAULT);
        byte[] data = Base64.decode(responseObj.get("data").getAsString(), Base64.DEFAULT);
        EncryptedSession encSession = new EncryptedSession(key, data, asymKey);
        encSession.unlock();

        return encSession;
    }

    //See decryptSessionResponse(String, Key)
    //Passes a json object and uses a default data key of 'clientdata'
    public static ServiceRequest preparePublicEncryptedRequest(JsonObject data, Key key)
    throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException,
    BadPaddingException, NoSuchAlgorithmException, InvalidKeyException
    {
        return preparePublicEncryptedRequest(data, key, "clientData");
    }

    //RSA Encrypts the passed data with the passes asymmetric key
    //Returns a request with the data added at the paramName
    public static ServiceRequest preparePublicEncryptedRequest(JsonObject data, Key key, String paramName)
    throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException,
    BadPaddingException, NoSuchAlgorithmException, InvalidKeyException
    {
        String encData = Base64.encodeToString(CryptoCommons.publicEncrypt(data.toString().getBytes("UTF-8"), key), Base64.NO_WRAP);
        ServiceRequest serviceRequest   =   new ServiceRequest();
        serviceRequest.addParam(paramName, encData);

        return serviceRequest;
    }

    //Generates a JsonObject request object with a request ID and nonce
    public static JsonObject prepareAuthenticatedRequest(String requestID, String nonce)
    {
        JsonObject requestObj   =   new JsonObject();
        requestObj.addProperty("requestID", requestID);
        requestObj.addProperty("nonce", nonce);

        return requestObj;
    }

    //Parses the json string and returns it's JsonObject
    public static JsonObject parseJsonInput(String json)
    {
        return new JsonParser().parse(json).getAsJsonObject();
    }
}
