pipeline {
  agent any
  options { 
    timestamps () 
  }
  parameters {
    string(name:'repoUrl', defaultValue: 'http://svnIP/svn/ems/trunk@HEAD', description: 'SVN代码路径')
    string(name:'pomPath', defaultValue: 'ems-pom/pom.xml', description: 'maven构建pom.xml')
    string(name:'mavenSettingPath', defaultValue: '/var/lib/jenkins/conf/mps/settings.xml', description: 'maven构建settings.xml')
    booleanParam(defaultValue: false, name: 'OPTION_1', description: 'Process ems-server')
    booleanParam(defaultValue: false, name: 'OPTION_2', description: 'Process ems-backend')
    booleanParam(defaultValue: false, name: 'OPTION_3', description: 'Process ems-web')
    booleanParam(defaultValue: false, name: 'OPTION_4', description: 'Process upload')
    booleanParam(defaultValue: false, name: 'OPTION_5', description: 'Process ems-envelope')
  }
  environment {
    CRED_ID='67486f7c-f59b-45b0-87b2-f063a8b570ad'
  }
  stages {
   //  stage ('Checkout') {
   //    steps {
   //      script {
   //        def scmVars = checkout ([$class: 'SubversionSCM', 
   //          filterChangelog: false, 
   //          ignoreDirPropChanges: false, 
   //          locations: [[credentialsId: CRED_ID, 
   //          depthOption: 'infinity', 
   //          ignoreExternalsOption: true, 
   //          local: '.', 
   //          remote: params.repoUrl]], 
   //          workspaceUpdater: [$class: 'UpdateUpdater']])
   //        svnversion = scmVars.SVN_REVISION
   //      }
   //      sh "echo 当前版本：${svnversion}"
   //    }
   //  }
   //  stage ('maven构建') {
   //    steps {
   //     withMaven(jdk: 'jdk1.7', maven: 'maven3.3.9') { 
   //       sh "mvn -f ${params.pomPath} -s ${params.mavenSettingPath} -Dmaven.test.skip=true clean install -U -Pnsit" 
   //     }
   //   }
   // }
   stage ('推送测试包') {
    steps {
      sh "echo 当前发布服务为：${params.OPTION_1}"
      script {
        if ( "${params.OPTION_1}"=="true" ) {
          sh "echo 当前发布服务为：ems-server"
        } 
        else if ( "${params.OPTION_2}"=="true" ) {
          sh "echo 当前发布服务为：ems-backend"
        }
        else if ( "${params.OPTION_3}"=="true" ) {
          sh "echo 当前发布服务为：ems-web" 
        }
        else if ( "${params.OPTION_4}"=="true" ) {
          sh "echo 当前发布服务为：upload"
        }
        else if ( "${params.OPTION_5}"=="true" ) {
          sh "echo 当前发布服务为：ems-envelope"
        }
        else {
          sh "echo 当前发布服务为：ems-server、ems-backend、ems-web、upload及ems-envelope"
        }
        }
      }
    }
  }
}