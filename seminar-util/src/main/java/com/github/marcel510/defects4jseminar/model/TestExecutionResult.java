package com.github.marcel510.defects4jseminar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TestExecutionResult {
    @JsonIgnore
    private final Test test;
    @JsonIgnore
    private final boolean passed;
    private final String uniformPath;
    private final double duration;
    private final String result;
    private final String message;

    public TestExecutionResult(Test test, boolean passed, String uniformPath, double duration, String result,
            String message) {
        this.test = test;
        this.passed = passed;
        this.uniformPath = uniformPath;
        this.duration = duration;
        this.result = result;
        this.message = message;
    }

    public Test getTest() {
        return test;
    }

    public String getUniformPath() {
        return uniformPath;
    }

    public double getDuration() {
        return duration;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPassed() {
        return passed;
    }

    @Override
    public String toString() {
        return "TestExecutionResult{" +
                "id=" + test.getId() +
                ", passed=" + passed +
                ", uniformPath='" + uniformPath + '\'' +
                ", duration=" + duration +
                ", result='" + result + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
