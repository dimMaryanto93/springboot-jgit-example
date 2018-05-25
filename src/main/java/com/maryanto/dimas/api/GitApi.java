package com.maryanto.dimas.api;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/git")
public class GitApi {

    @PostMapping("/createProject/{projectName}")
    public ResponseEntity createProject(@PathVariable("projectName") String projectName) {
        String gitRepository = new StringBuilder(System.getProperty("user.home"))
                .append(File.separator).append(projectName)
                .append(File.separator).append(".git").toString();
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            Repository repository = builder.setGitDir(new File(gitRepository))
                    .readEnvironment()
                    .findGitDir()
                    .build();
            repository.create(true);
            return new ResponseEntity(repository.getDirectory().getAbsolutePath(), HttpStatus.OK);
        } catch (java.lang.IllegalStateException ilste) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }
}
