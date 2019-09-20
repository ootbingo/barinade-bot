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

            docker.withRegistry("http://h2841273.stratoserver.net:10091/repository/docker/", "barinade-registry") {
                def devImage = docker.build("barinade/bot:dev")
                devImage.push()
            }
        }

        stage("Cleanup") {
            sh "docker image prune -f"
        }
    }
}
