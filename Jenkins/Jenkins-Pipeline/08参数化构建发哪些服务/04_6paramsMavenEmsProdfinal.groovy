pipeline {
  agent any
  options { 
    timestamps () 
  }
  parameters {
    string(name:'repoUrl', defaultValue: 'http://svnIP/svn/ems/trunk@HEAD', description: 'SVN代码路径')
    string(name:'pomPath', defaultValue: 'ems-pom/pom.xml', description: 'maven构建pom.xml')
    string(name:'conf', defaultValue: 'pro', description: 'pro/config.properties')
    string(name:'mavenSettingPath', defaultValue: '/opt/jenkins2.107/jenkins/conf/mps/settings.xml', description: 'maven构建settings.xml')
  }
  environment {
    CRED_ID='6d6075b3-1250-410a-93a9-0e12d671cd4d'
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
   stage ('dingtalk反馈') {
    steps {
      echo "dingtalk"
    }
      post {
        always {
         script {   
          if (currentBuild.currentResult=="SUCCESS"){
           dingTalk accessToken: 'dingtalkToken', imageUrl: 'https://i.imgur.com/MG0SSIl.png', jenkinsUrl: 'https://i.imgur.com/UYXymcf.png', message: '构建成功！不要点开', notifyPeople: ''
         }
         else {
           dingTalk accessToken: 'dingtalkToken', imageUrl: 'https://i.imgur.com/98KAEyj.png', jenkinsUrl: 'https://i.imgur.com/UYXymcf.png', message: '构建失败！不要点开，直接访问jenkins查看', notifyPeople: ''
         }
       }
     }
    }
   }
  }
}