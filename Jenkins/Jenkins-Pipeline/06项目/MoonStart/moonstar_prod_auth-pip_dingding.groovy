pipeline {
    options {
        timestamps () 
        buildDiscarder logRotator(artifactDaysToKeepStr: '3', artifactNumToKeepStr: '1', daysToKeepStr: '3', numToKeepStr: '10')
    }
    agent any
    parameters {
        string(name:'server1IP', defaultValue: 'serverIP', description: '部署代码所在server1_ip地址')
        // string(name:'server2IP', defaultValue: '10.90.2.18', description: '部署代码所在server2_ip地址')
        string(name:'pomPath', defaultValue: 'plusplatform-auth/pom.xml', description: 'maven构建pom.xml')
        string(name:'jarPack', defaultValue: 'plusplatform-auth/plusplatform-auth-server/target/plusplatform-auth.jar', description: 'maven构建出的jar包')
        string(name: 'proPath',defaultValue:'/opt/plusplatform', description: '服务路径')
    }
    stages {
       stage ('拉取gitlab代码') {
         tools { git 'git1.8.3' }
         steps {
            git branch: 'master', credentialsId: '75a79a50-ea18-4fb9-9179-6d800118c85a', url: 'git@gitlabIP:plusplatform/moonstar.git'
        }
    }

    stage ('maven构建') {
        steps {
           withMaven(jdk: 'jdk1.8', maven: 'maven3', mavenSettingsFilePath: '/var/lib/jenkins/settings.xml', mavenLocalRepo: '/var/lib/jenkins/.m2/repository') { 
               sh "mvn -f ${params.pomPath} -Dmaven.test.skip=true clean install" 
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

        stage('推送生产包'){
           steps {
            sh "scp ${params.jarPack} root@${params.server1IP}:${params.proPath}"
            sh "scp /var/lib/ms root@${params.server1IP}:${params.proPath}"
            // sh "scp ${params.jarPack} root@${params.server2IP}:${params.proPath}"
            // sh "scp /var/lib/ms root@${params.server2IP}:${params.proPath}"
            }
        }

        stage ('重启服务') {
            steps {
                sh "ssh -f -n root@${params.server1IP} chmod +x ${params.proPath}/ms"
                sh "ssh -f -n root@${params.server1IP} ${params.proPath}/ms ${params.proPath}/${jarPackname} pro"
                // sh "ssh -f -n root@${params.server2IP} chmod +x ${params.proPath}/ms"
                // sh "ssh -f -n root@${params.server2IP} ${params.proPath}/ms ${params.proPath}/${jarPackname} pro"
            }
        }
  //       stage ('推送钉钉'){
  //         steps { 
  //           echo "开始钉钉推送消息"
		// }

        post {
            always {
             script {   
              if (currentBuild.currentResult=="SUCCESS"){
               dingTalk accessToken: 'dingtalkToken', imageUrl: 'https://i.imgur.com/MG0SSIl.png', jenkinsUrl: 'https://i.imgur.com/UYXymcf.png', message: '发布成功！不要点开，老陶请吃鸡', notifyPeople: ''
            }
              else {
               dingTalk accessToken: 'dingtalkToken', imageUrl: 'https://i.imgur.com/98KAEyj.png', jenkinsUrl: 'https://i.imgur.com/UYXymcf.png', message: '发布失败！不要点开，直接问分管运维', notifyPeople: ''
            }
        }
    }
}
}
}
}