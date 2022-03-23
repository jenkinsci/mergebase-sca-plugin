package com.mergebase.jenkins;

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

//    private static final String CLT_PROPERTIES = "mergebase.clt.properties";
//    public static final String MB_CUSTOMER = "mb.customer";
//    public static final String MB_URL = "mb.url";

    private String domain;
    private String customerToken;
    private String projectName;
    private String severityThreshold;
    private String mbScanPath;
    private boolean scanAll;
    private boolean debugMode;
    private boolean jsonOutput;

    private static final Logger LOG = LoggerFactory.getLogger(MergebaseStepBuilder.class);

    @DataBoundConstructor
    public MergebaseStepBuilder(final String domain,
                                final String customerToken,
                                final String projectName,
                                final String severityThreshold,
                                final String mbScanPath,
                                boolean scanAll,
                                boolean debugMode,
                                boolean jsonOutput) {
        this.domain = domain;
        this.customerToken = customerToken;
        this.projectName = projectName;
        this.severityThreshold = severityThreshold;
        this.mbScanPath = mbScanPath;
        this.scanAll = scanAll;
        this.debugMode = debugMode;
        this.jsonOutput = jsonOutput;
    }

    public MergebaseStepBuilder(){}

    @DataBoundSetter
    public void setDomain(String domain) {
        this.domain = domain;
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

    public String getDomain() {
        return domain;
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

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        URL res = getClass().getClassLoader().getResource("mergebase.jar");
        Path file = null;
        try {
            assert res != null;
            file = Paths.get(res.toURI());
            GenericRunContext genericRunContext = GenericRunContext.forFreestyleProject(run, workspace, launcher, listener);
            MergebaseConfig mergebaseConfig = new MergebaseConfig();
            mergebaseConfig.setCustomerToken(customerToken);
            mergebaseConfig.setDomain(domain);
            mergebaseConfig.setProjectName(projectName);
            mergebaseConfig.setSeverityThreshold(severityThreshold);
            mergebaseConfig.setEnableScanAll(scanAll);
            mergebaseConfig.setEnableDebugMode(debugMode);
            mergebaseConfig.setEnableJsonOutput(jsonOutput);
            String tmpPath = mbScanPath;
            if(mbScanPath == null  || mbScanPath.equals("")){
                tmpPath = ".";
            }
            mergebaseConfig.setScanPath(tmpPath);
            mergebaseConfig.setEnableDebugMode(false);
            mergebaseConfig.setEnableScanAll(false);
            MergeBaseRun.scanProject(genericRunContext, mergebaseConfig, file);
        } catch (URISyntaxException e) {
            //e.printStackTrace();
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

        public FormValidation doCheckDomain(@QueryParameter String value) {
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
