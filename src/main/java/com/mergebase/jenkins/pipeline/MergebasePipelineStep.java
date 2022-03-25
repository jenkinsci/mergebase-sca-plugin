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

import static hudson.Util.fixEmptyAndTrim;

public class MergebasePipelineStep  extends Step {

    private String url;
    private String customerToken;
    private String projectName;
    private String severityThreshold;
    private String mbScanPath;
    private String wrapperPath;
    private boolean scanAll;
    private boolean debugMode;
    private boolean jsonOutput;
    private boolean killBuild;

    @DataBoundConstructor
    public MergebasePipelineStep() {
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

    public boolean isScanAll() {
        return scanAll;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public String getUrl() {
        return url;
    }

    public String getMbScanPath() {
        return mbScanPath;
    }

    public String getWrapperPath() {
        return wrapperPath;
    }

    public boolean isJsonOutput() {
        return jsonOutput;
    }

    public boolean isKillBuild() {
        return killBuild;
    }

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = url;
    }

    @DataBoundSetter
    public void setMbScanPath(String mbScanPath) {
        this.mbScanPath = mbScanPath;
    }

    @DataBoundSetter
    public void setWrapperPath(String wrapperPath) {
        this.wrapperPath = wrapperPath;
    }

    @DataBoundSetter
    public void setJsonOutput(boolean jsonOutput) {
        this.jsonOutput = jsonOutput;
    }

    @DataBoundSetter
    public void setKillBuild(boolean killBuild) {
        this.killBuild = killBuild;
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
    public void setScanAll(boolean scanAll) {
        this.scanAll = scanAll;
    }

    @DataBoundSetter
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        MergebaseConfig mergebaseConfig = new MergebaseConfig();
        mergebaseConfig.setCustomerToken(customerToken);
        mergebaseConfig.setDomain(url);
        mergebaseConfig.setProjectName(projectName);
        mergebaseConfig.setSeverityThreshold(severityThreshold);
        mergebaseConfig.setEnableScanAll(scanAll);
        mergebaseConfig.setEnableDebugMode(debugMode);
        mergebaseConfig.setEnableJsonOutput(jsonOutput);
        mergebaseConfig.setKillBuild(killBuild);
        mergebaseConfig.setWrapperPath(fixEmptyAndTrim(wrapperPath));
        String tmpPath = mbScanPath;
        if(mbScanPath == null  || mbScanPath.equals("")){
            tmpPath = ".";
        }
        mergebaseConfig.setScanPath(tmpPath);
        mergebaseConfig.setEnableDebugMode(false);
        mergebaseConfig.setEnableScanAll(false);
        return new Execution(stepContext, mergebaseConfig);
    }


    public static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 2L;
        private MergebaseConfig mergebaseConfig;

        public Execution(StepContext context, MergebaseConfig mergebaseConfig) {
            super(context);
            this.mergebaseConfig = mergebaseConfig;
        }

        @Override
        protected Void run() throws IOException, MergebaseException {
            MergeBaseRun.scanProject(GenericRunContext.forPipelineProject(getContext()), mergebaseConfig);
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
