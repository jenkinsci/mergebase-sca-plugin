package com.mergebase.util;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShellExecutorImpl implements ShellExecutor {

    private static final String WINDOWS_SHELL = "cmd.exe";
    private static final String UNIX_SHELL = "sh";
    private static final String WINDOWS_OPTIONS = "/C";
    private static final String UNIX_OPTIONS = "-c";

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private static final String SHELL = IS_WINDOWS ? WINDOWS_SHELL : UNIX_SHELL;
    private static final String SHELL_OPTIONS = IS_WINDOWS ? WINDOWS_OPTIONS : UNIX_OPTIONS;

    private final boolean passThru;
    private String output = null;
    private String errorOutput = null;
    private final ProcessExecutor pe;

    public ShellExecutorImpl() {
        this(false);
    }

    public ShellExecutorImpl(boolean passThru) {
        this.passThru = passThru;
        pe = new ProcessExecutor();
    }

    @Override
    public List<String> getLines() {
        return output != null ? ShellExecutorImpl.intoLines(output) : null;
    }

    @Override
    public List<String> getErrorLines() {
        return errorOutput != null ? ShellExecutorImpl.intoLines(errorOutput) : null;
    }

    @Override
    public boolean isWindows() {
        return IS_WINDOWS;
    }

    @Override
    public int runProcess(
            Path path, String commandUnix, String commandWindows
    ) {
        return runProcessInner(path, commandUnix, commandWindows);
    }

    @Override
    public void runProcess2(Path path, String commandUnix, String commandWindows) throws ShellExecutionException {
        final int exitCode = runProcess(path, commandUnix, commandWindows);
        if (exitCode != 0) {
            final List<String> errorLines = getErrorLines();
            throw new ShellExecutionException(exitCode, errorLines != null ? errorLines : Collections.singletonList("exit code " + exitCode));
        }
    }

    private int runProcessInner(
            Path path, String commandUnix, String commandWindows
    ) {
        output = null;
        errorOutput = null;
        final String command = IS_WINDOWS && commandWindows != null ? commandWindows : commandUnix;
        final ByteArrayOutputStream errOut = new ByteArrayOutputStream();
        final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();

       // Logger.debug("Invoking: " + SHELL + " " + SHELL_OPTIONS + " " + command);

        if (path != null) {
            pe.directory(path.toFile());
        }
        pe.environment(System.getenv());
        pe.command(SHELL, SHELL_OPTIONS, command);
        pe.timeout(61, TimeUnit.MINUTES);
        pe.closeTimeout(60, TimeUnit.MINUTES);
        pe.readOutput(false);
        if (passThru) {
            pe.redirectError(System.err);
            pe.redirectOutput(System.out);
        } else {
            pe.redirectError(errOut);
            pe.redirectOutput(stdOut);
        }
        Integer exitValue = null;

        try {
            ProcessResult pr = pe.execute();
            exitValue = pr.getExitValue();

            if (!passThru) {
                this.output = stdOut.toString(StandardCharsets.UTF_8.name());
                this.errorOutput = errOut.toString(StandardCharsets.UTF_8.name());
//                if (Logger.debug()) {
//                    Logger.debug("-- [STDERR]=" + this.errorOutput);
//                    Logger.debug("-- [STDOUT]=" + this.output);
//                }
            }

            return exitValue;
        } catch (Exception e) {
            throw new RuntimeException("Shell invocation failed: " + e.getMessage(), e);
        } finally {
//            Logger.debug("Ran command (Exit-Code=" + exitValue + "): " + command);
        }
    }

    private static List<String> intoLines(final String... strings) {
        List<String> list = new ArrayList<>();
        if (strings != null) {
            for (final String s : strings) {
                if (s != null) {
                    StringReader sr = new StringReader(s);
                    BufferedReader br = new BufferedReader(sr);
                    String line;
                    try {
                        while ((line = br.readLine()) != null) {
                            list.add(line);
                        }
                    } catch (IOException ioe) {
                        throw new RuntimeException("impossible - StringReader does not throw IOException - " + ioe, ioe);
                    }
                }
            }
        }
        return list;
    }


}
