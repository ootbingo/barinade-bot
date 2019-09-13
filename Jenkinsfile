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
                sh "./gradlew test --reload-dependencies"
            }
        }

        stage("Build") {
            steps {
                sh "./gradlew build -x test"
            }
        }
    }
}
