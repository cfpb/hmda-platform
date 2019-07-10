# Newman Jenkins API Testing

## Introduction

For more information on Newman CLI integration , checkout the [Newman Command Line Intergration](https://www.getpostman.com/docs/v6/postman/collection_runs/command_line_integration_with_Newman).

## Newman CLI

This repo serves as a template for implementing a CLI interface for testing API endpoints via a postman collections JSON file. With this method, scripts can be created inside `Jenkinsfiles` to test `HMDA-Platform` API endpoints either periodically as standalone Jenkins job or during the build processes of the individual HMDA-Platform micro-services.

## Jenkins Job Testing

By created a `MultiPlatform Project` in Jenkins where the the target `HMDA-Platform` microservies exits, users can reference the `HMDA-Devops` repo and branch for their test cases in the folder `newman-tests`.

# Example Source Control Declaration:

![Example Source Control Declaration:](img/source_branch_example.png?raw=true "Example Source Control Declaration")


# Jenkinsfile Path

Ensure the path to the `Jenkinsfile` is properly defined (starting from the root of the repo).

![Example Jenkinsfile Declaration:](img/jenkinsfile_path_example.png?raw=true "Example Jenkinsfile Declaration")


# Jenkinsfile Example

The following script serves as a template of how to define your standalone test Jenkins jobs. Please note you should be able to include the `stage` section of this example to your respective `Jenkinsfile`.

You will have to ensure the workspace is true for the target workspace:

 - Node is available in the operating environment
 - The workspace has access to the Newman test JSON Jenkinsfiles
 - You have permission to access the target API endpoints from this Jenkins environment

```
podTemplate(label: 'buildNode', containers: [
    containerTemplate(name: 'node', image: 'node:8', ttyEnabled: true, command: 'cat')
]) {
    node('buildNode') {
    checkout scm
         stage('Setup NPM and Newman') {
             container('node') {
                sh "npm update"
                sh "npm install -g newman"
                sh "cd newman-tests/hmda-checkDigit && newman run hmda-checkDigit-api-test.json -d hmda-checkDigit-api-config.json"
              }
         }
     }}

```

## Newman Test and Configuration JSON

Please refer to the following Wiki pages for test configuration.

[Newman Postman Collection Test Configuration](NewmanTests.md).
