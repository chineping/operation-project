pipeline {
    agent any
    parameters {
        // choice( choices: ['greeting' , 'silence'], description: '', name: 'REQUESTED_ACTION')
        choice(choices: "greeting\nsilence\n", description: 'delimiters within string', name: 'REQUESTED_ACTION')
    }

    stages {
        stage ('Speak') {
            when {
                // Only say hello if a "greeting" is requested
                expression { params.REQUESTED_ACTION == 'greeting' }
            }
            steps {
                echo "Hello, bitwiseman!"
            }
        }
    }
}