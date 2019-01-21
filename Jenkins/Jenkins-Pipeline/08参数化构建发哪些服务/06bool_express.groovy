pipeline {
    agent any
    parameters{
        booleanParam(name: 'skip_run', description: 'Skips all stages. Used to update parameters in case of changes.', defaultValue: false)
    }

    stages {
        stage('Doing Stuff') {
        when {
            expression { return params.skip_run ==~ /(?i)(N|NO|F|FALSE|OFF|STOP)/ }
        }
        steps {
            echo "${params.skip_run}"
            }
        }
    }
}