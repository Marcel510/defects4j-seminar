package com.github.marcel510.defects4jseminar;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TestwiseInformationAdapter {
    private static final String BASE_PATH = System.getProperty("SWQ_TEAMSCALEAGENT_BASEPATH", "http://localhost:8123");
    private final boolean enabled;

    private final CloseableHttpClient httpClient = HttpClients.createMinimal();

    public TestwiseInformationAdapter(PrintStream logStream) {
        String env = System.getenv("SWQ_TESTWISELISTENER_ENABLE");
        enabled = env != null && env.equals("true");
        logStream.println("TestwiseInformationAdapter ENABLED " + enabled);
    }

    public void end(final String test) {
        report(test, false);
    }

    public void start(final String test) {
        report(test, true);
    }

    private void report(final String test, final boolean start) {
        if (!enabled) {
            return;
        }

        String endpoint = start ? "/test/start/" : "/test/end/";
        String testEncoded;
        try {
            testEncoded = URLEncoder.encode(test, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        HttpPost httpPost = new HttpPost(BASE_PATH + endpoint + testEncoded);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() != 204) {
                System.exit(1);
                throw new IllegalStateException("Failure! " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
