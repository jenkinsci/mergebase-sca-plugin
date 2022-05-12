package com.mergebase.jenkins;

import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Locale;

public class MergebaseStepBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testConfigRoundtrip() throws Exception {
        String projectName = "mergebase-test-project";
        String severityThreshold = "5.0";
        String mbScanPath = ".";
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new MergebaseStepBuilder(projectName,
                severityThreshold,
                mbScanPath,
                false,
                false,
                false,
                false));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new MergebaseStepBuilder(projectName,
                severityThreshold,
                mbScanPath,
                false,
                false,
                false,
                false), project.getBuildersList().get(0));
    }
}