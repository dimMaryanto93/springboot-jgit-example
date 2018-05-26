package com.maryanto.dimas.config;

import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitRepositoryConfiguration {


    @Bean
    public FileRepositoryBuilder getFileRepository() {
        return new FileRepositoryBuilder();
    }

}
