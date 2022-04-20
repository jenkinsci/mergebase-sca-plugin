package com.mergebase.jenkins;

import com.mergebase.jenkins.execption.MergebaseException;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Result;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.Secret;
import io.jenkins.cli.shaded.org.slf4j.Logger;
import io.jenkins.cli.shaded.org.slf4j.LoggerFactory;
import net.sf.json.JSONObject;
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
import org.kohsuke.stapler.StaplerRequest;

import static hudson.Util.fixEmptyAndTrim;

public class MergebaseStepBuilder extends Builder implements SimpleBuildStep {

    private String projectName;
    private String severityThreshold;
    private String mbScanPath;
    private boolean scanAll;
    private boolean debugMode;
    private boolean jsonOutput;
    private boolean killBuild;

    private static final Logger LOG = LoggerFactory.getLogger(MergebaseStepBuilder.class);

    @DataBoundConstructor
    public MergebaseStepBuilder(final String projectName,
                                final String severityThreshold,
                                final String mbScanPath,
                                boolean scanAll,
                                boolean debugMode,
                                boolean jsonOutput,
                                boolean killBuild) {

        this.projectName = projectName;
        this.severityThreshold = severityThreshold;
        this.mbScanPath = mbScanPath;
        this.scanAll = scanAll;
        this.debugMode = debugMode;
        this.jsonOutput = jsonOutput;
        this.killBuild = killBuild;
    }

    public MergebaseStepBuilder(){}

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

    public String getUrl() {
        MergebaseStepBuilderDescriptor mergebaseStepBuilderDescriptor = (MergebaseStepBuilderDescriptor) super.getDescriptor();
        return mergebaseStepBuilderDescriptor.getUrl();
    }

    public Secret getCustomerToken() {
        MergebaseStepBuilderDescriptor mergebaseStepBuilderDescriptor = (MergebaseStepBuilderDescriptor) super.getDescriptor();
        return mergebaseStepBuilderDescriptor.getCustomerToken();
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
        MergebaseStepBuilderDescriptor mergebaseStepBuilderDescriptor = (MergebaseStepBuilderDescriptor) super.getDescriptor();
        return mergebaseStepBuilderDescriptor.getWrapperPath();
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        try {
            if(fixEmptyAndTrim(getUrl()) == null) {
                throw new InterruptedException("Please ensure you have set your MergeBase dashboard URL in Global Settings. Contact an administrator for more information.");
            }

            if(fixEmptyAndTrim(getCustomerToken().getPlainText()) == null) {
                throw new InterruptedException("Please ensure you have set your MergeBase Customer Token in Global Settings. Contact an administrator for more information.");
            }

            GenericRunContext genericRunContext = GenericRunContext.forFreestyleProject(run, workspace, launcher, listener);
            MergebaseConfig mergebaseConfig = new MergebaseConfig();
            mergebaseConfig.setCustomerToken(getCustomerToken());
            mergebaseConfig.setDomain(getUrl());
            mergebaseConfig.setProjectName(projectName);
            mergebaseConfig.setSeverityThreshold(severityThreshold);
            mergebaseConfig.setEnableScanAll(scanAll);
            mergebaseConfig.setEnableDebugMode(debugMode);
            mergebaseConfig.setEnableJsonOutput(jsonOutput);
            mergebaseConfig.setKillBuild(killBuild);
            mergebaseConfig.setWrapperPath(fixEmptyAndTrim(getWrapperPath()));
            String tmpPath = mbScanPath;
            if(mbScanPath == null  || mbScanPath.equals("")){
                tmpPath = ".";
            }
            mergebaseConfig.setScanPath(tmpPath);
            MergeBaseRun.scanProject(genericRunContext, mergebaseConfig);

        } catch (MergebaseException e) {
            throw new InterruptedException(e.getLocalizedMessage());
        }
    }


    @Extension
    public static final class MergebaseStepBuilderDescriptor extends BuildStepDescriptor<Builder> {
        private String wrapperPath;
        private String url;
        private Secret customerToken;

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            wrapperPath = formData.getString("wrapperPath");
            url = formData.getString("url");
            customerToken = Secret.fromString(formData.getString("customerToken"));
            save();
            return super.configure(req, formData);
        }

        public MergebaseStepBuilderDescriptor() {
            super();
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

        public FormValidation doCheckMbScanPath(@QueryParameter String value) {
            if(value.contains("../")) {
                return FormValidation.error("You cannot specific a path outside the current workspace.");
            }
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

        public String getWrapperPath() {
            return wrapperPath;
        }

        public String getUrl() {
            return url;
        }

        public Secret getCustomerToken() {
            return customerToken;
        }
    }
}
