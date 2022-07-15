package com.github.marcel510.defects4jseminar;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "convertExecutionReport":
                if (args.length < 5) {
                    break;
                }
                System.out.printf("Converting execution report: %s, %s, %s, %s%n", args[1], args[2], args[3], args[4]);
                convertExecutionReport(args[1], args[2], args[3], args[4]);
                return;

            case "generateAgentConfig":
                if (args.length < 5) {
                    break;
                }
                System.out.printf("Generating agent config: %s, %s, %s, %s%n", args[1], args[2], args[3], args[4]);
                generateAgentConfig(args[1], args[2], args[3], args[4]);
                return;

            case "uploadToTeamscale":
                if (args.length < 4) {
                    break;
                }
                System.out.printf("Uploading to Teamscale: %s, %s, %s%n", args[1], args[2], args[3]);
                uploadToTeamscale(args[1], args[2], args[3]);
                return;

            case "createTeamscaleProject":
                if (args.length < 4) {
                    break;
                }
                System.out.printf("Creating teamscale project: %s, %s, %s%n", args[1], args[2], args[3]);
                createTeamscaleProject(args[1], args[2], args[3]);
                return;

            case "retrieveMinimizedTestSuites":
                if (args.length < 4) {
                    break;
                }
                System.out.printf("Retrieving minimized testsuites: %s, %s, %s%n", args[1], args[2], args[3]);
                retrieveMinimizedTestSuites(args[1], args[2], args[3]);
                return;

            case "createAndUpload":
                if (args.length < 4) {
                    break;
                }
                createAndUpload(args[1], args[2], args[3]);
                return;

            case "autoProcess":
                if (args.length < 4) {
                    break;
                }
                final TeamscaleAutoProcessor autoProcessor = new TeamscaleAutoProcessor();
                autoProcessor.process(args[1], args[2], args[3]);
                return;

            default:
                break;
        }

        printUsage();
    }

    private static Project getProject(final String workDir, final String d4jProjectName, final String id) {
        try {
            return new Project(workDir, d4jProjectName, id);
        } catch (InvalidPathException e) {
            System.out.printf("Invalid path: %s (%s)%n", e.getReason(), e.getInput());
            printUsage();
            System.exit(1);
        } catch (NumberFormatException e) {
            System.out.printf("Bug id must be a number '%s'%n", id);
            printUsage();
            System.exit(1);
        }
        return null;
    }

    private static void createAndUpload(final String workDir, final String d4jProjectName, final String id) {
        final Project project = getProject(workDir, d4jProjectName, id);
        TeamscaleProjectCreator teamscaleProjectCreator = new TeamscaleProjectCreator();
        System.out.printf("Creating teamscale project: %s, %s, %s%n", workDir, d4jProjectName, id);
        teamscaleProjectCreator.createProject(project);
        final TeamscaleUploader teamscaleUploader = new TeamscaleUploader();
        System.out.printf("Uploading to Teamscale: %s, %s, %s%n", workDir, d4jProjectName, id);
        teamscaleUploader.upload(project);
    }

    private static void retrieveMinimizedTestSuites(final String workDir, final String d4jProjectName, final String id) {
        final Project project = getProject(workDir, d4jProjectName, id);
        final TeamscaleMinimizedTestSuiteRetriever retriever = new TeamscaleMinimizedTestSuiteRetriever();
        retriever.retrieveAll(project);

    }

    private static void createTeamscaleProject(final String workDir, final String d4jProjectName, final String id) {
        final Project project = getProject(workDir, d4jProjectName, id);
        TeamscaleProjectCreator teamscaleProjectCreator = new TeamscaleProjectCreator();
        teamscaleProjectCreator.createProject(project);
    }

    private static void uploadToTeamscale(final String workDir, final String d4jProjectName, final String id) {
        final Project project = getProject(workDir, d4jProjectName, id);
        final TeamscaleUploader teamscaleUploader = new TeamscaleUploader();
        teamscaleUploader.upload(project);
    }

    private static void convertExecutionReport(final String inputFilePath, final String outputDirPath,
            final String d4jProjectName, final String bugId) {
        try {
            final Path inputFilePathResolved = Paths.get(inputFilePath);
            final Path outputDirPathResolved = Paths.get(outputDirPath);
            final int id = Integer.parseInt(bugId);
            final ExecutionReportConverter executionReportConverter = new ExecutionReportConverter();
            executionReportConverter.convertFile(inputFilePathResolved, outputDirPathResolved, d4jProjectName, id);
        } catch (InvalidPathException e) {
            System.out.printf("Invalid path: %s (%s)%n", e.getReason(), e.getInput());
            printUsage();
        } catch (NumberFormatException e) {
            System.out.printf("Bug id must be a number '%s'%n", bugId);
            printUsage();
        }
    }

    private static void generateAgentConfig(final String outputFilePath, final String coverageDirPath, final String project,
            final String bugId) {
        try {
            final Path outputFilePathResolved = Paths.get(outputFilePath);
            final Path coverageDirPathResolved = Paths.get(coverageDirPath);
            final int parsedBugId = Integer.parseInt(bugId);
            final AgentConfigGenerator agentConfigGenerator = new AgentConfigGenerator();
            agentConfigGenerator.generate(outputFilePathResolved, coverageDirPathResolved, project, parsedBugId);
        } catch (InvalidPathException e) {
            System.out.printf("Invalid path: %s (%s)%n", e.getReason(), e.getInput());
            printUsage();
        } catch (NumberFormatException e) {
            System.out.printf("Bug id must be a number '%s'%n", bugId);
            printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("\t<.> convertExecutionReport <input file path> <output dir path> <d4j project name> <d4j bug id>");
        System.out.println("\t<.> generateAgentConfig <output file path> <coverage dir path> <d4j project name> <d4j bug id>");
        System.out.println("\t<.> uploadToTeamscale <work dir path> <d4j project name> <d4j bug id>");
        System.out.println("\t<.> createTeamscaleProject <work dir path> <d4j project name> <d4j bug id>");
        System.out.println("\t<.> createAndUpload <work dir path> <d4j project name> <d4j bug id>");
        System.out.println("\t<.> retrieveMinimizedTestSuites <work dir path> <d4j project name> <d4j bug id>");
        System.out.println("\t<.> autoProcess <work dir path> <d4j project name> <d4j bug ids in format '1,2,3,4-10'>");
    }
}
