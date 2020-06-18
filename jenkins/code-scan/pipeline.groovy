pipeline {

    agent any

    options {
        ansiColor('xterm')
        buildDiscarder(logRotator(numToKeepStr:'10'))
        skipDefaultCheckout()
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    gitScm.checkout(
                        env.SCM_APP_REPO,
                        "master",
                        false
                    )
                }
            }
        }

        stage('Code Scan') {
            steps {
                step(
                    script {
                        security.codeScan("${env.APP_NAME}-scan")
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
