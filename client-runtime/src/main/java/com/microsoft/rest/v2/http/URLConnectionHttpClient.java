package com.microsoft.rest.v2.http;

import rx.Single;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class URLConnectionHttpClient extends HttpClient {
    private final Proxy proxy;

    public URLConnectionHttpClient() {
        this(null);
    }

    public URLConnectionHttpClient(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public Single<HttpResponse> sendRequestAsync(HttpRequest request) {
        Single<HttpResponse> result;

        try {
            final URL requestUrl = new URL(request.url());
            final HttpURLConnection connection = (HttpURLConnection)(proxy == null ? requestUrl.openConnection() : requestUrl.openConnection(proxy));

            final String httpMethod = request.httpMethod();
            if (httpMethod.equalsIgnoreCase("PATCH")) {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            }
            else {
                connection.setRequestMethod(httpMethod);
            }

            for (final HttpHeader header : request.headers()) {
                connection.setRequestProperty(header.name(), header.value());
            }

            final HttpRequestBody requestBody = request.body();
            if (requestBody != null) {
                connection.setDoOutput(true);
                try (final InputStream requestBodyInputStream = requestBody.createInputStream();
                     final OutputStream requestBodyOutputStream = connection.getOutputStream()) {
                    readAll(requestBodyInputStream, requestBodyOutputStream);
                }
            }

            final int statusCode = connection.getResponseCode();

            final HttpHeaders responseHeaders = new HttpHeaders();
            final Map<String,List<String>> responseHeaderFields = connection.getHeaderFields();
            for (final Map.Entry<String,List<String>> responseHeader : responseHeaderFields.entrySet()) {
                final String headerName = responseHeader.getKey();
                if (headerName != null) {
                    final List<String> headerValues = responseHeader.getValue();
                    String headerValue = "";
                    for (final String value : headerValues) {
                        if (!headerValue.isEmpty()) {
                            headerValue += ",";
                        }
                        headerValue += value;
                    }
                    responseHeaders.set(headerName, headerValue);
                }
            }

            byte[] responseBody;
            try (final InputStream responseBodyStream = (200 <= statusCode && statusCode < 300 ? connection.getInputStream() : connection.getErrorStream())) {
                final ByteArrayOutputStream bufferOutputStream = new ByteArrayOutputStream();
                readAll(responseBodyStream, bufferOutputStream);
                bufferOutputStream.flush();
                responseBody = bufferOutputStream.toByteArray();
            }

            result = Single.<HttpResponse>just(new URLConnectionHttpResponse(statusCode, responseHeaders, responseBody));
        } catch (IOException e) {
            result = Single.error(e);
        }

        return result;
    }

    private static void readAll(InputStream inputStream, OutputStream outputStream) throws IOException {
        if (inputStream != null && outputStream != null) {
            final byte[] bufferArray = new byte[2048];
            int bytesRead = inputStream.read(bufferArray);
            while (bytesRead > 0) {
                outputStream.write(bufferArray, 0, bytesRead);
                bytesRead = inputStream.read(bufferArray);
            }
        }
    }

    public static class Factory implements HttpClient.Factory {
        @Override
        public HttpClient create(Configuration configuration) {
            return new URLConnectionHttpClient(configuration == null ? null : configuration.proxy());
        }
    }
}
