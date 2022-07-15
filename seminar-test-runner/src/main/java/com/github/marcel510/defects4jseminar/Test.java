package com.github.marcel510.defects4jseminar;

public class Test {
    private final String className;
    private final String methodName;
    private final String uniformPath;

    public Test(String className, String methodName, String uniformPath) {
        this.className = className;
        this.methodName = methodName;
        this.uniformPath = uniformPath;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getUniformPath() {
        return uniformPath;
    }

}
