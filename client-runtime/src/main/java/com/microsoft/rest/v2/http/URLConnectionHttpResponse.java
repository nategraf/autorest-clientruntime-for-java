package com.microsoft.rest.v2.http;

import rx.Observable;
import rx.Single;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class URLConnectionHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final byte[] body;

    public URLConnectionHttpResponse(int statusCode, HttpHeaders headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String headerValue(String headerName) {
        return headers.value(headerName);
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public Single<InputStream> bodyAsInputStreamAsync() {
        return Single.<InputStream>just(new ByteArrayInputStream(body));
    }

    @Override
    public Single<byte[]> bodyAsByteArrayAsync() {
        return Single.just(body);
    }

    @Override
    public Observable<byte[]> streamBodyAsync() {
        return Observable.just(body);
    }

    @Override
    public Single<String> bodyAsStringAsync() {
        return Single.just(new String(body));
    }
}
