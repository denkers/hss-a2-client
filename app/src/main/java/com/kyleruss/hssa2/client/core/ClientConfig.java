//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

public class ClientConfig
{
    public static final int CONN_PORT               =   34918;

    public static final String CONTEXT_PATH         =   "/hssa2/live";

    public static final String CONN_ADDR_LIVE       =   "http://122.62.25.62:";

    public static final String CONN_ADDR_LOCAL      =   "http://192.168.1.68:";

    public static final String CONN_URL             =   CONN_ADDR_LOCAL + CONN_PORT + CONTEXT_PATH;

    public static final String APP_NAME             =   "Safe SMS";

    public static final String STORED_PRKEY_NAME    =   "safesms_private_key";

    public static final String STORED_PUKEY_NAME    =   "safesms_public_key";
}
