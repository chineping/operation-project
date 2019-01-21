pipeline {
    options {
        timestamps () 
        buildDiscarder logRotator(artifactDaysToKeepStr: '3', artifactNumToKeepStr: '1', daysToKeepStr: '3', numToKeepStr: '10')
    }
    agent any
    parameters {
        string(name:'serverIP', defaultValue: 'serverIP', description: '部署代码所在ip地址')
        string(name: 'proPath',defaultValue:'/data/harbin', description: '服务路径')
}
    stages {
        stage ('拉取gitlab代码') {
            tools { git 'git2.14.1' }
            steps {
                git branch: 'master', credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: 'git@gitlabIP:BJ/haerbin-web.git'
            }
        }

        stage ('nodejs构建') {
            tools {
                nodejs 'NodeJS_V8.12'
            }

            steps {             
                sh "npm install && npm run build" 
                }
            }

        stage ('备份原代码'){
            agent {
               label 'serverIP'
            }

            steps{
                sh "cp -a ${params.proPath} /data/bak/`date +%Y%m%d`+harbin"
            }
        }

        stage ('推送测试包'){
            steps {
                sh "scp -r dist/* root@${params.serverIP}:${params.proPath}"
            }
        }
    }
}