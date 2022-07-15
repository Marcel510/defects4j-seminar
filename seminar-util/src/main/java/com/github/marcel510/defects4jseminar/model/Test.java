package com.github.marcel510.defects4jseminar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Test {
    @JsonIgnore
    private final int id;
    @JsonIgnore
    private final String testPath;
    @JsonIgnore
    private final String testName;
    private final String uniformPath;
    private final String sourcePath;
    private final String content;

    public Test(int id, String testPath, String testName, String uniformPath, String sourcePath, String content) {
        this.id = id;
        this.testPath = testPath;
        this.testName = testName;
        this.uniformPath = uniformPath;
        this.sourcePath = sourcePath;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getTestPath() {
        return testPath;
    }

    public String getTestName() {
        return testName;
    }

    public String getUniformPath() {
        return uniformPath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getContent() {
        return content;
    }
}
