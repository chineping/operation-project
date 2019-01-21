pipeline {
  agent any
  options { 
    timestamps () 
  }
  parameters {
    string(name:'server', defaultValue: 'server1IP,server2IP,/usr/local/ems-server/webapps,../ems_prod_pipeline/ems-server/target/ems-server.war', description: '生产PROD环境服务器ems-server')
    string(name:'backend', defaultValue: 'backend1IP,backend2IP,/usr/local/ems-web/webapps,../ems_prod_pipeline/ems-backend/target/ems-backend.war', description: '生产PROD环境服务器ems-backend')
    string(name:'web', defaultValue: 'webIP,/usr/local/tomcat_central/webapps,../ems_prod_pipeline/ems-web/target/ems-web.war,7788', description: '生产PROD环境服务器ems-web')
    string(name:'upload', defaultValue: 'uploadIP,/usr/local/upload/webapps,../ems_prod_pipeline/upload/target/upload.war', description: '生产PROD环境服务器ems-upload')
    string(name:'envelope', defaultValue: 'webIP,/data/ems-envelope/webapps,../ems_prod_pipeline/ems-envelope/target/ems-envelope.war,7788', description: '生产PROD环境服务器ems-envelope')
    booleanParam(defaultValue: false, name: 'ems_server', description: 'Process ems-server')
    booleanParam(defaultValue: false, name: 'ems_backend', description: 'Process ems-backend')
    booleanParam(defaultValue: false, name: 'ems_web', description: 'Process ems-web')
    booleanParam(defaultValue: false, name: 'ems_upload', description: 'Process ems_upload')
    booleanParam(defaultValue: false, name: 'ems_envelope', description: 'Process ems-envelope')
  }
  stages {
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
        echo 'Deploying to prod_ems-server'
        script {
          def split=params.server.split(",")
          proPath=split[2]
          mavenPath=split[3]
          for (int i = 0;i < 2;++i) {
              // scp ${mavenPath} root@\"${split[i]}\":/tmp
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-server|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/ems-server.war /usr/local/ems-server/war_bak/`date +%Y%m%d+`ems-server.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/ems-server*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              """
              // ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-server/bin/startup.sh
            }
          }
        }
      }
   stage ('发布服务：ems-backend') {
        when {
          expression { return params.ems_backend ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
        }
        steps {
          echo 'Deploying to prod_ems-backend'
          script {
            def split=params.backend.split(",")
            proPath=split[2]
            mavenPath=split[3]
            for (int i = 0;i < 2;++i) {
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-web|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/ems-backend.war /usr/local/ems-web/war_bak/`date +%Y%m%d+`ems-backend.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/ems-backend*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              """
              // ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-web/bin/startup.sh
            }
          }
          }}
   stage ('发布服务：ems-web') {
            when {
              expression { return params.ems_web ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
            }
            steps {
              echo 'Deploying to prod_ems-web'
              script {
                def split=params.web.split(",")
                proPath=split[1]
                mavenPath=split[2]
                port=split[3]
                for (int i = 0;i < 1;++i) {
                  def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" -p \"${port}\" ps -ef|grep tomcat_central|grep -v grep|awk '{print \$2}'"
                  pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
                  echo "you input pid is ${pid},to do sth"
                  sh """              
                  ssh -f -n root@\"${split[i]}\" -p \"${port}\" kill -9 ${pid}
                  ssh -f -n root@\"${split[i]}\" -p \"${port}\" mv ${proPath}/ems-web.war /data/ems-bak/`date +%Y%m%d+`ems-web.war
                  ssh -f -n root@\"${split[i]}\" -p \"${port}\" rm -fR ${proPath}/ems-web*
                  scp -P \"${port}\" ${mavenPath} root@\"${split[i]}\":${proPath}
                  """
                  // ssh -f -n root@\"${split[i]}\" -p \"${port}\" bash /usr/local/tomcat_central/bin/startup.sh
                }
              }
              }}
   stage ('发布服务：ems-upload') {
                when {
                  expression { return params.ems_upload ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
                }
                steps {
                  echo 'Deploying to prod_ems-upload'
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
                  }}
   stage ('发布服务：ems-envelope') {
        when {
          expression { return params.ems_envelope ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
        }
        steps {
        // echo "${params.ems_server}"
        echo 'Deploying to prod_ems-envelope'
        script {
          def split=params.envelope.split(",")
          proPath=split[1]
          mavenPath=split[2]
          port=split[3]
          for (int i = 0;i < 1;++i) {
              // scp ${mavenPath} root@\"${split[i]}\":/tmp
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" -p \"${port}\" ps -ef|grep envelope|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" -p \"${port}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" -p \"${port}\" mv ${proPath}/ems-envelope.war /data/ems-bak/`date +%Y%m%d+`ems-envelope.war
              ssh -f -n root@\"${split[i]}\" -p \"${port}\" rm -fR ${proPath}/ems-envelope*
              scp -P \"${port}\" ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" -p \"${port}\" bash /data/ems-envelope/bin/startup.sh
              """
            }
          }
        }
      }
    }
  }