package com.github.marcel510.defects4jseminar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.IntStream;

public class TeamscaleAutoProcessor {
    private static final int DELAY_IN_MS = 30000;

    private final TeamscaleProjectCreator projectCreator = new TeamscaleProjectCreator();
    private final TeamscaleUploader uploader = new TeamscaleUploader();
    private final TeamscaleMinimizedTestSuiteRetriever retriever = new TeamscaleMinimizedTestSuiteRetriever();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void process(final String workDir, final String d4jProjectName, final String ids) {
        List<Integer> idList = parseIdsToList(ids);
        Queue<Integer> createCandidates = new ArrayBlockingQueue<>(idList.size(), false, idList);
        Queue<Project> uploadCandidates = new ArrayBlockingQueue<>(idList.size());
        Queue<Project> minimizationCandidates = new ArrayBlockingQueue<>(idList.size());
        Queue<Project> deletionCandidates = new ArrayBlockingQueue<>(idList.size());

        long prevRunTimestamp = 0;
        while (!createCandidates.isEmpty() || !minimizationCandidates.isEmpty() || !uploadCandidates.isEmpty()
                || !deletionCandidates.isEmpty()) {

            long diff = System.currentTimeMillis() - prevRunTimestamp;
            if (diff < DELAY_IN_MS) {
                try {
                    long timeout = DELAY_IN_MS - diff;
                    System.out.printf("Waiting %s ms%n", timeout);
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            prevRunTimestamp = System.currentTimeMillis();

            Project newlyAdded = null;

            if (!createCandidates.isEmpty() && isCapacityAvailable()) {
                int id = createCandidates.remove();
                try {
                    Project candidate = new Project(workDir, d4jProjectName, id);
                    System.out.printf("Creating %s...%n", candidate.getTeamscaleProjectName());
                    projectCreator.createProject(candidate);
                    uploadCandidates.add(candidate);
                    System.out.printf("Done: Creating %s%n", candidate.getTeamscaleProjectName());
                } catch (RuntimeException e) {
                    createCandidates.add(id);
                }
            } else {
                System.out.println("No create candidates");
            }

            if (!uploadCandidates.isEmpty()) {
                Project candidate = uploadCandidates.remove();
                try {
                    System.out.printf("Uploading %s...%n", candidate.getTeamscaleProjectName());
                    uploader.upload(candidate);
                    newlyAdded = candidate;
                    System.out.printf("Done: Uploading %s%n", candidate.getTeamscaleProjectName());
                } catch (Exception e) {
                    uploadCandidates.add(candidate);
                }
            } else {
                System.out.println("No upload candidates");
            }

            if (!minimizationCandidates.isEmpty()) {
                Project candidate = minimizationCandidates.remove();
                boolean readyToMinimize;
                try {
                    readyToMinimize = isReadyToMinimize(candidate);
                } catch (Exception e) {
                    readyToMinimize = false;
                }
                if (readyToMinimize) {
                    try {
                        System.out.printf("Retrieving %s...%n", candidate.getTeamscaleProjectName());
                        retriever.retrieveAll(candidate);
                        minimizationCandidates.remove(candidate);
                        deletionCandidates.add(candidate);
                        System.out.printf("Done: Retrieving %s%n", candidate.getTeamscaleProjectName());
                    } catch (Exception ignored) {
                        minimizationCandidates.add(candidate);
                    }
                } else {
                    minimizationCandidates.add(candidate);
                }
            } else {
                System.out.println("No minimization candidates");
            }

            if (!deletionCandidates.isEmpty()) {
                Project candidate = deletionCandidates.remove();
                try {
                    deleteProject(candidate);
                } catch (Exception e) {
                    deletionCandidates.add(candidate);
                }
            } else {
                System.out.println("No deletion candidates");
            }

            if (newlyAdded != null) {
                minimizationCandidates.add(newlyAdded);
            }
        }
    }

    private List<Integer> parseIdsToList(final String idsRaw) {
        final List<Integer> result = new ArrayList<>(40);
        for (String id : idsRaw.split(",")) {
            id = id.trim();
            if (id.contains("-")) {
                String[] parts = id.split("-");
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);

                if (start >= end) {
                    throw new IllegalStateException("Range start greater than end " + start + " > " + end);
                }

                IntStream.rangeClosed(start, end).forEach(result::add);
            } else {
                result.add(Integer.parseInt(id));
            }
        }
        return result;
    }

    private boolean isReadyToMinimize(Project project) {
        System.out.printf("Checking if ready to minimize %s...%n", project.getTeamscaleProjectName());
        final String url = String.format("%s/api/projects/%s/metrics/table?uniform-path=&limit-to-profile=false" +
                        "&metrics=Line+Coverage&configuration-name=Teamscale+Default&all-partitions=true",
                TeamscalePropertiesProvider.getBasepath(), project.getTeamscaleProjectName());
        final HttpUriRequest request = RequestBuilder.get(url)
//                .addParameter("uniform-path", "")
//                .addParameter("limit-to-profile", "false")
//                .addParameter("metrics", "Line+Coverage")
//                .addParameter("configuration-name", "Teamscale+Default")
//                .addParameter("all-partitions", "true")
                .build();

        try {
            CloseableHttpResponse response = httpClient.execute(request);
            boolean result;
            if (response.getStatusLine().getStatusCode() != 200) {
                result = false;
            } else {
                try {
                    JsonNode jsonNode = objectMapper.readTree(EntityUtils.toString(response.getEntity(),
                            StandardCharsets.UTF_8));

                    double lineCoverage = jsonNode.get(0).get("metrics").get(0).get("value").asDouble(1.0D);
                    result = lineCoverage != 0.0D;
                } catch (JsonProcessingException | NullPointerException e) {
                    result = false;
                }

            }
            System.out.printf("Ready to minimize: %s%n", result);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isCapacityAvailable() {
        System.out.printf("Checking if capacity available...%n");
        final String url = String.format("%s/api/execution-status/workers", TeamscalePropertiesProvider.getBasepath());
        final HttpUriRequest request = RequestBuilder.get(url).build();

        try {
            CloseableHttpResponse response = httpClient.execute(request);
            boolean result;
            if (response.getStatusLine().getStatusCode() != 200) {
                result = false;
            } else {
                try {
                    JsonNode jsonNode = objectMapper.readTree(EntityUtils.toString(response.getEntity(),
                            StandardCharsets.UTF_8));

                    double utilization = jsonNode.get(0).get("utilization").asDouble(1.0D);
                    int jobQueueSize = jsonNode.get(0).get("jobQueueSize").asInt(100);

                    result = utilization <= 0.9 || jobQueueSize <= 80;
                } catch (JsonProcessingException | NullPointerException e) {
                    result = false;
                }

            }
            System.out.printf("Capacity available: %s%n", result);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteProject(Project project) {
        System.out.printf("Deleting %s%n", project.getTeamscaleProjectName());

        final String url = String.format("%s/api/projects/%s", TeamscalePropertiesProvider.getBasepath(),
                project.getTeamscaleProjectName());
        final HttpUriRequest request = RequestBuilder.delete(url).build();
        try {
            CloseableHttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != 204 && response.getStatusLine().getStatusCode() != 404) {
                throw new IllegalStateException("unable to delete");
            }

            System.out.printf("Deleted %s%n", project.getTeamscaleProjectName());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
