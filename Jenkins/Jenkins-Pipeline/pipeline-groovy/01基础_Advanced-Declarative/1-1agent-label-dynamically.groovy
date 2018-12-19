agentName = "Windows"
agentLabel = "${println 'Right Now the Agent Name is ' + agentName; return agentName}"

pipeline {
    agent none

    stages {
        stage('Prep') {
            steps {
                script {
                    agentName = "Linux"
                }
            }
        }
        stage('Checking') {
            steps {
                script {
                    println agentLabel
                    println agentName
                }
            }
        }
        stage('Final') {
            agent { label agentLabel }

            steps {
                script {
                    println agentLabel
                    println agentName
                }
            }
    }

    }
}