pipeline {
  agent any
  options { 
    timestamps () 
  }
  parameters {
    string(name:'server', defaultValue: 'serverIP,/usr/local/ems-server/webapps/ems-server.war,../ems_prod_pipeline/ems-server/target/ems-server.war', description: '生产PROD环境服务器ems-server')
    string(name:'backend', defaultValue: 'backendIP,/usr/local/ems-web/webapps/ems-backend.war,../ems_prod_pipeline/ems-backend/target/ems-backend.war', description: '生产PROD环境服务器ems-backend')
    string(name:'web', defaultValue: 'webIP,/usr/local/tomcat_central/webapps/ems-web.war,../ems_prod_pipeline/ems-web/target/ems-web.war,7788', description: '生产PROD环境服务器ems-web')
    string(name:'upload', defaultValue: 'uploadIP,/usr/local/upload/webapps/upload.war,../ems_prod_pipeline/upload/target/upload.war', description: '生产PROD环境服务器ems-upload')
    string(name:'envelope', defaultValue: 'webIP,/data/ems-envelope/webapps/ems-envelope.war,../ems_prod_pipeline/ems-envelope/target/ems-envelope.war,7788', description: '生产PROD环境服务器ems-envelope')
    string(name:'tmpPath', defaultValue: '/opt/projectVersion/avsVersion', description: 'jenkinsTmp临时存放目录')
    booleanParam(defaultValue: false, name: 'ems_server', description: 'Process ems-server')
    booleanParam(defaultValue: false, name: 'ems_backend', description: 'Process ems-backend')
    booleanParam(defaultValue: false, name: 'ems_web', description: 'Process ems-web')
    booleanParam(defaultValue: false, name: 'ems_upload', description: 'Process ems_upload')
    booleanParam(defaultValue: false, name: 'ems_envelope', description: 'Process ems-envelope')
  }
  stages {
    stage ('获取服务包：ems-server') {
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
        echo 'GettingPackage from prod_ems-server'
        script {
          def split=params.server.split(",")
          serverIP=split[0]
          proPath=split[1]
          mavenPath=split[2]
          sh """
          mkdir ${params.tmpPath}/`date +%Y%m%d`/{OnlineProduction,verifyProdPackage} -pv
          cp -a ${mavenPath} ${params.tmpPath}/`date +%Y%m%d`/verifyProdPackage
          scp -o StrictHostKeyChecking=no root@${serverIP}:${proPath} ${params.tmpPath}/`date +%Y%m%d`/OnlineProduction            
          """          
        }
      }
    }
    stage ('获取服务包：ems-backend') {
      when {
        expression { return params.ems_backend ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
      }
      steps {
        echo 'GettingPackage from prod_ems-backend'
        script {
          def split=params.backend.split(",")
          serverIP=split[0]
          proPath=split[1]
          mavenPath=split[2]
          sh """
          mkdir ${params.tmpPath}/`date +%Y%m%d`/{OnlineProduction,verifyProdPackage} -pv
          cp -a ${mavenPath} ${params.tmpPath}/`date +%Y%m%d`/verifyProdPackage
          scp -o StrictHostKeyChecking=no root@${serverIP}:${proPath} ${params.tmpPath}/`date +%Y%m%d`/OnlineProduction            
          """  
        }
      }
    }
    stage ('获取服务包：ems-web') {
      when {
        expression { return params.ems_web ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
      }
      steps {
        echo 'GettingPackage from prod_ems-web'
        script {
          def split=params.web.split(",")
          serverIP=split[0]
          proPath=split[1]
          mavenPath=split[2]
          port=split[3]
          sh """
          mkdir ${params.tmpPath}/`date +%Y%m%d`/{OnlineProduction,verifyProdPackage} -pv
          cp -a ${mavenPath} ${params.tmpPath}/`date +%Y%m%d`/verifyProdPackage
          scp -o StrictHostKeyChecking=no -P ${port} root@${serverIP}:${proPath} ${params.tmpPath}/`date +%Y%m%d`/OnlineProduction            
          """ 
        }
      }
    }
    stage ('获取服务包：ems-upload') {
      when {
        expression { return params.ems_upload ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
      }
      steps {
        echo 'GettingPackage from prod_ems-upload'
        script {
          def split=params.upload.split(",")
          serverIP=split[0]
          proPath=split[1]
          mavenPath=split[2]
          sh """
          mkdir ${params.tmpPath}/`date +%Y%m%d`/{OnlineProduction,verifyProdPackage} -pv
          cp -a ${mavenPath} ${params.tmpPath}/`date +%Y%m%d`/verifyProdPackage
          scp -o StrictHostKeyChecking=no root@${serverIP}:${proPath} ${params.tmpPath}/`date +%Y%m%d`/OnlineProduction            
          """  
        }
      }
    }
    stage ('获取服务包：ems-envelope') {
      when {
        expression { return params.ems_envelope ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/ }
      }
      steps {
        // echo "${params.ems_server}"
        echo 'GettingPackage from prod_ems-envelope'
        script {
          def split=params.envelope.split(",")
          serverIP=split[0]
          proPath=split[1]
          mavenPath=split[2]
          port=split[3]
          sh """
          mkdir ${params.tmpPath}/`date +%Y%m%d`/{OnlineProduction,verifyProdPackage} -pv
          cp -a ${mavenPath} ${params.tmpPath}/`date +%Y%m%d`/verifyProdPackage
          scp -o StrictHostKeyChecking=no -P ${port} root@${serverIP}:${proPath} ${params.tmpPath}/`date +%Y%m%d`/OnlineProduction            
          """ 
        }
      }
    }
    // stage ('linux打包') {
    //   steps {
    //     sh """
    //     cd ${params.tmpPath}
    //     zip -r `date +%Y%m%d`.zip `date +%Y%m%d`
    //     """
    //   }
    // }
    stage ("传输到线下") {
      agent {
       label 'wwindowsIP'
     }
     steps {
      script {
        bat "WinSCP.exe /console /script=E:\\jenkins_slave\\sample2.txt /log=jenkinsIP.log"
        }
      }
    }
  }
}
