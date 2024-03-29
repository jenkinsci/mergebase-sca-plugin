package com.mergebase.jenkins;

import hudson.util.Secret;

import java.io.Serializable;

public class MergebaseConfig implements Serializable {
    private String domain;
    private Secret customerToken;
    private String projectName;
    private String severityThreshold;
    private boolean enableScanAll;
    private boolean enableDebugMode;
    private boolean enableJsonOutput;
    private boolean killBuild;
    private String scanPath;
    private String wrapperPath;

    public MergebaseConfig(){}

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Secret getCustomerToken() {
        return customerToken;
    }

    public void setCustomerToken(Secret customerToken) {
        this.customerToken = customerToken;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSeverityThreshold() {
        return severityThreshold;
    }

    public void setSeverityThreshold(String severityThreshold) {
        this.severityThreshold = severityThreshold;
    }

    public boolean isEnableScanAll() {
        return enableScanAll;
    }

    public void setEnableScanAll(boolean enableScanAll) {
        this.enableScanAll = enableScanAll;
    }

    public boolean isEnableDebugMode() {
        return enableDebugMode;
    }

    public void setEnableDebugMode(boolean enableDebugMode) {
        this.enableDebugMode = enableDebugMode;
    }

    public boolean isEnableJsonOutput() {
        return enableJsonOutput;
    }

    public void setEnableJsonOutput(boolean enableJsonOutput) {
        this.enableJsonOutput = enableJsonOutput;
    }

    public boolean isKillBuild() {
        return killBuild;
    }

    public void setKillBuild(boolean killBuild) {
        this.killBuild = killBuild;
    }

    public String getScanPath() {
        return scanPath;
    }

    public void setScanPath(String scanPath) {
        this.scanPath = scanPath;
    }

    public String getWrapperPath() {
        return wrapperPath;
    }

    public void setWrapperPath(String wrapperPath) {
        this.wrapperPath = wrapperPath;
    }
}
