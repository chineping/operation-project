pipeline {
    agent none
    stages {
        stage('Non-Sequential Stage') {
            agent {
                label 'master'
            }
            steps {
                echo "On Non-Sequential Stage"
            }
        }
        stage('Sequential') {
            agent {
                label 'master'
            }
            environment {
                FOR_SEQUENTIAL = "some-value"
            }
            stages {
               stage('In Sequential 1') {
                   steps {
                       echo "In Sequential 1"
                   }
               }
               stage('In Sequential 2') {
                   steps {
                       echo "In Sequential 2"
                   }
               }
            }
        }
    }
}
