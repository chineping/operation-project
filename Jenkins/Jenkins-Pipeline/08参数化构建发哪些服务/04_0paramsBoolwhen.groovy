pipeline {
  agent any
  options { 
    timestamps () 
  }
  parameters {
    string(name:'repoUrl', defaultValue: 'http://svnIP/svn/ems/trunk@HEAD', description: 'SVN代码路径')
    string(name:'pomPath', defaultValue: 'ems-pom/pom.xml', description: 'maven构建pom.xml')
    string(name:'mavenSettingPath', defaultValue: '/var/lib/jenkins/conf/mps/settings.xml', description: 'maven构建settings.xml')
    string(name:'server', defaultValue: 'server1IP,server2IP,/usr/local/ems-server/webapps,/var/lib/jenkins/workspace/ems_sit/ems-server/target/ems-server.war', description: '新SIT环境服务器ems-server')
    booleanParam(defaultValue: false, name: 'ems_server', description: 'Process ems-server')
    booleanParam(defaultValue: false, name: 'ems_backend', description: 'Process ems-backend')
    booleanParam(defaultValue: false, name: 'ems_web', description: 'Process ems-web')
    booleanParam(defaultValue: false, name: 'upload', description: 'Process upload')
    booleanParam(defaultValue: false, name: 'ems_envelope', description: 'Process ems-envelope')
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
   stage ('发布服务：ems-server') {
      // when {
      //   expression { return params.ems_server ==~ /(?i)(N|NO|F|FALSE|OFF|STOP)/ }
      // }
      // steps {
      //   echo "${params.ems_server}"
      // }
      when {
        expression { return params.ems_server ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
      }
      steps {
        // echo "${params.ems_server}"
        echo 'Deploying to new-sit_ems-server'
        script {
          def split=params.server.split(",")
              proPath=split[2]
              mavenPath=split[3]
            for (int i = 0;i < 2;++i) {
              // scp ${mavenPath} root@\"${split[i]}\":/tmp
              sh """              
              ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-server|grep -v grep|awk '{print $2}'|xargs kill -9
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/ems-server.war /usr/local/ems-server/war_bak/`date +%Y%m%d+`ems-server.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/ems-server*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-server/bin/startup.sh
              """
          }
        }
      }
    }
  }
}