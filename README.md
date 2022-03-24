# mergebase-jenkins-plugin

## Introduction

Scan your Source Code for vulnerabilities with MergeBase. Officially supported by MergeBase Software.

## Getting started

### Required Parameters
The following parameters are required. The values in parentheses is used in pipeline workflow configuration.

Project Name (projectName):
A unique name for your project. This will be the name that represents the project in the MergeBase Dashboard.

MergeBase Dashboard URL (url):
Add your dashboard URL in the form https://[your-organization].mergebase.com . If you have an on-premise installation, use your custom URL.

Customer Token (customerToken):
Your API token from your Dashboard. This can be found on the Settings page.

### Optional Parameters
The values in parentheses is used in pipeline workflow configuration.

Severity Threshold (severityThreshold):
Vulnerabilities below the following CVSS/Risk Score threshold are ignored. (between 0.0 - 10.0)

Path to scan(mbScanPath):
This defaults to `./`. It can be modified for your project's setup.

Scan all projects found (scanAll): 
Scan the build directory recursively to find all projects with compatible build files. This defaults to false, and the MergeBase scanner will select the first build file it find in the current directory or the specific file if you have selected a file-path. 

Enable Debug logging (debugMode):
Enables debug output for use in troubleshooting.

Enable JSON output (jsonOutput):
Outputs the MergeBase report in JSON form for use in automation.

### Freestyle Projects
In a freestyle project, add "MergeBase SCA Scan" build step. Add the required parameters as listed above.  

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

