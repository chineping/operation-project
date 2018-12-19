agentName = "sit"
agentLabel = "${-> println 'Right Now the Agent Name is ' + agentName; return agentName}"
pipeline {
    options {
        timestamps () 
        buildDiscarder logRotator(artifactDaysToKeepStr: '3', artifactNumToKeepStr: '1', daysToKeepStr: '3', numToKeepStr: '10')
    }
    agent any
    parameters {
        string(name:'serverIP', defaultValue: 'IP', description: '部署代码所在ip地址')
        string(name:'WarPack', defaultValue: 'build/libs/smart-services-finance-ledger-0.1.1.jar', description: 'gradle构建出的服务jar包')
        string(name: 'proPath',defaultValue:'/opt/bj-jiaohang', description: '服务路径')
        string(name: 'logPath',defaultValue:'/opt/bj-jiaohang/logs', description: '日志路径')
        string(name: 'bakPath',defaultValue:'/opt/bj-jiaohang/bak', description: '备份路径')
    }
    stages {
        stage ('拉取代码库') {
            steps {
                git branch: 'master', credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: 'git@gitlabIP:smart/smart-services/smart-services-finance-ledger.git'
            }
        }

        stage ('gradle构建') {
            tools {
                    gradle 'gradle_4.8'
                }

            steps {
                sh "gradle bootjar"      
            }
        }

        stage ('获取包名、agentName') {
            steps {
                script {
                    def split=params.WarPack.split("/")
                    WarPackname=split[-1]
                    agentName = "${params.serverIP}"
                }
            }
        }

        stage ('输出包名') {
            steps {
                echo "${WarPackname}"
            }
        }

        stage ('停服务备份生产包') {
            steps {
                node ( agentLabel as String ) {  // Evaluate the node label later
                    sh returnStatus: true, script: "stop finance-ledger"
                    script {
                        def pid = sh returnStdout: true ,script: "ps -ef|grep finance-ledger|grep -v grep|awk '{print \$2}'"
                        pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " "); 
                        // pid = pid.trim()
                        echo "you input pid is ${pid},to do sth"
                        sh "kill -9 ${pid}"
                    }
                    sh "mv ${params.proPath}/${WarPackname} ${params.bakPath}/`date +%Y%m%d-%H%M%S`+${WarPackname}"
                }
            }
        }
            
        stage ('推送测试包') {
            steps {
                sh """
                scp ${params.WarPack} root@${params.serverIP}:${params.proPath}
                """
            }
        }

        stage ('重启服务') {
            steps {
                node ( agentLabel as String ) {
                    script {
                        withEnv (['JENKINS_NODE_COOKIE=dontkillme']) {
                            sh """
                            nohup java -Xms128m -Xmx512m -XX:PermSize=64M -XX:MaxPermSize=128M -jar ${params.proPath}/${WarPackname} --spring.profiles.active=pro > ${params.logPath}/${WarPackname}.log 2>&1 &
                            """
                        }
                    }  
                } 
            }
        }
    }
}