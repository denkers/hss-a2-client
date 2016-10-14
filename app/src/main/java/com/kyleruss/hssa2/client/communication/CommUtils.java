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
    public static ServiceRequest prepareEncryptedSessionRequest(EncryptedSession session)
    throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException
    {
        session.initCipher(Cipher.ENCRYPT_MODE);
        String data =   URLEncoder.encode(Base64.encodeToString(session.processData(), Base64.NO_WRAP), "UTF-8");
        String key  =   URLEncoder.encode(Base64.encodeToString(session.encryptKey(), Base64.NO_WRAP), "UTF-8");

        ServiceRequest serviceRequest   =   new ServiceRequest();
        serviceRequest.addParam("key", key);
        serviceRequest.addParam("data", data);
        return serviceRequest;
    }

    public static ServiceRequest preparePublicEncryptedRequest(JsonObject data, Key key)
    throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException,
    BadPaddingException, NoSuchAlgorithmException, InvalidKeyException
    {
        return preparePublicEncryptedRequest(data, key, "clientData");
    }

    public static ServiceRequest preparePublicEncryptedRequest(JsonObject data, Key key, String paramName)
    throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException,
    BadPaddingException, NoSuchAlgorithmException, InvalidKeyException
    {
        String encData = Base64.encodeToString(CryptoCommons.publicEncrypt(data.toString().getBytes("UTF-8"), key), Base64.NO_WRAP);
        ServiceRequest serviceRequest   =   new ServiceRequest();
        serviceRequest.addParam(paramName, encData);

        return serviceRequest;
    }

    public static JsonObject prepareAuthenticatedRequest(String requestID, String nonce)
    {
        JsonObject requestObj   =   new JsonObject();
        requestObj.addProperty("requestID", requestID);
        requestObj.addProperty("nonce", nonce);

        return requestObj;
    }

    public static JsonObject parseJsonInput(String json)
    {
        return new JsonParser().parse(json).getAsJsonObject();
    }
}
