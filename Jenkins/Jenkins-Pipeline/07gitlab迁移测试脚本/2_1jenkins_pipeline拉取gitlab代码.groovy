pipeline {
    options {
        timestamps () 
        buildDiscarder logRotator(artifactDaysToKeepStr: '3', artifactNumToKeepStr: '1', daysToKeepStr: '3', numToKeepStr: '10')
    }
    agent any
    parameters {
        string(name:'serverIP', defaultValue: 'serverIP', description: '部署代码所在ip地址')
        string(name: 'proPath',defaultValue:'/opt/bj-jiaotong/financial-manager/dist', description: '服务路径')
        string(name: 'bakPath',defaultValue:'/opt/bj-jiaotong/financial-manager/bak', description: '备份路径')
    }
    stages {
        stage ('拉取gitlab代码') {
                steps {
                    git branch: 'master', credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: 'git@gitlabIP:plusplatform/plusplatform.git'
                }
            }
        }

        // stage ('nodejs构建') {
        //     tools {
        //         nodejs 'NodeJS_V8.11.3'
        //     }

        //     steps {
        //         sh """
        //         npm --version && node --version
        //         npm --registry https://registry.npm.taobao.org install express
        //         npm install && npm run build 
        //         """
        //         }
        //     }

        // stage ('备份原代码'){
        //     agent {
        //        label 'serverIP'
        //     }

        //     steps{
        //         sh "mv ${params.proPath} ${params.bakPath}/`date +%Y%m%d`+jiaohang"
        //     }
        // }

        // stage ('推送测试包'){
        //     steps {
        //         sh "scp -r dist/ root@${params.serverIP}:${params.proPath}"
        //     }
        // }
    // }
}