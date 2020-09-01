projects = [
    [name: "auth", repo: "hmda-platform", jenkinsfilePath: "auth/Jenkinsfile"],
    [name: "check-digit", repo: "hmda-platform", jenkinsfilePath: "check-digit/Jenkinsfile"],
    [name: "email-service", repo: "hmda-platform", jenkinsfilePath: "email-service/Jenkinsfile"],
    [name: "hmda-analytics", repo: "hmda-platform", jenkinsfilePath: "hmda-analytics/Jenkinsfile"],
    [name: "hmda-data-browser-api", repo: "hmda-platform", jenkinsfilePath: "data-browser/Jenkinsfile"],
    [name: "hmda-frontend", repo: "hmda-frontend", jenkinsfilePath: "Jenkinsfile"],
    [name: "hmda-help", repo: "hmda-help", jenkinsfilePath: "Jenkinsfile"],
    [name: "hmda-platform", repo: "hmda-platform", jenkinsfilePath: "hmda/Jenkinsfile"],
    [name: "hmda-data-publisher", repo: "hmda-platform", jenkinsfilePath: "hmda-data-publisher/Jenkinsfile"],
    [name: "hmda-reporting", repo: "hmda-platform", jenkinsfilePath: "hmda-reporting/Jenkinsfile"],
    [name: "keycloak", repo: "hmda-platform", jenkinsfilePath: "kubernetes/keycloak/Jenkinsfile"],
    [name: "institutions-api", repo: "hmda-platform", jenkinsfilePath: "institutions-api/Jenkinsfile"],
    [name: "irs-publisher", repo: "hmda-platform", jenkinsfilePath: "irs-publisher/Jenkinsfile"],
    [name: "modified-lar", repo: "hmda-platform", jenkinsfilePath: "modified-lar/Jenkinsfile"],
    [name: "newman", repo: "hmda-platform", jenkinsfilePath: "newman/Jenkinsfile"],
    [name: "rate-limit", repo: "hmda-platform", jenkinsfilePath: "rate-limit/Jenkinsfile"],
    [name: "ratespread-calculator", repo: "hmda-platform", jenkinsfilePath: "ratespread-calculator/Jenkinsfile"],
    [name: "theme-provider", repo: "hmda-platform", jenkinsfilePath: "kubernetes/keycloak/theme-provider/Jenkinsfile"]
]

projects.each { project ->
    multibranchPipelineJob(project.name) {
        branchSources {
            github {
                id('hmda')
                repoOwner('cfpb')
                repository(project.repo)
                scanCredentialsId('cfpbhmdadeploybot-github')
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
                periodic(30)
            }
        }
    }
}

// repositories for code scanning
def repositories = [
    [name: 'hmda-frontend', repo: "https://github.com/cfpb/hmda-frontend"],
    [name: 'hmda-platform',repo: "https://github.com/cfpb/hmda-platform"],
    [name: 'hmda-help',repo: "https://github.com/cfpb/hmda-help"]
]

repositories.each{ repo ->

    pipelineJob("code-scan/${repo.name}") {

        triggers {
            cron('10 9 * * *')
        }

        environmentVariables {
            env('APP_NAME', repo.name)
            env('SCM_APP_REPO', repo.repo)
            env('SCM_APP_BRANCH', 'master')


            keepBuildVariables(true)
        }
        definition {
            cps {
                script(readFileFromWorkspace("jenkins/code-scan/pipeline.groovy"))
                sandbox()
            }
        } 

    }
}


multibranchPipelineJob('hmda-platform-api-docs') {
    branchSources {
        github {
            id('hmda')
            repoOwner('cfpb')
            repository("hmda-platform-api-docs")
            scanCredentialsId('cfpbhmdadeploybot-github')
        }
        orphanedItemStrategy {
            discardOldItems {
                daysToKeep(1)
            }
        }
        factory {
            workflowBranchProjectFactory {
                scriptPath("Jenkinsfile")
            }
        }
        triggers {
            periodic(30)
        }
    }
}