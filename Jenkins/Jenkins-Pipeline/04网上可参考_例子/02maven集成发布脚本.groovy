#!/usr/bin/env groovy

pipeline {
    //确认使用主机/节点机
    agent any /*{ 
        node { label ' master'} 
    }*/
    //  声明参数 
    parameters{
        //SVN代码路径
        string(name:'repoUrl', defaultValue: 'http://svn.com/svn/server/job', description: ' SVN代码路径')
        // 部署内容的相对路径
        string(name:'deployLocation', defaultValue: 'target/*.jar,target/alternateLocation/*.*,'+'target/classes/*.*,target/classes/i18n/*.*,target/classes/rawSQL/*.*,'+'target/classes/rawSQL/mapper/*.*,target/classes/rawSQL/mysql/*.*,'+'target/classes/rawSQL/sqlserver/*.*', description: '部署内容的相对路径 ')
        //服务器参数采用了组合方式，避免多次选择
        string(name:'dev_server', defaultValue: 'IP,Port,Name,Passwd', description: '开发服务器(IP,Port,Name,Passwd)')
        string(name:'ZHtest_server', defaultValue: 'IP,Port,Name,Passwd', description: '中文测试服务器(IP,Port,Name,Passwd)')
        string(name:'alT19_server', defaultValue: 'IP,Port,Name,Passwd', description: ' 生产服务器T1(IP,Port,Name,Passwd)')
        string(name:'alT20_server', defaultValue: 'IP,Port,Name,Passwd', description: ' 生产服务器T2(IP,Port,Name,Passwd)')
    }
    // 声明使用的工具 
    tools {
        maven 'maven'
        jdk   'jdk1.8'
    }
    //常量参数，初始确定后一般不需更改
    environment{
        // SVN服务全系统只读账号cred_id【参数值对外隐藏】
        CRED_ID='CRED_ID'
        //项目经理邮箱地址
        PM_EMAIL='PM'
        // Jenkins负责人
        JM_EMAIL='QA'
        //测试人员邮箱地址【参数值对外隐藏】
        TEST_EMAIL='Tester'
    }
    triggers {
        pollSCM('H/5 * * * 1-5')
    }
    //pipeline运行结果通知给触发者
    post{
        //执行后清理workspace
        always{
            echo "clear workspace......"
            deleteDir()
        }
        failure{
            script { 
                emailext body: '${JELLY_SCRIPT,template="static-analysis"}', 
                recipientProviders: [[$class: 'RequesterRecipientProvider'],[$class: 'DevelopersRecipientProvider']], 
                subject: '${JOB_NAME}- Build # ${BUILD_NUMBER} - Failure!'
            }
        }
    }

    stages {
        stage('清理本地仓库') {
            steps{
                sh "/home/jenkins/del_lastUpdated.sh"
            }
        }
        stage('Checkout') {
            steps {
                script {
                //从SVN拉取代码
                def scmVars = checkout ([$class: 'SubversionSCM', 
                additionalCredentials: [],
                excludedCommitMessages: '', 
                excludedRegions: '', 
                excludedRevprop: '', 
                excludedUsers: '', 
                filterChangelog: false, 
                ignoreDirPropChanges: false, 
                includedRegions: '', 
                locations: [[credentialsId: CRED_ID, 
                    depthOption: 'infinity', 
                    ignoreExternalsOption: true, 
                    local: '.', 
                    remote: params.repoUrl]], 
                    workspaceUpdater: [$class: 'UpdateUpdater']])
                svnversion = scmVars.SVN_REVISION
                }
                //sh "echo ${svnversion}"
            }
        }
        // 编译构建代码
        stage('构建') {
            steps{
                // maven构建
                sh "mvn -Dmaven.test.failure.ignore clean install"
            }
        }
        stage('静态检查') {
            steps {
                echo "starting codeAnalyze with SonarQube......"
                //sonar:sonar.QualityGate should pass
                withSonarQubeEnv('Sonar-6.4') {
                  //固定使用项目根目录${basedir}下的pom.xml进行代码检查
                  //sh "mvn -f pom.xml clean compile sonar:sonar"
                  sh "mvn sonar:sonar "+
                  "-Dsonar.sourceEncoding=UTF-8 "//+
                  //"-Dsonar.language=java,groovy,xml"+
                  //"-Dsonar.projectVersion=${v} "+
                  //"-Dsonar.projectKey=${JOB_NAME} "+
                  //"-Dsonar.projectName=${JOB_NAME}"
                }
                script {
                //  未通过代码检查，中断 
                timeout(10) { 
                    //利用sonar webhook功能通知pipeline代码检测结果，未通过质量阈，pipeline将会fail
                    def qg = waitForQualityGate() 
                        if (qg.status != 'OK') {
                            error "未通过Sonarqube的代码质量阈检查，请及时修改！failure: ${qg.status}"
                        }
                    }
                }
            }
        }
        stage('归档') {
            steps {
                // 归档文件
                /*archiveArtifacts artifacts: 'target/*.jar,target/alternateLocation/*.*,'+'target/classes/*.*,target/classes/i18n/*.*,target/classes/rawSQL/*.*,'+'target/classes/rawSQL/mapper/*.*,target/classes/rawSQL/mysql/*.*,'+'target/classes/rawSQL/sqlserver/*.*',fingerprint: true*/
                archiveArtifacts params.deployLocation
            }
        }
        stage('部署到开发环境 ') {
            steps {
                //根据param.server分割获取参数,包括IP,jettyPort,username,password
                script {
                    def dev_split=params.dev_server.split(",")
                    dev_serverIP=dev_split[0]
                    dev_serverPort=dev_split[1]
                    dev_serverName=dev_split[2]
                    dev_serverPasswd=dev_split[3]
                }
                echo 'Deploying to dev_server'
                //清理清理旧程序
                sh "/home/jenkins/del_158_client.sh 'bas'"
                // 部署到开发环境
                sh "scp -r target/*.jar ${dev_serverName}@${dev_serverIP}:/jenkins/datacenter/bas/"
                sh "scp -r target/alternateLocation ${dev_serverName}@${dev_serverIP}:/jenkins/datacenter/bas/"
                sh "rsync -av target/classes/ --exclude=com ${dev_serverName}@${dev_serverIP}:/jenkins/datacenter/bas/"
                
                //  重启服务
                sh "/home/jenkins/kill_158_client.sh bas-job bas"
            }
        }
        stage('开发环境接口自动化测试') {
            agent{
                label 'Slave_Linux_69_2'
            }
            steps{
                sh "sleep 60s"
                echo "starting interfaceTest......"
                /*echo ' 节点是: ${env.NODE_NAME}'
                echo ' 节点是: ${env.NODE_LABELS}'
                echo '${currentBuild}'
                echo '${env}'
                echo " 当前BuildId： ${env.BUILD_ID}"*/
                dir('/home/jenkins/pm_test')
                {
                    sh '(source /etc/profile;newman -c APD201test_bas.postman_collection.json)'
                }
            }
        }
        stage('对当前版本代码打tag') {
            steps{
                timeout(5) {
                    script {
                        input message:' 需要打tag嘛？'
                    }
                }
                //sh "echo ${params.repoUrl}"
                //sh "echo ${svnversion}"
                sh "/home/jenkins/del_crea_tag.sh bas-job ${params.repoUrl} ${svnversion}"
            }
        }
        stage('确认是否部署到测试环境') {
            steps{
                timeout(5) {
                    script {
                        mail to: "${JM_EMAIL} ${PM_EMAIL}",
                        subject: "PineLine '${JOB_NAME}' (${BUILD_NUMBER})人工验收通知",
                        body: "提交的PineLine '${JOB_NAME}' (${BUILD_NUMBER})进入人工验收环节\n请及时前往${env.BUILD_URL}进行测试验收"
                        input message:'部署到测试环境？'//,submitter:"${PM_EMAIL}"
                        // 中文环境 
                        def ZHtest_split=params.ZHtest_server.split(",")
                        ZHtest_serverIP=ZHtest_split[0]
                        ZHtest_serverPort=ZHtest_split[1]
                        ZHtest_serverName=ZHtest_split[2]
                        ZHtest_serverPasswd=ZHtest_split[3]
                    }
                }
                //中文测试环境
                //清理清理旧程序
                sh "/home/jenkins/del_32_client.sh 'bas'"
                // 部署到中文测试环境
                sh "scp -r target/*.jar ${ZHtest_serverName}@${ZHtest_serverIP}:/jenkins/datacenter/bas/"
                sh "scp -r target/alternateLocation ${ZHtest_serverName}@${ZHtest_serverIP}:/jenkins/datacenter/bas/"
                sh "rsync -av target/classes/ --exclude=com ${ZHtest_serverName}@${ZHtest_serverIP}:/jenkins/datacenter/bas/"
            }
        }
        stage('部署到测试环境') {
            steps{
                echo 'Deploying to ZHtest_server'
                
                //  重启服务
                sh "/home/jenkins/kill_32_client.sh bas-job bas"
            }
        }
        stage('测试环境接口自动化测试') {
            agent{
                label 'Slave_Linux_69_2'
            }
            steps{
                //sh "sleep 60s"
                echo "starting interfaceTest......"
                dir('/home/jenkins/pm_test')
                {
                    sh '(source /etc/profile;newman -c API_test.json)'
                }
            }
        }
        stage('是否发布到生产环境？') {
            steps{
                timeout(10) {
                    script {
                        mail to: "${JM_EMAIL} ${PM_EMAIL}",
                        subject: "PineLine '${JOB_NAME}' (${BUILD_NUMBER})发布生产环境通知",
                        body: "提交的PineLine '${JOB_NAME}' (${BUILD_NUMBER})进入生产环境部署\n请及时前往${env.BUILD_URL}进行确认"
                        input message:'部署到生产环境？',submitter:"${PM_EMAIL}"
                        // 中文环境 
                        def ZHtest_split=params.ZHtest_server.split(",")
                        ZHtest_serverIP=ZHtest_split[0]
                        ZHtest_serverPort=ZHtest_split[1]
                        ZHtest_serverName=ZHtest_split[2]
                        ZHtest_serverPasswd=ZHtest_split[3]
                        // 英文环境 
                        def UStest_split=params.UStest_server.split(",")
                        UStest_serverIP=UStest_split[0]
                        UStest_serverPort=UStest_split[1]
                        UStest_serverName=UStest_split[2]
                        UStest_serverPasswd=UStest_split[3]
                    }
                }
                //文件服务环境
                //清理清理旧程序
                sh "/home/jenkins/del_file_client.sh 'bas'"
                sh "scp -r target/*.jar ${file_serverName}@${file_serverIP}:/data/datacenter/bas/"
                sh "scp -r target/alternateLocation ${file_serverName}@${file_serverIP}:/data/datacenter/bas/"
                sh "rsync -av target/classes/ --exclude=com ${file_serverName}@${file_serverIP}:/data/datacenter/bas/"

            }
        }
        stage('部署生产环境') {
            parallel {
                stage('中文环境') {
                    steps{
                        //sh "sleep 60s"
                        echo "starting  Deploy Chinese_Server......"
                    }
                }
                stage('英文环境') {
                    steps{
                        //sh "sleep 60s"
                        echo "starting Deploy English_Server......"
                    }
                }
            }
        }
    }   
}