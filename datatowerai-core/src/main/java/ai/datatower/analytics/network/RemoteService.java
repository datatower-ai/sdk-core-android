/*
 * Copyright (C) 2022 ThinkingData
 */

package ai.datatower.analytics.network;

import java.io.IOException;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

/**
 * RemoteService.
 * */
public interface RemoteService {
    String performRequest(String endpointUrl, String params,
                          boolean debug, SSLSocketFactory sslSocketFactory,
                          final Map<String, String> extraHeaders)
            throws IOException, DTHttpException;
}
