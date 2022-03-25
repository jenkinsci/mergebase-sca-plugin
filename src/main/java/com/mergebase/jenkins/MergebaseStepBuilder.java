package com.mergebase.jenkins;

import com.mergebase.jenkins.execption.MergebaseException;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import io.jenkins.cli.shaded.org.slf4j.Logger;
import io.jenkins.cli.shaded.org.slf4j.LoggerFactory;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import jenkins.tasks.SimpleBuildStep;
import static hudson.Util.fixEmptyAndTrim;

public class MergebaseStepBuilder extends Builder implements SimpleBuildStep {

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

    private static final Logger LOG = LoggerFactory.getLogger(MergebaseStepBuilder.class);

    @DataBoundConstructor
    public MergebaseStepBuilder(final String url,
                                final String customerToken,
                                final String projectName,
                                final String severityThreshold,
                                final String mbScanPath,
                                final String wrapperPath,
                                boolean scanAll,
                                boolean debugMode,
                                boolean jsonOutput,
                                boolean killBuild) {
        this.url = url;
        this.customerToken = customerToken;
        this.projectName = projectName;
        this.severityThreshold = severityThreshold;
        this.mbScanPath = mbScanPath;
        this.wrapperPath = wrapperPath;
        this.scanAll = scanAll;
        this.debugMode = debugMode;
        this.jsonOutput = jsonOutput;
        this.killBuild = killBuild;
    }

    public MergebaseStepBuilder(){}

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = url;
    }

    @DataBoundSetter
    public void setCustomerToken(String customerToken) {
        this.customerToken = customerToken;
    }

    @DataBoundSetter
    public void setSeverityThreshold(String severityThreshold) {
        this.severityThreshold = severityThreshold;
    }

    @DataBoundSetter
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @DataBoundSetter
    public void setScanAll(boolean scanAll) {
        this.scanAll = scanAll;
    }

    @DataBoundSetter
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @DataBoundSetter
    public void setMbScanPath(String mbScanPath) {
        this.mbScanPath = mbScanPath;
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
    public void setWrapperPath(String wrapperPath) {
        this.wrapperPath = wrapperPath;
    }

    public String getUrl() {
        return url;
    }

    public String getCustomerToken() {
        return customerToken;
    }

    public String getSeverityThreshold() {
        return severityThreshold;
    }

    public String getProjectName() {
        return projectName;
    }

    public boolean isScanAll() {
        return scanAll;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isJsonOutput() {
        return jsonOutput;
    }

    public String getMbScanPath() {
        return mbScanPath;
    }

    public boolean isKillBuild() {
        return killBuild;
    }

    public String getWrapperPath() {
        return wrapperPath;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        try {
            GenericRunContext genericRunContext = GenericRunContext.forFreestyleProject(run, workspace, launcher, listener);
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
            MergeBaseRun.scanProject(genericRunContext, mergebaseConfig);
        } catch (MergebaseException e) {
            throw new InterruptedException(e.getLocalizedMessage());
        }
    }


    // TODO improve validation
    @Extension
    public static final class MergebaseStepBuilderDescriptor extends BuildStepDescriptor<Builder> {

        public MergebaseStepBuilderDescriptor() {
            load();
        }

        public FormValidation doCheckProjectName(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error("Must have a project name");
            return FormValidation.ok();
        }

        public FormValidation doCheckUrl(@QueryParameter String value) {
            value = fixEmptyAndTrim(value);
            if(value == null) {
                return FormValidation.errorWithMarkup("You must add your MergeBase Dashboard URL. URL must be entered in the form <code>https://[your-domain].mergebase.com </code>.");
            }

            if(!value.toLowerCase(Locale.ROOT).startsWith("https://")) {
                return FormValidation.errorWithMarkup("Not a valid URL value. URL must be entered in the form <code>https://[your-domain].mergebase.com </code>.");
            }

            if(!value.toLowerCase(Locale.ROOT).endsWith(".com")) {
                return FormValidation.errorWithMarkup("Not a valid URL value. URL must be entered in the form <code>https://[your-domain].mergebase.com </code>.");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckCustomerToken(@QueryParameter String value) {
            value = fixEmptyAndTrim(value);
            if(value == null) {
                return FormValidation.error("You must add your customer token.");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckMbScanPath(@QueryParameter String value) {
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "MergeBase Build Step";
        }

        public String getIconFileName() {
            return "/plugin/mergebase-jenkins-plugin/img/mb-logo-and-name.png";
        }
    }
}
