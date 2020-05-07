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
                            remote: 'https://github.cfpb.gov/HMDA-Operations/hmda-devops.git'
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
