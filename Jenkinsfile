pipeline {

    environment {
        buildStatus = ""
    }

    agent any

    stages {

        stage("Prepare") {
            steps {
                sh "./gradlew clean"
            }
        }

        stage("Test") {
            steps {
                sh "./gradlew test"
            }
        }

        stage("Build") {
            steps {
                sh "./gradlew build -x test"
            }
        }

        stage("Build And Run Docker Image (Dev)") {

            when {
                anyOf {
                    branch "develop"
                }
            }

            steps {
                script {
                    docker.withRegistry("https://scaramangado.de:10191", "barinade-registry") {
                        def devImage = docker.build("barinade/bot:dev")
                        devImage.push()
                    }
                }

                withCredentials([sshUserPrivateKey(credentialsId: "BarinadeSSH", keyFileVariable: 'keyfile')]) {
                    sh "ssh barinade@scaramangado.de -i $keyfile docker-compose -f /barinade/barinade-infrastructure/dev/docker-compose.yml up -d --force-recreate bot"
                }
            }
        }

        stage("Cleanup") {
            steps {
                sh "docker image prune -f"
            }
        }

        stage("Discord Notification") {

            when {
                anyOf {
                    branch "master"
                    branch "develop"
                    not { equals expected: "SUCCESS", actual: currentBuild.currentResult }
                }
            }

            steps {

                script { buildStatus = currentBuild.currentResult == "SUCCESS" ? "successful" : "failed" }

                discordSend(webhookURL: "https://discordapp.com/api/webhooks/637956171864604683/3fv_nq0NWEyKvw-U8kocLD7WjgChybnp8j7hJtaZw7eo7aBYvtkAYpLVGDUaGvfAEEWd",
                        title: "Build of ${env.BRANCH_NAME} $buildStatus", result: currentBuild.currentResult)
            }
        }
    }
}
