//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.fragment;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.communication.CommUtils;
import com.kyleruss.hssa2.client.communication.HTTPAsync;
import com.kyleruss.hssa2.client.communication.ServiceRequest;
import com.kyleruss.hssa2.client.communication.ServiceResponse;
import com.kyleruss.hssa2.client.core.ClientConfig;
import com.kyleruss.hssa2.client.core.KeyManager;
import com.kyleruss.hssa2.client.core.RequestManager;
import com.kyleruss.hssa2.client.core.User;
import com.kyleruss.hssa2.client.core.UserManager;
import com.kyleruss.hssa2.commons.EncryptedSession;
import com.kyleruss.hssa2.commons.RequestPaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UsersFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener
{
    private UserListAdapter usersAdapter;
    private View view;

    public UsersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //getActivity().getActionBar().setTitle("Users");
        view   =   inflater.inflate(R.layout.fragment_users, container, false);


        view.findViewById(R.id.usersRefresh).setOnClickListener(this);
        List userList       =   new ArrayList<>(UserManager.getInstance().getUserList());
        usersAdapter        =   new UserListAdapter(userList, getActivity());
        ListView listView   =   (ListView) view.findViewById(R.id.userList);
        listView.setAdapter(usersAdapter);
        listView.setOnItemClickListener(this);

        fetchUserList();
        return view;
    }

    private void showSendSMSFragmentForUser(User user)
    {
        Fragment smsFragment    =   new SendSMSFragment();
        Bundle data             =   new Bundle();
        data.putString("phoneID", user.getPhoneID());
        smsFragment.setArguments(data);

        getFragmentManager()
        .beginTransaction()
        .replace(R.id.container, smsFragment)
        .addToBackStack(null)
        .commit();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        User user   =   UserManager.getInstance().getUserAt(position);
        showSendSMSFragmentForUser(user);
    }

    public void refreshUserList()
    {
        usersAdapter.setList(UserManager.getInstance().getUserList());
    }

    public void fetchUserList()
    {
        try
        {
            Map.Entry<String, String> authRequest = RequestManager.getInstance().generateRequest();
            JsonObject requestObj           =   CommUtils.prepareAuthenticatedRequest(authRequest.getKey(), authRequest.getValue());
            requestObj.addProperty("userID", UserManager.getInstance().getActiveUser().getPhoneID());
            EncryptedSession encSession     =   new EncryptedSession(requestObj.toString().getBytes("UTF-8"), KeyManager.getInstance().getServerPublicKey());
            ServiceRequest request          =   CommUtils.prepareEncryptedSessionRequest(encSession);
            request.setURL(ClientConfig.CONN_URL + RequestPaths.USER_LIST_REQ);
            request.setGet(false);

            UserListTask task           =   new UserListTask();
            task.execute(request);
        }

        catch(Exception e)
        {
            e.printStackTrace();
            new ServiceResponse("Failed to fetch user list", false).showToastResponse(getActivity());
            //Log.d("FETCH_USERS_FAIL", e.getMessage());
        }
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.usersRefresh)
            fetchUserList();

    }

    private class UserListTask extends HTTPAsync
    {
        protected void onPreExecute()
        {
            showServicingSpinner(getActivity(), "Retrieving online users");
        }

        @Override
        protected void onPostExecute(String response)
        {
            try
            {
                hideServicingSpinner();

                EncryptedSession encSession =   CommUtils.decryptSessionResponse(response, KeyManager.getInstance().getClientPrivateKey());
                JsonObject responseObj      =   CommUtils.parseJsonInput(new String(encSession.getData()));
                String requestID            =   responseObj.get("requestID").getAsString();
                String nonce                =   responseObj.get("nonce").getAsString();

                if(RequestManager.getInstance().verifyAndDestroy(requestID, nonce))
                {
                    JsonObject userListObj  =   responseObj.get("userList").getAsJsonObject();
                    JsonArray userList      =   userListObj.get("users").getAsJsonArray();
                    int userCount           =   userListObj.get("userCount").getAsInt();
                    UserManager userManager =   UserManager.getInstance();
                    userManager.clearUsers();

                    for(int i = 0; i < userCount; i++)
                    {
                        JsonObject userObj  =   userList.get(i).getAsJsonObject();
                        User user           =   new User(userObj.get("phoneID").getAsString(), userObj.get("name").getAsString());
                        user.setEmail(userObj.get("email").getAsString());

                        if(userObj.has("profileImage"))
                            user.setProfileImage(Base64.decode(userObj.get("profileImage").getAsString(), Base64.DEFAULT));

                        userManager.addUser(user.getPhoneID(), user);
                    }

                    refreshUserList();
                    Log.d("FETCH_USERS_RESPONSE", userListObj.toString());
                }

                else  new ServiceResponse("Failed to authenticate response", false).showToastResponse(getActivity());
                //Toast.makeText(getActivity().getApplicationContext(), "Failed to authenticate response", Toast.LENGTH_SHORT).show();
            }

            catch(Exception e)
            {
                new ServiceResponse("Failed to fetch user list", false).showToastResponse(getActivity());
                Log.d("USER_LIST_FETCH_FAIL", e.getMessage());
            }
        }
    }
}
