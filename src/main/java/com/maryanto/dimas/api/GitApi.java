package com.maryanto.dimas.api;

import com.maryanto.dimas.config.GitProperties;
import com.maryanto.dimas.model.GitCommit;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/git")
public class GitApi {

    @Autowired
    private GitProperties properties;

    @PostMapping("/createProject/{projectName}")
    public ResponseEntity createProject(
            @PathVariable("projectName") String projectName,
            @RequestBody(required = false) GitCommit commitModel) {
        try {
            Git gitCommand = new InitCommand().setGitDir(properties.getBaseGitDir(projectName)).call();
            List<File> folders = properties.createFolder(projectName, Arrays.asList("SIT", "UIT"));
            for (File folder : folders) {
                if (!folder.exists()) {
                    boolean created = folder.mkdirs();
                    if (created) {
                        File gitkeep = new File(
                                new StringBuilder(folder.getCanonicalPath())
                                        .append(File.separator)
                                        .append(".gitkeep")
                                        .toString());
                        gitkeep.createNewFile();
                    }
                }
            }

            AddCommand addCommand = gitCommand.add().addFilepattern(".");
            addCommand.call();

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
            return new ResponseEntity(refObject, HttpStatus.CREATED);
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

    @GetMapping("/project/{projectName}")
    public ResponseEntity listDirectory(@PathVariable("projectName") String projectName) {
        return null;
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
