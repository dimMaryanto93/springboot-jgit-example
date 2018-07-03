package com.maryanto.dimas.api;

import com.maryanto.dimas.config.GitProperties;
import com.maryanto.dimas.model.GitCommit;
import com.maryanto.dimas.model.GitLog;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/git/projects")
public class GitApi {

    private final static Logger console = LoggerFactory.getLogger(GitApi.class);

    @Autowired
    private GitProperties properties;

    @RequestScope
    @PostMapping("/create/{projectName}")
    public ResponseEntity createProject(
            @PathVariable("projectName") String projectName,
            @RequestBody(required = false) GitCommit commitModel) {
        try {

            Repository repository = properties.getRepository(projectName);
            repository.create();
            Git gitCommand = new Git(repository);

            List<File> folders = properties.createFolder(projectName, Arrays.asList("SIT", "UIT"));

            gitCommand.add().addFilepattern(".").call();

            CommitCommand commitCommand = gitCommand.commit().setMessage("init project")
                    .setCommitter(commitModel.getUsername(), commitModel.getEmail());
            commitCommand.call();

            Iterable<RevCommit> listLog = gitCommand.log().setMaxCount(1).call();
            List<GitCommit> refObject = new ArrayList<>();
            for (RevCommit log : listLog) {
                GitCommit commit = new GitCommit();
                commit.setId(log.getId().getName());
                commit.setMessage(log.getFullMessage());
                commit.setUsername(log.getCommitterIdent().getName());
                commit.setEmail(log.getCommitterIdent().getEmailAddress());
                refObject.add(commit);
            }
            repository.close();
            return new ResponseEntity(refObject.get(0), HttpStatus.CREATED);
        } catch (java.lang.IllegalStateException ilste) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        } catch (GitAPIException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/directories/{projectName}")
    public ResponseEntity listDirectory(@PathVariable("projectName") String projectName) {
        List<String> directories = new ArrayList<>();
        String basePath = properties.getBaseDirectory(projectName);
        File directory = new File(basePath);
        File[] files = directory.listFiles();
        for (File dir : files) {
            if (!dir.getName().equalsIgnoreCase(".git") && dir.isDirectory())
                directories.add(dir.getName());
        }

        if (directories.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity(directories, HttpStatus.OK);
        }
    }

    @GetMapping("/logs/{projectName}")
    public ResponseEntity listLog(@PathVariable("projectName") String projectName) {
        List<GitLog> listLog = new ArrayList<>();
        try {
            Git gitCommand = new Git(properties.getRepository(projectName));
            Iterable<RevCommit> logs = gitCommand.log().call();
            for (RevCommit log : logs) {
                StringBuilder sb = new StringBuilder();

                console.info("{}", log.getFooterLines());
                listLog.add(new GitLog(
                        log.toObjectId().getName(),
                        log.getCommitterIdent().getName(),
                        log.getCommitterIdent().getEmailAddress(),
                        log.toString(),
                        log.getFullMessage()
                ));
            }

            return new ResponseEntity(listLog, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (NoHeadException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (GitAPIException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/diff/{projectName}/files/{fileName}")
    public ResponseEntity diffFileLastCommit(@PathVariable String projectName, @PathVariable String fileName) {
        try {
            Repository repository = properties.getRepository(projectName);
            ObjectReader objectReader = repository.newObjectReader();
            ObjectId idNow = repository.resolve("HEAD");
            ObjectId idOld = repository.resolve("HEAD^");

            CanonicalTreeParser newFile = new CanonicalTreeParser();
            newFile.reset(objectReader, idNow);

            CanonicalTreeParser oldFile = new CanonicalTreeParser();
            oldFile.reset(objectReader, idOld);

            Git gitCommand = new Git(repository);
            DiffCommand diffCommand = gitCommand.diff();
            List<DiffEntry> listDiff = diffCommand.setNewTree(newFile).setOldTree(oldFile).call();
            for (DiffEntry diff : listDiff) {
                console.info("informasi {}", diff);
            }
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (GitAPIException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
    }


}
