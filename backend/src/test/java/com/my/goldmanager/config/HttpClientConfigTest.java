package com.my.goldmanager.config;

import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;

import org.junit.jupiter.api.Test;

class HttpClientConfigTest {

    @Test
    void httpClientBuilder_providesBuilder() {
        HttpClientConfig config = new HttpClientConfig();
        HttpClient.Builder builder = config.httpClientBuilder();
        assertNotNull(builder);
        assertNotNull(builder.build());
    }
}

