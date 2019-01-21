pipeline {
    agent {
        node {
            label "win-anthony"
            customWorkspace "${env.JOB_NAME}/${env.BUILD_NUMBER}"
        }
   }
   stages {
       stage ("list directory") {
           steps {
               script {
                    bat "dir"
               }
           }
       }
 
       stage ("Which java") {
           steps {
               script {
                    bat "java -version"
               }
           }
       }
   }
}
