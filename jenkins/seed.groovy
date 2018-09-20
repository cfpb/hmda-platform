projects = [
    [name: "cfpb", repo: "hmda-platform", jenkinsfilePath: "hmda/Jenkinsfile", ],
    [name: "cfpb", repo: "hmda-platform", jenkinsfilePath: "check-digit/Jenkinsfile", ],
    [name: "cfpb", repo: "hmda-help", jenkinsfilePath: "Jenkinsfile", ]
]

projects.each { project ->
    multibranchPipelineJob(project.name) {
        branchSources {
            github {
                repoOwner('cfpb')
                repository(project.repo)
                scanCredentialsId('github')
            }
            factory {
                workflowBranchProjectFactory {
                    scriptPath(project.jenkinsfilePath)
                }
            }
        }
    }
}