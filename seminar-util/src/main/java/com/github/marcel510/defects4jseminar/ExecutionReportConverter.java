package com.github.marcel510.defects4jseminar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.marcel510.defects4jseminar.model.Test;
import com.github.marcel510.defects4jseminar.model.TestExecutionResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExecutionReportConverter {
    //"%s;%s;%s;%s;%s%n", className, methodName, durationFormatted, result, message
    private static final Pattern EXECUTION_REPORT_LINE_PATTERN =
            Pattern.compile("^([^;]*);([^;]*);([^;]*);([^;]*);([^;]*)$");

    private final ObjectMapper mapper;

    public ExecutionReportConverter() {
        this.mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void convertFile(final Path inputFilePath, final Path outputDirPath,
            final String d4jProjectName, final int id) {
        try {
            final List<String> testExecutionReport = Files.readAllLines(inputFilePath, StandardCharsets.UTF_8);
            final List<TestExecutionResult> allTestsExecuted = convert(testExecutionReport);
            final List<Test> allTests = allTestsExecuted.stream().map(TestExecutionResult::getTest)
                    .collect(Collectors.toList());

            final String allTestsExport = mapper.writeValueAsString(allTests);
            FileUtils.write(outputDirPath.resolve("test-list.json").toFile(), allTestsExport, StandardCharsets.UTF_8);
            final String allTestsExecutedExport = mapper.writeValueAsString(allTestsExecuted);
            FileUtils.write(outputDirPath.resolve("test-execution.json").toFile(), allTestsExecutedExport,
                    StandardCharsets.UTF_8);

            final List<String> infoLines = new ArrayList<>();
            infoLines.add(String.format("%s;%s;original;%s;%.3f", d4jProjectName, id, allTests.size(),
                    allTestsExecuted.stream().mapToDouble(TestExecutionResult::getDuration).sum()));
            FileUtils.writeLines(outputDirPath.resolve("info.csv").toFile(), infoLines);

            final List<String> allUniformPaths = allTests.stream().map(Test::getUniformPath)
                    .collect(Collectors.toList());
            FileUtils.writeLines(outputDirPath.resolve("testsuite-original.txt").toFile(), allUniformPaths);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<TestExecutionResult> convert(final List<String> lines) {
        final int maxSize = lines.size();
        final List<TestExecutionResult> testExecutionResults = new ArrayList<>(maxSize);

        int id = 0;
        for (final String line : lines) {
            if (line != null) {
                final Matcher matcher = EXECUTION_REPORT_LINE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    final String sourcePath = matcher.group(1).replace(".", "/");
                    final String uniformPath = sourcePath + "/" + matcher.group(2);
                    final Test test = new Test(id, matcher.group(1), matcher.group(2), uniformPath, sourcePath, null);

                    final String result = matcher.group(4);
                    final boolean passed = "PASSED".equals(result);
                    final double duration = Double.parseDouble(matcher.group(3));
                    String message;
                    try {
                        message = URLDecoder.decode(matcher.group(5), StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }

                    if (StringUtils.isBlank(message)) {
                        message = null;
                    }

                    final TestExecutionResult testExecutionResult = new TestExecutionResult(test,
                            passed, uniformPath, duration, result, message);

                    testExecutionResults.add(testExecutionResult);
                }
            }
        }
        return testExecutionResults;
    }
}
