pipeline {

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
                    branch "develop";
                    branch "feature/docker"
                }
            }

            steps {
                script {
                    docker.withRegistry("https://scaramangado.de:10191", "barinade-registry") {
                        def devImage = docker.build("barinade/bot:dev")
                        devImage.push()
                    }
                }
            }
        }

        stage("Cleanup") {
            steps {
                sh "docker image prune -f"
            }
        }
    }
}
