package com.github.marcel510.defects4jseminar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TeamscaleMinimizedTestSuiteRetriever {
    private static final String[] PRIORITIZATION_STRATEGIES = {"GREEDY", "HGS"};
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void retrieveAll(final Project project) {
        for (String prioritizationStrategy : PRIORITIZATION_STRATEGIES) {
            final String testSuiteRaw = getMinimizedTestSuite(project.getTeamscaleProjectName(), prioritizationStrategy);
            final JsonNode testSuiteJson;
            try {
                testSuiteJson = objectMapper.readTree(testSuiteRaw);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            List<String> uniformPathsOfSelectedTests = new ArrayList<>();
            testSuiteJson.withArray("testCluster").elements().forEachRemaining(cluster ->
                    cluster.withArray("tests").elements().forEachRemaining(test ->
                            uniformPathsOfSelectedTests.add(test.get("uniformPath").asText())));
            List<Integer> durations = new ArrayList<>();
            testSuiteJson.withArray("testCluster").elements().forEachRemaining(cluster ->
                    cluster.withArray("tests").elements().forEachRemaining(test ->
                            durations.add(test.get("durationInMs").asInt())));

            System.out.printf("Retrieved minimized test suite (%s) with %s selected tests%n", prioritizationStrategy,
                    uniformPathsOfSelectedTests.size());
            final String fileName = "testsuite-" + prioritizationStrategy.toLowerCase() + ".txt";
            final String fileNameJson = "testsuite-raw-" + prioritizationStrategy.toLowerCase() + ".json";

            final List<String> infoLines = new ArrayList<>();
            //project_name;id;name;test_amount;duration
            infoLines.add(String.format("%s;%s;%s;%s;%.3f", project.getD4jProjectName(), project.getId(),
                    prioritizationStrategy.toLowerCase(),
                    uniformPathsOfSelectedTests.size(),
                    durations.stream().mapToInt(Integer::intValue).sum() / 1000d));
            try {
                FileUtils.writeLines(project.getCoverageDir().resolve(fileName).toFile(), StandardCharsets.UTF_8.toString(),
                        uniformPathsOfSelectedTests);
                FileUtils.write(project.getCoverageDir().resolve(fileNameJson).toFile(), testSuiteRaw, StandardCharsets.UTF_8);
                FileUtils.writeLines(project.getCoverageDir().resolve("info.csv").toFile(), infoLines, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getMinimizedTestSuite(final String projectName, final String prioritizationStrategy) {
        final String path = String.format("%s/api/projects/%s/minimized-tests",
                TeamscalePropertiesProvider.getBasepath(), projectName);
        final HttpUriRequest request = RequestBuilder.get(path)
                .addHeader(TeamscalePropertiesProvider.getAuthHeader())
                .addParameter("end", "HEAD")
                .addParameter("partitions", "Unit Tests")
                .addParameter("prioritization-strategy", prioritizationStrategy)
                .addParameter("ensure-processed", "false")
                .addParameter("include-non-impacted", "true")
                .addParameter("include-failed-and-skipped", "true")
                .addParameter("include-added-tests", "true")
                .addParameter("max-exec-time", Integer.toString(Integer.MAX_VALUE)) // No time restricted minimization
                .addParameter("clustering-regex", "") // Minimize per test
                .build();

        try {
            CloseableHttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IllegalStateException("Request failed! " + response.getStatusLine().getStatusCode());
            }

            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
