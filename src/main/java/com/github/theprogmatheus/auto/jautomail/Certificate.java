package com.github.theprogmatheus.auto.jautomail;

import lombok.Data;

import java.io.File;

@Data
public class Certificate {

    private final Participant owner;
    private final File file;
}
