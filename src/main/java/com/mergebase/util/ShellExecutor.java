package com.mergebase.util;

import java.nio.file.Path;
import java.util.List;

public interface ShellExecutor {

    /**
     * Run a shell command for Unix or Windows
     * @param path the working directory
     * @param commandUnix the Unix command
     * @param commandWindows the Windows command. If null, use the Unix command
     * @return the exit code
     */
    int runProcess(Path path, String commandUnix, String commandWindows);

    /**
     * Same as runProcess but instead of returning the exit code, it throws a {@link ShellExecutionException}, which
     * allows the error to bubble up to a higher level.
     * @param path the working directory
     * @param commandUnix the Unix command
     * @param commandWindows the Windows command. If null, use the Unix command
     */
    void runProcess2(Path path, String commandUnix, String commandWindows) throws ShellExecutionException;

    /**
     *
     * @return the output lines from executing the command or null
     */
    List<String> getLines();

    /**
     *
     * @return the error output or null
     */
    List<String> getErrorLines();

    boolean isWindows();
}
