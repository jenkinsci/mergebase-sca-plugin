package com.mergebase.jenkins;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MergeBaseRun {
    public static String scanProject(
            GenericRunContext context,
            MergebaseConfig mergebaseConfig,
            Path mbJarLocation
    ) {
        PrintStream logger = context.getTaskListener().getLogger();
        FilePath workspace = context.getWorkspace();
        Launcher launcher = context.getLauncher();
        EnvVars envVars = context.getEnvVars();

        final List<String> args = buildArgs(mergebaseConfig, mbJarLocation);
        int exitCode;
        try {
            logger.println("Testing project...");
            logger.println("> " + args.stream().collect(Collectors.joining(" ")));
            exitCode = launcher.launch()
                    .cmds(args)
                    //.envs(commandEnvVars)
                    .stdout(logger)
                    .stderr(logger)
                    .quiet(true)
                    .pwd(workspace)
                    .join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static List<String> buildArgs(final MergebaseConfig mergebaseConfig, Path mergebaseJarPath) {
        List<String> args = new ArrayList<>();
        args.add("java");
        args.add("-Dmb.customer=" + mergebaseConfig.getCustomerToken());
        args.add("-Dmb.url=" + mergebaseConfig.getDomain());
        args.add("-jar");
        args.add(mergebaseJarPath.toAbsolutePath().toString());
        if(mergebaseConfig.isEnableDebugMode()) {
            args.add("--debug");
        }
        if(mergebaseConfig.isEnableScanAll()) {
            args.add("--all");
        }

        if(mergebaseConfig.isEnableJsonOutput()) {
            args.add("--json");
        }

        if(!mergebaseConfig.isKillBuild()) {
            args.add("--exitZero");
        }
        args.add("--name=" + mergebaseConfig.getProjectName());
        args.add(mergebaseConfig.getScanPath());
        return args;
    }
}
