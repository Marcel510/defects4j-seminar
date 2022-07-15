package com.github.marcel510.defects4jseminar;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TeamscalePropertiesProvider {
    private static final String TEAMSCALE_BASEPATH = System.getProperty("SWQ_TEAMSCALE_BASEPATH",
            "http://localhost:8080");

    private static final String USER_NAME = "admin";
    private static final String ACCESS_TOKEN = "";

    public static String getAuth() {
        final String auth = USER_NAME + ":" + ACCESS_TOKEN;

        return "Basic " + Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    public static Header getAuthHeader() {
        return new BasicHeader("Authorization", getAuth());
    }

    public static String getBasepath() {
        return TEAMSCALE_BASEPATH;
    }
}
