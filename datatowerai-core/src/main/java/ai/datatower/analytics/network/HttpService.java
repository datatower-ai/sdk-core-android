/*
 * Copyright (C) 2022 ThinkingData
 */

package ai.datatower.analytics.network;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import ai.datatower.analytics.utils.LogUtils;

/**
 * HttpService发送数据.
 */
public class HttpService implements RemoteService {

    @Override
    public String performRequest(String endpointUrl, String params,
                                 boolean debug, SSLSocketFactory socketFactory,
                                 Map<String, String> extraHeaders)
            throws DTHttpException, IOException {
        InputStream in = null;
        OutputStream out = null;
        BufferedOutputStream bout = null;
        BufferedReader br = null;
        HttpURLConnection connection = null;


        try {
            final URL url = new URL(endpointUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15 * 1000);
            connection.setReadTimeout(15 * 1000);
            if (null != socketFactory && connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
            }

            if (null != params) {
                String query;

                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                if (debug) {
                    query = params;
                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");
                    connection.setUseCaches(false);
                    connection.setRequestProperty("charset", "utf-8");
                } else {
                    connection.setRequestProperty("Content-Type", "text/plain");
                    try {
                        query = encodeData(params);
                    } catch (IOException e) {
                        throw new InvalidParameterException(e.getMessage());
                    }
                }
                if (extraHeaders != null) {
                    for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }

                connection.setFixedLengthStreamingMode(query.getBytes(StandardCharsets.UTF_8).length);
                out = connection.getOutputStream();
                bout = new BufferedOutputStream(out);
                bout.write(query.getBytes(StandardCharsets.UTF_8));

                bout.flush();
                bout.close();
                bout = null;
                out.close();
                out = null;

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    in = connection.getInputStream();
                    br = new BufferedReader(new InputStreamReader(in));
                    StringBuilder buffer = new StringBuilder();
                    String str;
                    while ((str = br.readLine()) != null) {
                        buffer.append(str);
                    }
                    in.close();
                    br.close();
                    return buffer.toString();
                } else {
                    try {
                        in = connection.getErrorStream();
                        br = new BufferedReader(new InputStreamReader(in));
                        StringBuilder buffer = new StringBuilder();
                        String str;
                        while ((str = br.readLine()) != null) {
                            buffer.append(str);
                        }
                        in.close();
                        br.close();
                        throw new RemoteVerificationException(responseCode, buffer.toString());
                    } catch (Throwable t) {
                        if (t instanceof RemoteVerificationException) throw t;
                        LogUtils.e("DT Http", "Failed to get input stream: " + t.getMessage());
                        throw new ServiceUnavailableException(
                                "Service unavailable with response code: " + responseCode);
                    }
                }
            } else {
                throw new InvalidParameterException("Content is null");
            }

        } finally {
            if (null != bout) {
                try {
                    bout.close();
                } catch (final IOException e) {
                    //ignored
                }
            }

            if (null != out) {
                try {
                    out.close();
                } catch (final IOException e) {
                    //ignored
                }
            }
            if (null != in) {
                try {
                    in.close();
                } catch (final IOException ignored) {
                    //ignored
                }
            }
            if (null != br) {
                try {
                    br.close();
                } catch (final IOException ignored) {
                    //ignored
                }
            }
            if (null != connection) {
                connection.disconnect();
            }
        }
    }

    private String encodeData(final String rawMessage) throws IOException {
//        ByteArrayOutputStream os = new ByteArrayOutputStream(rawMessage.getBytes().length);
//        GZIPOutputStream gos = new GZIPOutputStream(os);
//        gos.write(rawMessage.getBytes());
//        gos.close();
//        byte[] compressed = os.toByteArray();
//        os.close();
        return rawMessage;
    }
}
