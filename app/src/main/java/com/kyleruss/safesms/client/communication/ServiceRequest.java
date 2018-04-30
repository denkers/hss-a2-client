//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.safesms.client.communication;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ServiceRequest
{
    //The connection URL
    private String url;

    //Params to be passed
    private Map<String, Object> params;

    //True if this is a GET request false if POST
    private boolean isGet;

    public ServiceRequest()
    {
        url     =   "";
        params  =   new HashMap();
        isGet   =   true;
    }

    public ServiceRequest(String url, boolean isGet)
    {
        this(url, new HashMap(), isGet);
    }

    public ServiceRequest(String url, Map<String, Object> params, boolean isGet)
    {
        this.url    =   url;
        this.params =   params;
        this.isGet  =   isGet;
    }

    public String getUrl()
    {
        return url;
    }

    public void setURL(String url)
    {
        this.url    =   url;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public void addParam(String name, Object value)
    {
        params.put(name, value);
    }

    public boolean isGet()
    {
        return isGet;
    }

    public void setGet(boolean isGet)
    {
        this.isGet  =   isGet;
    }

    //Generates the param string from the parameter map
    public String prepareParams()
    {
        if(params.size() == 0) return "";

        String enc  =   "";
        for(Object entryObj : params.entrySet())
        {
            Map.Entry<String, Object> entry =   (Map.Entry<String, Object>) entryObj;
            String name                     =   entry.getKey();
            Object value                    =   entry.getValue();

            enc += name + "=" + value + "&";
        }

        enc     =   enc.substring(0, enc.length() - 1);
        return enc;
    }

    //Creates a connection with either GET/POST methods
    //URL, params and method must be set before calling
    public HttpURLConnection getConnection() throws IOException
    {
        String preparedURL      =   isGet? (url + "?" + prepareParams()) : url;
        URL urlObj              =   new URL(preparedURL);
        HttpURLConnection conn  =   (HttpURLConnection) urlObj.openConnection();

        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        if(!isGet)
        {
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
        }

        return conn;
    }
}
