pipeline {
    agent any
    options { 
        timestamps () 
    }  

    stages {
        stage('Non-Parallel Stage') {
            steps {
                echo 'This stage will be executed first.'
            }
        }

        stage('Parallel Stage') {
            failFast true
            parallel {
                stage('并行一') {
                    steps {
                        echo "并行一"
                    }
                }

                stage('并行二') {
                    steps {
                        echo "并行二"
                    }
                }

                stage('并行三') {
                    stages {
                        stage('Nested 1') {
                            steps {
                                echo "In stage Nested 1 within Branch C"
                            }
                        }

                        stage('Nested 2') {
                            steps {
                                echo "In stage Nested 2 within Branch C"
                            }
                        }
                    }
                }
            }
        }
    }
}

