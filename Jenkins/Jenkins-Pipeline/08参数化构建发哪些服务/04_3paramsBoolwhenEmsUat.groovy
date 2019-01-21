pipeline {
  agent any
  options { 
    timestamps () 
  }
  parameters {
    string(name:'repoUrl', defaultValue: 'http://svnIP/svn/ems/trunk@HEAD', description: 'SVN代码路径')
    string(name:'pomPath', defaultValue: 'ems-pom/pom.xml', description: 'maven构建pom.xml')
    string(name:'conf', defaultValue: 'uat', description: 'uat/config.properties')
    string(name:'mavenSettingPath', defaultValue: '/var/lib/jenkins/conf/mps/settings.xml', description: 'maven构建settings.xml')
    string(name:'server', defaultValue: 'serverIP,/usr/local/ems-server-1/webapps,ems-server/target/ems-server.war', description: 'UAT环境服务器ems-server')
    string(name:'backend', defaultValue: 'serverIP,/usr/local/ems-backend-1/webapps,ems-backend/target/ems-backend.war,/usr/local/ems-backend-2/webapps', description: 'UAT环境服务器ems-backend')
    string(name:'web', defaultValue: 'webIP,/usr/local/ems-web-www/webapps,ems-web/target/ems-web.war', description: 'UAT环境服务器ems-web')
    string(name:'upload', defaultValue: 'serverIP,/usr/local/upload/webapps,upload/target/upload.war', description: 'UAT环境服务器ems-upload')
    string(name:'envelope', defaultValue: 'serverIP,/data/ems-envelope/webapps,ems-envelope/target/ems-envelope.war', description: 'UAT环境服务器ems-envelope(暂无红包服务)')
    booleanParam(defaultValue: false, name: 'ems_server', description: 'Process ems-server')
    booleanParam(defaultValue: false, name: 'ems_backend', description: 'Process ems-backend')
    booleanParam(defaultValue: false, name: 'ems_web', description: 'Process ems-web')
    booleanParam(defaultValue: false, name: 'ems_upload', description: 'Process ems_upload')
    booleanParam(defaultValue: false, name: 'ems_envelope', description: 'Process ems-envelope')
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
        echo 'Deploying to uat_ems-server'
        script {
          def split=params.server.split(",")
          proPath=split[1]
          mavenPath=split[2]
          for (int i = 0;i < 1;++i) {
              // scp ${mavenPath} root@\"${split[i]}\":/tmp
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-server|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/ems-server.war /usr/local/ems-server-1/war_bak/`date +%Y%m%d+`ems-server.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/ems-server*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-server-1/bin/startup.sh
              """
            }
          }
        }
      }
   stage ('发布服务：ems-backend') {
      when {
        expression { return params.ems_backend ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
      }
      steps {
        echo 'Deploying to uat_ems-backend'
        script {
          def split=params.backend.split(",")
          proPath=split[1]
          mavenPath=split[2]
          proPath2=split[3]
          for (int i = 0;i < 1;++i) {
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-backend|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/ems-backend.war /usr/local/ems-backend-1/war_bak/`date +%Y%m%d+`ems-backend.war
              ssh -f -n root@\"${split[i]}\" mv ${proPath2}/ems-backend.war /usr/local/ems-backend-2/war_bak/`date +%Y%m%d+`ems-backend.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/ems-backend*
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath2}/ems-backend*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              scp ${mavenPath} root@\"${split[i]}\":${proPath2}
              ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-backend-1/bin/startup.sh
              ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-backend-2/bin/startup.sh
              """
            }
          }
        }
      }
   stage ('发布服务：ems-web') {
      when {
        expression { return params.ems_web ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
      }
      steps {
        echo 'Deploying to uat_ems-web'
        script {
          def split=params.web.split(",")
          proPath=split[1]
          mavenPath=split[2]
          for (int i = 0;i < 1;++i) {
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-web-www|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/ems-web.war /usr/local/ems-web-www/war_bak/`date +%Y%m%d+`ems-web.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/ems-web*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-web-www/bin/startup.sh
              """
            }
          }
        }
      }
   stage ('发布服务：ems-upload') {
      when {
        expression { return params.ems_upload ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
      }
      steps {
        echo 'Deploying to uat_ems-upload'
        script {
          def split=params.upload.split(",")
          proPath=split[1]
          mavenPath=split[2]
          for (int i = 0;i < 1;++i) {
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep upload|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/upload.war /usr/local/upload/war_bak/`date +%Y%m%d+`upload.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/upload*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" bash /usr/local/upload/bin/startup.sh
              """
            }
          }
        }
      }
   stage ('发布服务：ems-envelope') {
      when {
        expression { return params.ems_envelope ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
      }
      steps {
        echo 'Deploying to uat_ems-envelope'
        script {
          def split=params.envelope.split(",")
          proPath=split[1]
          mavenPath=split[2]
          for (int i = 0;i < 1;++i) {
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-envelope|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/ems-envelope.war /data/ems-envelope/war_bak/`date +%Y%m%d+`ems-envelope.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/ems-envelope*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" bash /data/ems-envelope/bin/startup.sh
              """
            }
          }
        }
      }
    }
  }