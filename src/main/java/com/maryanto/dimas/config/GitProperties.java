package com.maryanto.dimas.config;

import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class GitProperties {

    @Autowired
    private GitRepositoryConfiguration git;

    public String uriRepository(String project) {
        return new StringBuilder(System.getProperty("user.home"))
                .append(File.separator).append(project)
                .append(File.separator).append(".git").toString();
    }

    public String uriAbsoluteRepository(String project) {
        return new StringBuilder(System.getProperty("user.home"))
                .append(File.separator).append(project).toString();
    }

    public Repository getRepository(String projectName) throws IOException {
        return git.getFileRepository().setGitDir(new File(uriRepository(projectName)))
                .readEnvironment()
                .findGitDir()
                .build();
    }
}
