pipeline {
    agent any
    tools {
        maven 'maven3'
        jdk 'jdk1.8'
    }
    stages {
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
