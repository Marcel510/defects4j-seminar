package com.github.marcel510.defects4jseminar.template;

public class ExternalAccountTemplate {
    private static final String TEMPLATE = "{\n" +
            "    \"credentialsName\": \"%CREDENTIALSNAME%\",\n" +
            "    \"uri\": \"%URI%\",\n" +
            "    \"username\": \"\",\n" +
            "    \"password\": \"\",\n" +
            "    \"connectorTypeInfo\": {\n" +
            "        \"connectorType\": \"SOURCE_CODE_REPOSITORY\",\n" +
            "        \"connectorEnum\": \"GIT\"\n" +
            "    }\n" +
            "}";

    public static String getFilled(final String credentialsName, final String uri) {
        final String uriSanitized = uri.replace("\\", "\\\\");
        return TEMPLATE.replace("%CREDENTIALSNAME%", credentialsName)
                .replace("%URI%", uriSanitized);
    }
}
