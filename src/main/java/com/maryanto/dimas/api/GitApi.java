package com.maryanto.dimas.api;

import com.maryanto.dimas.config.GitProperties;
import com.maryanto.dimas.model.GitCommit;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/git")
public class GitApi {

    @Autowired
    private GitProperties properties;

    @PostMapping("/createProject/{projectName}")
    public ResponseEntity createProject(@PathVariable("projectName") String projectName) {
        try {
            Repository repository = properties.getRepository(projectName);
            repository.create(true);
            return new ResponseEntity(repository.getDirectory().getAbsolutePath(), HttpStatus.OK);
        } catch (java.lang.IllegalStateException ilste) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/project/{projectName}/{filename}")
    public ResponseEntity commitProject(
            @PathVariable("projectName") String projectName,
            @PathVariable("filename") String filename,
            @RequestBody GitCommit commit) {
        try {
            Repository repository = properties.getRepository(projectName);
            Git gitCommand = new Git(repository);

            String builder = new StringBuilder(properties.uriAbsoluteRepository(projectName))
                    .append(File.separator).append(filename).toString();
            System.out.println(builder);

            AddCommand addCommand = gitCommand.add();
            addCommand.addFilepattern(".").call();

            gitCommand.commit().setMessage(commit.getMessage()).call();
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (NoFilepatternException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.GONE);
        } catch (GitAPIException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.MULTI_STATUS);
        }
    }
}
