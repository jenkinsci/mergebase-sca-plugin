package com.mergebase.jenkins.pipeline;

import com.mergebase.jenkins.GenericRunContext;
import com.mergebase.jenkins.MergeBaseRun;
import com.mergebase.jenkins.MergebaseConfig;
import com.mergebase.jenkins.MergebaseStepBuilder;
import com.mergebase.jenkins.execption.MergebaseException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.rmi.MarshalException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MergebasePipelineStep  extends Step {

    private String domain;
    private String customerToken;
    private String projectName;
    private String severityThreshold;
    private String scanPath;
    private boolean scanAll;
    private boolean debugMode;

    @DataBoundConstructor
    public MergebasePipelineStep() {
    }


    public String getDomain() {
        return domain;
    }

    public String getCustomerToken() {
        return customerToken;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getSeverityThreshold() {
        return severityThreshold;
    }

    public String getScanPath() {
        return scanPath;
    }

    public boolean isScanAll() {
        return scanAll;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    @DataBoundSetter
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @DataBoundSetter
    public void setCustomerToken(String customerToken) {
        this.customerToken = customerToken;
    }

    @DataBoundSetter
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @DataBoundSetter
    public void setSeverityThreshold(String severityThreshold) {
        this.severityThreshold = severityThreshold;
    }

    @DataBoundSetter
    public void setScanPath(String scanPath) {
        this.scanPath = scanPath;
    }

    @DataBoundSetter
    public void setScanAll(boolean scanAll) {
        this.scanAll = scanAll;
    }

    @DataBoundSetter
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new Execution(stepContext);
    }


    public static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 2L;

        public Execution(StepContext context) {
            super(context);
        }

        @Override
        protected Void run() throws IOException, URISyntaxException, MergebaseException {
            URL res = getClass().getClassLoader().getResource("mergebase.jar");
            MergebaseConfig mergebaseConfig = new MergebaseConfig();
            MergeBaseRun.scanProject(GenericRunContext.forPipelineProject(getContext()), mergebaseConfig, Paths.get(res.toURI()));
            return null;
        }
    }

    @Extension
    @Symbol("mergebaseScan")
    public static class MergebasePipelineStepDescriptor extends StepDescriptor {
        MergebaseStepBuilder mergebaseStepBuilder = new MergebaseStepBuilder();

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return new HashSet<>(Arrays.asList(EnvVars.class, FilePath.class, Launcher.class, Run.class, TaskListener.class));
        }

        @Override
        public String getFunctionName() {
            return "mergebaseScan";
        }


        @Override
        public String getDisplayName() {
            return "Run MergeBase SCA Scan";
        }

        @Override
        public String getConfigPage() {
            return getViewPage(MergebaseStepBuilder.class, "config.jelly");
        }

    }
}
