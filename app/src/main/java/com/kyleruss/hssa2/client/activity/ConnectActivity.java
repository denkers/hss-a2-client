//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonObject;
import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.communication.HTTPAsync;
import com.kyleruss.hssa2.client.communication.ServiceRequest;
import com.kyleruss.hssa2.client.core.ClientConfig;
import com.kyleruss.hssa2.client.core.KeyManager;
import com.kyleruss.hssa2.commons.RequestPaths;

import static com.kyleruss.hssa2.client.communication.CommUtils.parseJsonInput;

public class ConnectActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        String url  =   ClientConfig.CONN_URL + RequestPaths.SERV_PUBLIC_GET_REQ;
        Log.d("CONNECT_URL", url);
        ServiceRequest request  =   new ServiceRequest(ClientConfig.CONN_URL + RequestPaths.SERV_PUBLIC_GET_REQ, true);
        ConnectServicer servicer = new ConnectServicer();
        servicer.execute(request);
    }

    private class ConnectServicer extends HTTPAsync
    {
        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                JsonObject responseObj = parseJsonInput(response);
                String keyStr = responseObj.get("serverPublicKey").getAsString();
                KeyManager.getInstance().setServerPublicKey(keyStr);
            }

            catch(Exception e)
            {
                Log.d("SPUBLIC_FETCH_FAIL", e.getMessage());
            }
        }
    }
}
