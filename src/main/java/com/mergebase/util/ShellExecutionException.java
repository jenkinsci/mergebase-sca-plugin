package com.mergebase.util;

import java.util.Collections;
import java.util.List;

public class ShellExecutionException extends Exception {

    private final int exitCode;
    private final List<String> errorLines;

    public ShellExecutionException(int exitCode, List<String> errorLines) {
        this.exitCode = exitCode;
        this.errorLines = errorLines;
    }

    public ShellExecutionException(int exitCode, String line) {
        this.exitCode = exitCode;
        this.errorLines = Collections.singletonList(line);
    }

    public int getExitCode() {
        return exitCode;
    }

    public List<String> getErrorLines() {
        return errorLines;
    }
}
