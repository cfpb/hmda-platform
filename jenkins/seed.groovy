projects = [
    [name: "hmda-help", repo: "hmda-help", jenkinsfilePath: "Jenkinsfile"]
]

projects.each { project ->
    multibranchPipelineJob(project.name) {
        properties([[$class: 'ParametersDefinitionProperty', parameterDefinitions: [[$class: 'StringParameterDefinition', name: 'myparam', defaultValue: 'default value']]]])
        branchSources {
            github {
                repoOwner('cfpb')
                repository(project.repo)
                scanCredentialsId('github')
                buildForkPRHead(true)
                buildForkPRMerge(false) 
            }
            orphanedItemStrategy {
                discardOldItems {
                    daysToKeep(1)
                }
            }
            factory {
                workflowBranchProjectFactory {
                    scriptPath(project.jenkinsfilePath)
                }
            }
            triggers {
                periodic(10)
            }
        }
    }
}
