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

        stage ("touch file") {
            steps {
                script {
                    touch_file = env.WORKSPACE + "/testdata/"+ env.BUILD_NUMBER +".log"
                    touch touch_file
                }
            }
        }
    }
}