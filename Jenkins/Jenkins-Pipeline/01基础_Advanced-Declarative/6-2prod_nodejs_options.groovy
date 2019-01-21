pipeline {
    options {
        timestamps () 
        buildDiscarder logRotator(artifactDaysToKeepStr: '3', artifactNumToKeepStr: '1', daysToKeepStr: '3', numToKeepStr: '10')
    }
    agent any
    parameters {
        string(name:'server1IP', defaultValue: 'IP1', description: '部署代码server1所在ip地址')
        string(name:'server2IP', defaultValue: 'IP2', description: '部署代码server2所在ip地址')
        string(name: 'proPath',defaultValue:'/opt/harbin', description: '生产服务路径')
        string(name: 'bakPath',defaultValue:'/opt/h5_bak', description: '生产备份路径')
}
    stages {
        stage ('拉取gitlab代码') {
            steps {
                git branch: 'master', credentialsId: '75a79a50-ea18-4fb9-9179-6d800118c85a', url: 'git@gitlabIP:BJ/haerbin-web.git'
            }
        }

        stage ('nodejs构建') {
            tools {
                nodejs 'NODEJS_V8.12'
            }

            steps {             
                sh "npm install && npm run build" 
                }
            }

        stage ('备份原代码'){
            steps{
                sh "ssh -f -n root@${params.server1IP} cp -a ${params.proPath} ${params.bakPath}/`date +%Y%m%d`+harbin"
                sh "ssh -f -n root@${params.server2IP} cp -a ${params.proPath} ${params.bakPath}/`date +%Y%m%d`+harbin"
            }
        }

        stage ('推送测试包'){
            steps {
                sh "scp -r dist/* root@${params.server1IP}:${params.proPath}"
                sh "scp -r dist/* root@${params.server2IP}:${params.proPath}"
            }
        }
    }
}