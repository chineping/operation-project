pipeline {
  agent any
  options { 
    timestamps () 
  }
  parameters {
    string(name:'repoUrl', defaultValue: 'http://svnIP/svn/ems/trunk@HEAD', description: 'SVN代码路径')
    string(name:'pomPath', defaultValue: 'ems-pom/pom.xml', description: 'maven构建pom.xml')
    string(name:'conf', defaultValue: 'pro', description: 'pro/config.properties')
    string(name:'mavenSettingPath', defaultValue: '/var/lib/jenkins/conf/mps/settings.xml', description: 'maven构建settings.xml')
  }
  environment {
    CRED_ID='67486f7c-f59b-45b0-87b2-f063a8b570ad'
  }
  stages {
    stage ('Checkout') {
      steps {
        script {
          def scmVars = checkout ([$class: 'SubversionSCM', 
            filterChangelog: false, 
            ignoreDirPropChanges: false, 
            locations: [[credentialsId: CRED_ID, 
            depthOption: 'infinity', 
            ignoreExternalsOption: true, 
            local: '.', 
            remote: params.repoUrl]], 
            workspaceUpdater: [$class: 'UpdateUpdater']])
          svnversion = scmVars.SVN_REVISION
        }
        sh "echo 当前版本：${svnversion}"
      }
    }
    stage ('maven构建') {
      steps {
       withMaven(jdk: 'jdk1.7', maven: 'maven3.3.9') { 
         sh "mvn -f ${params.pomPath} -s ${params.mavenSettingPath} -Dmaven.test.skip=true clean install -U -P${params.conf}" 
       }
     }
   }
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
