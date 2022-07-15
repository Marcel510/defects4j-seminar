package com.github.marcel510.defects4jseminar;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Project {
    private final Path workDir;
    private final Path coverageDir;
    private final Path fixedDir;
    private final String d4jProjectName;
    private final int id;

    public Project(final String workDir, final String d4jProjectName, final int id) {
        this.workDir = Paths.get(workDir);
        this.id = id;
        this.coverageDir = getCoverageDir(this.workDir, d4jProjectName, this.id);
        this.fixedDir = getFixedDir(this.workDir, d4jProjectName, this.id);
        this.d4jProjectName = d4jProjectName;
    }

    public Project(final String workDir, final String d4jProjectName, final String id) {
        this(workDir, d4jProjectName, Integer.parseInt(id));
    }

    private static Path getCoverageDir(Path workDir, String d4jProjectName, int id) {
        return workDir.resolve(d4jProjectName + "_" + id + "_coverage");
    }

    private static Path getFixedDir(Path workDir, String d4jProjectName, int id) {
        return workDir.resolve(d4jProjectName + "_" + id + "_f");
    }

    public String getTeamscaleProjectName() {
        return d4jProjectName.toLowerCase() + "_" + id;
    }

    public Path getWorkDir() {
        return workDir;
    }

    public Path getCoverageDir() {
        return coverageDir;
    }

    public Path getFixedDir() {
        return fixedDir;
    }

    public String getD4jProjectName() {
        return d4jProjectName;
    }

    public int getId() {
        return id;
    }
}
