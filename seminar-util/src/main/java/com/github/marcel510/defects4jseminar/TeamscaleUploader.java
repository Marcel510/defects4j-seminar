package com.github.marcel510.defects4jseminar;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TeamscaleUploader {
    private static final Pattern TESTWISE_COVERAGE_FILE_PATTERN = Pattern.compile("^testwise-coverage.*\\.json$");

    public void upload(final Project project) {
        final String commitTimestamp = getCommitTimestamp(project.getFixedDir());
        final String path = String.format("%s/p/%s/external-report", TeamscalePropertiesProvider.getBasepath(),
                project.getTeamscaleProjectName());

        final List<File> testwiseCoverageFiles;
        try (final Stream<Path> pathStream = Files.walk(project.getCoverageDir(), 1)){
            testwiseCoverageFiles = pathStream.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(t -> TESTWISE_COVERAGE_FILE_PATTERN.matcher(t.getName()).matches())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Testwise coverage files found: " + testwiseCoverageFiles.size());

        try (final CloseableHttpClient httpClient = HttpClients.createDefault()){
            final MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            for (File testwiseCoverageFile : testwiseCoverageFiles) {
                final String fileText = FileUtils.readFileToString(testwiseCoverageFile, StandardCharsets.UTF_8);
                entityBuilder.addTextBody("report", fileText);
            }
            final HttpUriRequest request = RequestBuilder.post(path)
                    .setEntity(entityBuilder.build())
                    .addHeader(TeamscalePropertiesProvider.getAuthHeader())
                    .addParameter("format", "TESTWISE_COVERAGE")
                    .addParameter("t", "default:" + commitTimestamp)
                    .addParameter("partition", "Unit Tests")
                    .addParameter("message", "Testwise coverage upload with filecount: " + testwiseCoverageFiles.size())
                    .build();

            CloseableHttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IllegalStateException("Upload failed! " + response.getStatusLine().getStatusCode());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCommitTimestamp(final Path sourceDir) {
        try {
            final Process process = new ProcessBuilder()
                    .directory(sourceDir.toFile())
                    .command("git", "--no-pager", "log", "-n1", "--format=%ct000")
                    .start();

            final String out;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                out = reader.lines().collect(Collectors.joining(System.lineSeparator())).trim();
            }

            if (process.waitFor() != 0) {
                throw new RuntimeException("Excited unsuccessfully");
            }
            return out;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
