package com.github.marcel510.defects4jseminar;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitVersionHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TestwiseInformationListener implements TestListener, JUnitResultFormatter {
    private static final String BASE_PATH = System.getProperty("SWQ_TEAMSCALEAGENT_BASEPATH", "http://localhost:8123");
    private final CloseableHttpClient httpClient = HttpClients.createMinimal();
    ;

    @Override
    public void startTestSuite(JUnitTest suite) throws BuildException {

    }

    @Override
    public void endTestSuite(JUnitTest suite) throws BuildException {
    }

    @Override
    public void setOutput(OutputStream out) {

    }

    @Override
    public void setSystemOutput(String out) {

    }

    @Override
    public void setSystemError(String err) {

    }

    @Override
    public void addError(Test test, Throwable e) {
        end(test);
    }

    @Override
    public void addFailure(Test test, AssertionFailedError e) {
        end(test);
    }

    @Override
    public void endTest(Test test) {
        end(test);
    }

    @Override
    public void startTest(Test test) {
        report(test, true);
    }

    private void end(final Test test) {
        report(test, false);
    }

    private void report(final Test test, final boolean start) {
        String endpoint = start ? "/test/start/" : "/test/end/";
        HttpPost httpPost = new HttpPost(BASE_PATH + endpoint + getUniformPath(test));
        try {
            httpClient.execute(httpPost);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUniformPath(final Test test) {
        try {
            return URLEncoder.encode(JUnitVersionHelper.getTestCaseClassName(test).replace(".", "/") + "/" +
                    JUnitVersionHelper.getTestCaseName(test), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
