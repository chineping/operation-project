pipeline {
    agent any
    tools {
        maven 'maven3'
        jdk 'jdk1.8'
    }
    parameters {
        string(name:'serverIP', defaultValue: 'IP', description: '部署代码所在ip地址')
        string(name:'pomPath', defaultValue: 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/pom.xml', description: 'maven构建pom.xml')
        string(name:'jarPack', defaultValue: 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/target/plusplatform-product-harb-1.0.0-SNAPSHOT.jar', description: 'maven构建出的jar包')
        string(name: 'proPath',defaultValue:'/opt/plusplatform', description: '服务路径')
}
    stages {
        stage ('拉取gitlab代码') {
            steps {
                git branch: 'bj_dev', credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: 'git@gitlabIP:plusplatform/plusplatform.git'
            }
        }

        stage ('maven构建') {
            steps {
                sh "mvn -f ${params.pomPath} -Dmaven.test.skip=true clean install"
            }
        }

        stage ('获取包名') {
            steps {
                script {
                    def split=params.jarPack.split("/")
                    jarPackname=split[-1]
                }
            }
        }

        stage ('输出包名') {
            steps {
                sh "echo ${jarPackname}"
            }
        }

        stage('推送测试包'){
            steps {
                sh "scp ${params.jarPack} root@${params.serverIP}:${params.proPath}"
                sh "scp /var/lib/ms root@${params.serverIP}:${params.proPath}"
                }
        }

        stage('重启服务'){
            steps{
                sh "ssh -f -n root@${params.serverIP} chmod +x ${params.proPath}/ms"
                sh "ssh -f -n root@${params.serverIP} ${params.proPath}/ms ${params.proPath}/${jarPackname}"
            }
        }
    }
}