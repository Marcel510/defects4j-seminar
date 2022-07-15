package com.github.marcel510.defects4jseminar.template;

public class ProjectTemplate {
    private static final String TEMPLATE = "{\n" +
            "    \"name\": \"%PROJECTNAME%\",\n" +
            "    \"publicIds\": [\n" +
            "        \"%PROJECTNAME%\"\n" +
            "    ],\n" +
            "    \"profile\": \"Java (default)\",\n" +
            "    \"analysisGranularity\": {\n" +
            "        \"ageBoundaries\": [\n" +
            "            \"< 30 days ago\",\n" +
            "            \"< 90 days ago\"\n" +
            "        ],\n" +
            "        \"branchConfigurations\": [\n" +
            "            {\n" +
            "                \"branchNamePattern\": \"main|master|trunk|release.*\",\n" +
            "                \"periodicitySeconds\": [\n" +
            "                    0,\n" +
            "                    3600,\n" +
            "                    86400\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"branchNamePattern\": \".*\",\n" +
            "                \"periodicitySeconds\": [\n" +
            "                    0,\n" +
            "                    3600,\n" +
            "                    -1\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"connectors\": [\n" +
            "        {\n" +
            "            \"type\": \"Git\",\n" +
            "            \"connectorIdentifierOptionName\": \"Repository identifier\",\n" +
            "            \"options\": {\n" +
            "                \"Account\": \"%CREDENTIALSNAME%\",\n" +
            "                \"Path suffix\": \"\",\n" +
            "                \"Repository identifier\": \"repository1\",\n" +
            "                \"Included file names\": \"**.java, **.architecture\",\n" +
            "                \"Excluded file names\": \"**/package-info.java, **/module-info.java\",\n" +
            "                \"Important Branches\": \"\",\n" +
            "                \"Include Submodules\": \"false\",\n" +
            "                \"Submodule recursion depth\": \"10\",\n" +
            "                \"SSH Private Key ID\": \"\",\n" +
            "                \"Default branch name\": \"default\",\n" +
            "                \"Enable branch analysis\": \"false\",\n" +
            "                \"Included branches\": \".*\",\n" +
            "                \"Excluded branches\": \"_anon.*\",\n" +
            "                \"Start revision\": \"1 year ago\",\n" +
            "                \"Content exclude\": \"\",\n" +
            "                \"Polling interval\": \"60\",\n" +
            "                \"Test-code path pattern\": \"\",\n" +
            "                \"Test-code path exclude pattern\": \"\",\n" +
            "                \"Prepend repository identifier\": \"false\",\n" +
            "                \"End revision\": \"\",\n" +
            "                \"Text filter\": \"\",\n" +
            "                \"Language mapping\": \"\",\n" +
            "                \"Analysis report mapping\": \"\",\n" +
            "                \"Partition Pattern\": \"\",\n" +
            "                \"File-size exclude\": \"1MB\",\n" +
            "                \"Source library connector\": \"false\",\n" +
            "                \"Run to exhaustion\": \"false\",\n" +
            "                \"Preserve empty commits\": \"true\",\n" +
            "                \"Delta size\": \"500\",\n" +
            "                \"Path prefix transformation\": \"\",\n" +
            "                \"Path transformation\": \"\",\n" +
            "                \"Encoding\": \"\",\n" +
            "                \"Author transformation\": \"\",\n" +
            "                \"Branch transformation\": \"\"\n" +
            "            }\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    public static String getFilled(final String credentialsName, final String projectName) {
        return TEMPLATE.replace("%CREDENTIALSNAME%", credentialsName)
                .replace("%PROJECTNAME%", projectName);
    }
}
