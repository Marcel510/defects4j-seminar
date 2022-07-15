package com.github.marcel510.defects4jseminar;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AgentConfigGenerator {

    private static final String TEMPLATE = "mode=testwise\n" +
            "http-server-port=8123\n" +
            "includes=%INCLUDES%\n" +
            "excludes=*Test*%EXCLUDES%\n" +
            "out=%OUT%";
    private static final Map<String, String> PROJECT_INCLUDES_MAPPING = new HashMap<>();
    private static final Map<String, String> PROJECT_EXCLUDES_MAPPING = new HashMap<>();


    static {
        // Closure
        PROJECT_INCLUDES_MAPPING.put("Closure", "*com.google.javascript.*;*com.google.debugging.*");
        PROJECT_EXCLUDES_MAPPING.put("Closure", "*com.google.javascript.rhino.head.*;*com.google.javascript.rhino.testing.*;*com.google.javascript.jscomp.mozilla.rhino.*;*CGLIB*");

        // Gson
        PROJECT_INCLUDES_MAPPING.put("Gson", "*com.google.gson.*");

        // JacksonCore
        PROJECT_INCLUDES_MAPPING.put("JacksonCore", "*com.fasterxml.jackson.core.*");

        // JacksonDatabind
        PROJECT_INCLUDES_MAPPING.put("JacksonDatabind", "*com.fasterxml.jackson.databind.*");
        PROJECT_EXCLUDES_MAPPING.put("JacksonDatabind", "*CGLIB*");

        // JacksonXml
        PROJECT_INCLUDES_MAPPING.put("JacksonXml", "*com.fasterxml.jackson.dataformat.xml.*");

        // Jsoup
        PROJECT_INCLUDES_MAPPING.put("Jsoup", "*org.jsoup.*");

        // Mockito
        PROJECT_INCLUDES_MAPPING.put("Mockito", "*org.mockito.*");
        PROJECT_EXCLUDES_MAPPING.put("Mockito", "*CGLIB*;*org.mockito.cglib.*;*org.mockito.asm.*;");

        // Time
        PROJECT_INCLUDES_MAPPING.put("Time", "*org.joda.time.*");

        // Chart
        PROJECT_INCLUDES_MAPPING.put("Chart", "*org.jfree.chart.*");
    }
    public void generate(final Path outputFilePath, final Path coverageDirPath, final String project,
            final int bugId) {
        final String includes = PROJECT_INCLUDES_MAPPING.getOrDefault(project, "");
        final String excludes = PROJECT_EXCLUDES_MAPPING.get(project);
        final String out = coverageDirPath.toAbsolutePath().toString();
        final String filledTemplate = TEMPLATE.replace("%INCLUDES%", includes)
                .replace("%EXCLUDES%", excludes != null ? ";" + excludes : "")
                .replace("%OUT%", out);

        try {
            FileUtils.write(outputFilePath.toFile(), filledTemplate, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
