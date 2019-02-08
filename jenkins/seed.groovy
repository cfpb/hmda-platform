projects = [
    [name: "auth", repo: "hmda-platform", jenkinsfilePath: "auth/Jenkinsfile"],
    [name: "census-api", repo: "hmda-platform", jenkinsfilePath: "census-api/Jenkinsfile"],
    [name: "check-digit", repo: "hmda-platform", jenkinsfilePath: "check-digit/Jenkinsfile"],
    [name: "hmda-analytics", repo: "hmda-platform", jenkinsfilePath: "hmda-analytics/Jenkinsfile"],
    [name: "hmda-help", repo: "hmda-help", jenkinsfilePath: "Jenkinsfile"],
    [name: "hmda-homepage", repo: "hmda-homepage", jenkinsfilePath: "Jenkinsfile"],
    [name: "hmda-platform", repo: "hmda-platform", jenkinsfilePath: "hmda/Jenkinsfile"],
    [name: "hmda-platform-ui", repo: "hmda-platform-ui", jenkinsfilePath: "Jenkinsfile"],
    [name: "hmda-platform-tools", repo: "hmda-platform-tools", jenkinsfilePath: "Jenkinsfile"],
    [name: "hmda-pub-ui", repo: "hmda-pub-ui", jenkinsfilePath: "Jenkinsfile"],
    [name: "hmda-regulator", repo: "hmda-platform", jenkinsfilePath: "hmda-regulator/Jenkinsfile"],
    [name: "keycloak", repo: "hmda-platform", jenkinsfilePath: "kubernetes/keycloak/Jenkinsfile"],
    [name: "institutions-api", repo: "hmda-platform", jenkinsfilePath: "institutions-api/Jenkinsfile"],
    [name: "irs-publisher", repo: "hmda-platform", jenkinsfilePath: "irs-publisher/Jenkinsfile"],
    [name: "modified-lar", repo: "hmda-platform", jenkinsfilePath: "modified-lar/Jenkinsfile"],
    [name: "theme-provider", repo: "hmda-platform", jenkinsfilePath: "kubernetes/keycloak/theme-provider/Jenkinsfile"]
]

projects.each { project ->
    multibranchPipelineJob(project.name) {
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
