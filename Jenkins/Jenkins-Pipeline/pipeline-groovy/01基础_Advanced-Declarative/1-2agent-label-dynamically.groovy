agentName = "Windows"
agentLabel = "${-> println 'Right Now the Agent Name is ' + agentName; return agentName}"

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

            steps {
                node( agentLabel as String ) {  // Evaluate the node label later
                    echo "TEST"
                }
                script {
                    println agentLabel
                    println agentName
                }
            }
        }
    }
}