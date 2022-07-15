package com.github.marcel510.defects4jseminar;

import com.github.marcel510.defects4jseminar.template.ExternalAccountTemplate;
import com.github.marcel510.defects4jseminar.template.ProjectTemplate;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.file.Path;

public class TeamscaleProjectCreator {
    public void createProject(final Project project) {
        final String credentialsName = project.getTeamscaleProjectName() + "_git";
        createExternalAccount(credentialsName, project.getFixedDir());
        createProjectInternal(credentialsName, project.getTeamscaleProjectName());
    }

    private void createProjectInternal(final String credentialsName, final String projectName) {
        final String body = ProjectTemplate.getFilled(credentialsName, projectName);

        HttpUriRequest httpUriRequest = RequestBuilder.post(TeamscalePropertiesProvider.getBasepath() + "/api/projects")
                .setEntity(new StringEntity(body, ContentType.APPLICATION_JSON))
                .addHeader(TeamscalePropertiesProvider.getAuthHeader())
                .build();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(httpUriRequest);
            if (response.getStatusLine().getStatusCode() != 201) {
                throw new IllegalStateException("Request failed! " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createExternalAccount(final String credentialsName, final Path repositoryPath) {
        final String body = ExternalAccountTemplate.getFilled(credentialsName,
                repositoryPath.toAbsolutePath().toString());

        HttpUriRequest httpUriRequest = RequestBuilder.post(TeamscalePropertiesProvider.getBasepath() + "/api/external-accounts")
                .setEntity(new StringEntity(body, ContentType.APPLICATION_JSON))
                .addHeader(TeamscalePropertiesProvider.getAuthHeader())
                .build();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(httpUriRequest);
            if (response.getStatusLine().getStatusCode() != 204) {
                throw new IllegalStateException("Request failed! " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
