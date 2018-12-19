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
        string(name:'codeReg', defaultValue: 'smart-quizrule-persistence,smart-quizrule-service,smart-quizrule-manager,smart-quizrule-worker', description: 'smart-quizrule代码')
        string(name:'WarPack', defaultValue: 'smart-quizrule-manager/build/libs/smart-quizrule-manager-2.1.1-SNAPSHOT.war,smart-quizrule-worker/build/libs/smart-quizrule-worker-2.1.1-SNAPSHOT.war', description: 'gradle构建出的服务war包')
        string(name: 'proPath',defaultValue:'/opt/plusplatform', description: '服务路径')
        string(name: 'logPath',defaultValue:'/opt/plusplatform/logs', description: '日志路径')
        string(name: 'bakPath',defaultValue:'/opt/plusplatform/bak', description: '备份路径')
    }
    stages {
        stage ('分别拉取代码库') {
            steps {
                script {
                    def split=params.codeReg.split(",")
                    for (int i = 0;i < 4;++i) {
                        // echo split[i]
                        // echo "git@172.26.22.10:smart/smart-quizrule/${split[i]}.git"
                        echo "Checking out ${split[i]}"
                        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, 
                            extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "${split[i]}"]], submoduleCfg: [], 
                            userRemoteConfigs: [[credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: "git@gitlabIP:smart/smart-quizrule/${split[i]}.git"]]])
                    }
                }   
            }
        }

        stage ('gradle构建') {
            tools {
                    gradle 'gradle_4.8'
                }

            steps {
                script {
                    def split=params.codeReg.split(",")
                    for (int i = 0;i < 2;++i) {
                        dir ("${split[i]}") {
                            sh "gradle bjar -b publish.gradle"
                        }
                    }

                    for (int i = 2;i < 4;++i) {
                        dir ("${split[i]}") {
                            sh "gradle bootWar -b publish.gradle"
                        }
                    }
                }       
            }
        }

        stage ('获取包名、agentName') {
            steps {
                script {
                    def split=params.WarPack.split("/")
                    managerWarPackname=split[3].split(",")[0]
                    workerWarPackname=split[-1]
                    agentName = "${params.serverIP}"
                    def path=params.WarPack.split(",")
                    managerWarPackPath=path[0]
                    workerWarPackPath=path[1]
                }
            }
        }

        stage ('输出包名') {
            steps {
                echo "${managerWarPackname}"
                echo "${workerWarPackname}"
            }
        }

        stage ('停服务备份生产包') {
            steps {
                node ( agentLabel as String ) {  // Evaluate the node label later
                    sh returnStatus: true, script: "stop quizrule"
                    script {
                        def pid = sh returnStdout: true ,script: "ps -ef|grep quizrule|grep -v grep|awk '{print \$2}'"
                        pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " "); 
                        // pid = pid.trim()
                        echo "you input pid is ${pid},to do sth"
                        sh "kill -9 ${pid}"
                    }
                    sh "mv ${params.proPath}/${managerWarPackname} ${params.bakPath}/`date +%Y%m%d-%H%M%S`+${managerWarPackname}"
                    sh "mv ${params.proPath}/${workerWarPackname} ${params.bakPath}/`date +%Y%m%d-%H%M%S`+${workerWarPackname}" 
                }
            }
        }
            
        stage ('推送测试包') {
            steps {
                sh """
                scp ${managerWarPackPath} root@${params.serverIP}:${params.proPath}
                scp ${workerWarPackPath} root@${params.serverIP}:${params.proPath}
                """
            }
        }

        stage ('重启服务') {
            steps {
                node ( agentLabel as String ) {
                    script {
                        withEnv (['JENKINS_NODE_COOKIE=dontkillme']) {
                            sh """
                            nohup java -Xms2048m -Xmx2048m -XX:PermSize=128M -XX:MaxPermSize=256M -jar ${params.proPath}/${managerWarPackname} --spring.profiles.active=sit > ${params.logPath}/${managerWarPackname}.log 2>&1 &
                            nohup java -Xms2048m -Xmx2048m -XX:PermSize=128M -XX:MaxPermSize=256M -jar ${params.proPath}/${workerWarPackname} --spring.profiles.active=sit > ${params.logPath}/${workerWarPackname}.log 2>&1 &
                            """
                        }
                    }  
                } 
            }
        }
    }
}