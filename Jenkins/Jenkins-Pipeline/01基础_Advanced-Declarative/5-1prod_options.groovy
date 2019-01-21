pipeline {
    options {
        timestamps () 
        buildDiscarder logRotator(artifactDaysToKeepStr: '3', artifactNumToKeepStr: '1', daysToKeepStr: '3', numToKeepStr: '10')
    }
    agent any
    parameters {
        string(name:'server1IP', defaultValue: 'IP1', description: '部署代码所在server1_ip地址')
        string(name:'server2IP', defaultValue: 'IP2', description: '部署代码所在server2_ip地址')
        string(name:'pomPath', defaultValue: 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/pom.xml', description: 'maven构建pom.xml')
        string(name:'jarPack', defaultValue: 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/target/plusplatform-product-harb-1.0.0-SNAPSHOT.jar', description: 'maven构建出的jar包')
        string(name: 'proPath',defaultValue:'/opt/plusplatform', description: '服务路径')
        string(name: 'bakPath',defaultValue:'/opt/plusplatform/jar_bak', description: '备份路径')
    }
    stages {
        stage ('拉取gitlab代码') {
            steps {
                git branch: 'master', credentialsId: '75a79a50-ea18-4fb9-9179-6d800118c85a', url: 'git@gitlabIP:plusplatform/plusplatform.git'
            }
        }

        stage ('maven构建') {
            steps {
                // withMaven(jdk: 'jdk1.8', maven: 'maven3', mavenSettingsFilePath: '/opt/apache-maven-3.5.3/conf/settings.xml', mavenLocalRepo: '.repository') {
                withMaven(jdk: 'jdk1.8', maven: 'maven3', mavenSettingsFilePath: '/var/lib/jenkins/settings.xml', mavenLocalRepo: '/var/lib/jenkins/.m2/repository') {
                    // sh "mvn -f ${params.pomPath} -Dmaven.repo.local -Dmaven.test.skip=true clean install"
                    sh "mvn -f ${params.pomPath} -Dmaven.test.skip=true clean install"
                    // sh "mvn -Dmaven.test.skip=true clean install"
                }
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

        // stage ('输出包名') {
        //     steps {
        //         sh "echo ${jarPackname}"
        //     }
        // }
        
        stage ('备份生产包') {
            steps {
                // sh "ssh -f -n root@${params.server1IP} mkdir ${params.bakPath}"
                sh "ssh -f -n root@${params.server1IP} mv ${params.proPath}/${jarPackname} ${params.bakPath}/`date +%Y%m%d-%H%M%S`+${jarPackname}"
                // sh "ssh -f -n root@${params.server2IP} mkdir ${params.bakPath}"
                sh "ssh -f -n root@${params.server2IP} mv ${params.proPath}/${jarPackname} ${params.bakPath}/`date +%Y%m%d-%H%M%S`+${jarPackname}"
                }
        }

        stage ('推送生产包') {
            steps {
                sh "scp ${params.jarPack} root@${params.server1IP}:${params.proPath}"
                // sh "scp /var/lib/ms root@${params.server1IP}:${params.proPath}"
                sh "scp ${params.jarPack} root@${params.server2IP}:${params.proPath}"
                // sh "scp /var/lib/ms root@${params.server2IP}:${params.proPath}"
                }
        }

        stage ('重启服务') {
            steps {
                // sh "ssh -f -n root@${params.server1IP} chmod +x ${params.proPath}/ms"
                sh "ssh -f -n root@${params.server1IP} ${params.proPath}/ms ${params.proPath}/${jarPackname} pro"
                // sh "ssh -f -n root@${params.server2IP} chmod +x ${params.proPath}/ms"
                sh "ssh -f -n root@${params.server2IP} ${params.proPath}/ms ${params.proPath}/${jarPackname} pro"
            }
        }
    }
}