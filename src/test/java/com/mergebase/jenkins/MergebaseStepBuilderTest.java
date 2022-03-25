package com.mergebase.jenkins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Locale;

public class MergebaseStepBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    private String url;
    private String customerToken;
    private String projectName;
    private String severityThreshold;


    @Test
    public void testConfigRoundtrip() throws Exception {
        String url;
        String customerToken;
        String projectName;
        String severityThreshold;
        String mbScanPath;
        boolean scanAll;
        boolean debugMode;
        boolean jsonOutput;
        boolean killBuild;

        url = "https://demo.mergebase.com";
        customerToken = "test-token";
        projectName = "mergebase-test-project";
        severityThreshold = "5.0";
        mbScanPath = ".";
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new MergebaseStepBuilder(url,
                customerToken,
                projectName,
                severityThreshold,
                mbScanPath,
                "",
                false,
                false,
                false,
                false));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new MergebaseStepBuilder(url,
                customerToken,
                projectName,
                severityThreshold,
                mbScanPath,
                "",
                false,
                false,
                false,
                false), project.getBuildersList().get(0));
    }
}