package com.mergebase.jenkins;

import com.mergebase.jenkins.downloader.ToolDownloader;
import com.mergebase.jenkins.execption.MergebaseException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import org.springframework.security.core.parameters.P;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MergeBaseRun {
    public static String scanProject(
            GenericRunContext context,
            MergebaseConfig mergebaseConfig
    ) throws MergebaseException, IOException {
        PrintStream logger = context.getTaskListener().getLogger();
        FilePath workspace = context.getWorkspace();
        Launcher launcher = context.getLauncher();
        EnvVars envVars = context.getEnvVars();

        // ensure wrapper is downloaded and available
        final String jenkinsHome = envVars.get("JENKINS_HOME");
        String wrapperDownloadPath = "";
        if(mergebaseConfig.getWrapperPath() == null) {
            wrapperDownloadPath = jenkinsHome + "/.mergebase/wrapper";
        } else {
            wrapperDownloadPath = mergebaseConfig.getWrapperPath();
        }

        ToolDownloader.ensureWrapperDownload(wrapperDownloadPath);

        final List<String> args = buildArgs(mergebaseConfig, Paths.get(wrapperDownloadPath + "/mergebase.jar"));
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
