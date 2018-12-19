pipeline {
    agent any
    tools {
        maven 'maven3'
        jdk 'jdk1.8'
    }
    stages {
        stage ('GetCode') {
            steps {
                git branch: 'bj_dev', credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: 'git@gitlabIP:plusplatform/plusplatform.git'
                }
            }

        stage ('Build') {
            steps {
                sh 'mvn -f plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/pom.xml -Dmaven.test.failure.ignore=true clean install' 
                }
            post {
                success {
                    junit 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/target/surefire-reports/*.xml' 
                    }
                }
            }

        stage ('Utility Steps method') {
            steps {
                script {
                    def files = findFiles(glob: '**/*SNAPSHOT.jar')
                    echo files[0].name
                    //echo files[1].name
                }
            }
        }
    }
}
