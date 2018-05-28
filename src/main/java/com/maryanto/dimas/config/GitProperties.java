package com.maryanto.dimas.config;

import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitProperties {

    @Autowired
    private GitRepositoryConfiguration git;

    private String uriRepository(String project) {
        return new StringBuilder(getBaseDirectory(project))
                .append(File.separator).append(".git").toString();
    }

    public String getBaseDirectory(String projectName) {
        return new StringBuilder(System.getProperty("user.home"))
                .append(File.separator).append("projects")
                .append(File.separator).append(projectName)
                .toString();
    }

    public File getBaseGitDir(String projectName) {
        return new File(uriRepository(projectName));
    }

    public List<File> createFolder(String projectName, List<String> folderNames) {
        List<File> gitkeep = new ArrayList<>();
        for (String folder : folderNames) {
            File file = new File(
                    new StringBuilder(getBaseDirectory(projectName))
                            .append(File.separator)
                            .append(folder)
                            .append(File.separator)
                            .toString()
            );
            gitkeep.add(file);
        }
        return gitkeep;
    }


    public String uriAbsoluteRepository(String project) {
        return new StringBuilder(System.getProperty("user.home"))
                .append(File.separator).append(project).toString();
    }

    public Repository getRepository(String projectName) throws IOException {
        return git.getFileRepository()
                .setGitDir(getBaseGitDir(projectName))
                .readEnvironment()
                .findGitDir()
                .build();
    }
}
