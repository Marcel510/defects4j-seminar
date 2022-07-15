package com.github.marcel510.defects4jseminar;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.*;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Map;

public class ExecutionReportListener implements TestListener, JUnitResultFormatter {
    private static final double ONE_SECOND = 1e9;
    public static final int ROUND_UP_TO_NANO = 499999;
    private final PrintStream executionReportStream;
    private final Map<Test, Long> testStarts = new Hashtable<>();

    {
        try {
            this.executionReportStream = new PrintStream(new FileOutputStream("testExecutionReport.csv", true), true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

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
        exportWithStackTrace(test, "ERROR", e);
    }

    @Override
    public void addFailure(Test test, AssertionFailedError e) {
        exportWithStackTrace(test, "FAILURE", e);
    }

    public void testIgnored(Test test) {
        exportLine(test, "IGNORED", JUnitVersionHelper.getIgnoreMessage(test));
    }

    public void testAssumptionFailure(Test test, Throwable e) {
        exportWithStackTrace(test, "SKIPPED", e);
    }

    @Override
    public void endTest(Test test) {
        exportLine(test, "PASSED", "");
    }

    private void exportWithStackTrace(Test test, String result, Throwable t) {
        try {
            exportLine(test, result,
                    URLEncoder.encode(JUnitTestRunner.getFilteredTrace(t), StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void exportLine(Test test, String result, String message) {
        final long endTime = System.nanoTime();
        final double duration = (endTime - testStarts.get(test) + ROUND_UP_TO_NANO) / ONE_SECOND;
        final String durationFormatted = String.format("%.3f", duration);
        final String methodName = JUnitVersionHelper.getTestCaseName(test);
        final String className = JUnitVersionHelper.getTestCaseClassName(test);
        executionReportStream.printf("%s;%s;%s;%s;%s%n", className, methodName, durationFormatted, result, message);
    }

    @Override
    public void startTest(Test test) {
        testStarts.put(test, System.nanoTime());
    }
}
