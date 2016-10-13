//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonObject;
import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.communication.CommUtils;
import com.kyleruss.hssa2.client.communication.HTTPAsync;
import com.kyleruss.hssa2.client.communication.ServiceRequest;
import com.kyleruss.hssa2.client.core.ClientConfig;
import com.kyleruss.hssa2.client.core.KeyManager;
import com.kyleruss.hssa2.commons.CryptoUtils;
import com.kyleruss.hssa2.commons.EncryptedSession;
import com.kyleruss.hssa2.commons.RequestPaths;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.PublicKey;

import javax.crypto.Cipher;

import static com.kyleruss.hssa2.client.communication.CommUtils.parseJsonInput;

public class ConnectActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        getServerPublicKey();
    }

    public void getServerPublicKey()
    {
        String url              =   ClientConfig.CONN_URL + RequestPaths.SERV_PUBLIC_GET_REQ;
        ServiceRequest request  =   new ServiceRequest(url, true);
        ConnectTask servicer    =   new ConnectTask();
        servicer.execute(request);
    }

    private void startAuthCreateActivity()
    {
        Intent intent   =   new Intent(this, AuthCreateActivity.class);
        startActivity(intent);
    }

    private class ConnectTask extends HTTPAsync
    {
        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                JsonObject responseObj =    parseJsonInput(response);
                String keyStr           =   responseObj.get("serverPublicKey").getAsString();
                KeyManager.getInstance().setServerPublicKey(keyStr);
                startAuthCreateActivity();
            }

            catch(Exception e)
            {
                Log.d("SPUBLIC_FETCH_FAIL", e.getMessage());
            }
        }
    }
}
