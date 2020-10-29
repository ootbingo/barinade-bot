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

            post {
                always {
                    step([$class: 'JUnitResultArchiver', testResults: 'build/test-results/test/*.xml'])
                }
            }
        }

        stage("SonarQube") {

            steps {
                withCredentials([usernamePassword(credentialsId: "sonarcloud_ootbingo", usernameVariable: 'sonarUsername',
                        passwordVariable: 'sonarPassword')]) {
                    sh "./gradlew sonarqube -PsonarUsername=$sonarUsername -PsonarPassword=$sonarPassword"
                }
            }
        }

        stage("Build") {
            steps {
                sh "./gradlew build -x test"
            }
        }

        stage("Build And Run Docker Image (INT)") {

            when {
                anyOf {
                    branch "develop"
                }
            }

            steps {
                script {
                    docker.withRegistry("https://barinade.scaramangado.de:10193", "scaramangado-registry") {
                        def devImage = docker.build("barinade/bot:dev")
                        devImage.push()
                    }
                }

                withCredentials([sshUserPrivateKey(credentialsId: "BarinadeSSH", keyFileVariable: 'keyfile')]) {
                    sh "ssh -oStrictHostKeyChecking=no barinade@scaramangado.de -i $keyfile docker-compose -f /barinade/barinade-infrastructure/dev/docker-compose.yml up -d --force-recreate bot"
                }
            }
        }

        stage("Build And Run Docker Image (Prod)") {

            when {
                anyOf {
                    branch "release/1.0.0"
                }
            }

            steps {
                script {
                    docker.withRegistry("https://barinade.scaramangado.de:10193", "scaramangado-registry") {
                        def devImage = docker.build("barinade/bot:prod")
                        devImage.push()
                    }
                }

                withCredentials([sshUserPrivateKey(credentialsId: "BarinadeSSH", keyFileVariable: 'keyfile')]) {
                    sh "ssh -oStrictHostKeyChecking=no barinade@scaramangado.de -i $keyfile docker-compose -f /barinade/barinade-infrastructure/prod/docker-compose.yml up -d --force-recreate bot"
                }
            }
        }

        stage("Cleanup") {

            when {
                anyOf {
                    branch "master"
                    branch "develop"
                }
            }

            steps {
                sh "docker image prune -f"
            }
        }
    }

    post {
        always {
            script {
                if (env.BRANCH_NAME == "master" || env.BRANCH_NAME == "develop"
                        || currentBuild.currentResult != "SUCCESS") {

                    buildStatus = currentBuild.currentResult == "SUCCESS" ? "successful" : "failed"

                    discordSend(webhookURL: "https://discordapp.com/api/webhooks/637956171864604683/3fv_nq0NWEyKvw-U8kocLD7WjgChybnp8j7hJtaZw7eo7aBYvtkAYpLVGDUaGvfAEEWd",
                            title: "Build of ${env.BRANCH_NAME} $buildStatus", result: currentBuild.currentResult)
                }
            }
        }
    }
}
