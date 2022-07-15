package com.github.marcel510.defects4jseminar;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestSuiteTestRunner {
    private static final Pattern TEST_PATTERN = Pattern.compile("^(.*)/([^/]+)$");


    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("test suite file not provided!");
        }

        final PrintStream logStream;
        try {
            logStream = new PrintStream(new FileOutputStream("testsuitetestrunner_log", true), true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        final List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(args[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final List<Test> tests = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = TEST_PATTERN.matcher(line);
            if (matcher.matches()) {
                final String className = matcher.group(1).replace("/", ".");
                final String methodName = matcher.group(2);

                tests.add(new Test(className, methodName, line));
            } else {
                throw new IllegalStateException("Unexpected line: " + line);
            }
        }
        logStream.println("Total tests " + tests.size());

//        final TestwiseInformationAdapter adapter = new TestwiseInformationAdapter(logStream);
        final JUnitCore jUnitCore = new JUnitCore();
        boolean successful = true;
        for (Test test : tests) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(test.getClassName());
            } catch(Exception e) {
                logStream.println("Couldn't load class (" + test.getClassName() + "): " + e.getMessage());
                System.exit(1);
            }
            final Request request = Request.method(clazz, test.getMethodName());

            logStream.print(test.getUniformPath());

//            adapter.start(test.getUniformPath());
            Result result = jUnitCore.run(request);
//            adapter.end(test.getUniformPath());


            if (!result.wasSuccessful()) {
                logStream.println(" FAILED");
                successful = false;
            } else {
                logStream.println();
            }
        }

        System.exit(successful ? 0 : 2);
    }
}
