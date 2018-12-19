pipeline {
    agent any	
    stages {
        stage ('flow control') {
            steps {
                script {
                    if ( 10 == 10) {
                        println "pass"
                    } else {
                        println "failed"
                    }
                }
            }
        }
    }
}
