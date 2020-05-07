pipeline {

    agent any

    options {
        ansiColor('xterm')
        buildDiscarder(logRotator(numToKeepStr:'10'))
        skipDefaultCheckout()
        timestamps()
    }

    stages {
        stage('Init') {
            steps {
                script { 
                    library identifier: "hmdaUtils@master", changelog: false, retriever: modernSCM (
                        [
                            $class: 'GitSCMSource',
                            remote: env.SCM_HMDA_DEVOPS_REPO
                        ]
                    )
                }
                sh 'env | sort'
            }
        }

        stage('Checkout') {
            steps {
                script {
                    gitScm.checkout(
                        env.SCM_APP_REPO,
                        "master",
                        scanCredentialsId('cfpbhmdadeploybot-github'),
                        false
                    )
                }
            }
        }

        stage('Code Scan') {
            steps {
                step(
                    script {
                        security.codeScan("${env.APP_NAME}-scan", env.INCLUDE, env.EXCLUDE)
                    }
                )
            }
            post {
                always {
                    script {
                        security.codeScanPublish()
                    }
                }
            }
        }
    }
}
