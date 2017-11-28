package com.microsoft.rest.v2;

import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.URLConnectionHttpClient;

public class RestProxyWithURLConnectionTests extends RestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new URLConnectionHttpClient();
    }
}
