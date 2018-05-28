package com.maryanto.dimas.model;

import lombok.Data;

@Data
public class GitCommit {

    private String email;
    private String username;
    private String message;
    private String body;
}
