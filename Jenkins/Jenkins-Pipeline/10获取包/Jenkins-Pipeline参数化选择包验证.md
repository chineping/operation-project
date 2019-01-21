# Jenkins-Pipeline参数化选择包验证

## 一、背景

AVS项目发布前，为了验证代码的提交合并准确性，会把线上生产包以及当前jenkins打出的包进行Compare对比验证，每次打包的服务包不同，线上部署的服务器IP、服务路径以及登录方式不同，每次造成很多重复操作，所以使用参数化构建的方法实现拉包、验证并存档到windows服务器上。

## 二、上代码

pipeline代码：

```
pipeline {
###此时运行在jenkins服务器上
  agent any
  options { 
    timestamps () 
  }
  parameters {
###这里与发布服务的区别是指定到具体生产war包，对于双机的服务器只需拉一次代码包即可
    string(name:'server', defaultValue: 'serverIP,/usr/local/ems-server/webapps/ems-server.war,../ems_prod_pipeline/ems-server/target/ems-server.war', description: '生产PROD环境服务器ems-server')
    string(name:'backend', defaultValue: 'backendIP,/usr/local/ems-web/webapps/ems-backend.war,../ems_prod_pipeline/ems-backend/target/ems-backend.war', description: '生产PROD环境服务器ems-backend')
    string(name:'web', defaultValue: 'webIP,/usr/local/tomcat_central/webapps/ems-web.war,../ems_prod_pipeline/ems-web/target/ems-web.war,7788', description: '生产PROD环境服务器ems-web')
    string(name:'upload', defaultValue: 'uploadIP,/usr/local/upload/webapps/upload.war,../ems_prod_pipeline/upload/target/upload.war', description: '生产PROD环境服务器ems-upload')
    string(name:'envelope', defaultValue: 'webIP,/data/ems-envelope/webapps/ems-envelope.war,../ems_prod_pipeline/ems-envelope/target/ems-envelope.war,7788', description: '生产PROD环境服务器ems-envelope')
    string(name:'tmpPath', defaultValue: '/opt/projectVersion/avsVersion', description: 'jenkinsTmp临时存放目录')
###用于选择我要登录生产机器拉哪个代码包以及jenkins中同步打出待验证的新包
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
###这里与发布服务区别是指定serverIP：split[0]
          def split=params.server.split(",")
          serverIP=split[0]
          proPath=split[1]
          mavenPath=split[2]
###递归新建两个路径：线上生产包|待验证包，scp时是使用jenkins服务器去操作的，1、把root的公私钥copy到jenkins家目录下；2、指定参数StrictHostKeyChecking=no
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
###这里原本打算zip打包并且指定当前date年月日传输到线下，但是WinSCP与bat还是有区别的无法指定到date
    // stage ('linux打包') {
    //   steps {
    //     sh """
    //     cd ${params.tmpPath}
    //     zip -r `date +%Y%m%d`.zip `date +%Y%m%d`
    //     """
    //   }
    // }
###指定一个windows服务器IP，这里需要添加一个windows节点的jenkins_agent服务,可以设置成参数化，但是本脚本只有一个label设置成参数化反而比较麻烦还需要切割
    stage ("传输到线下") {
      agent {
       label 'w10.100.0.249'
     }
###运行一个bat脚本使用WinSCP.exe命令需要对\进行转义，需要安装WinSCP软件、配置环境变量并且安装到所有用户，否则jenkins服务器无法使用该命令
     steps {
      script {
        bat "E:\\WinSCP\\WinSCP.exe /console /script=E:\\jenkins_slave\\sample2.txt /log=jenkinsIP.log"
        }
      }
    }
  }
}
```

WinSCP代码：

```
###the script on errors，option echo  on|off
option echo off
###option batch on|off|abort|continue
option batch on
###option confirm  on|off
option confirm off
###open [ sftp|ftp|scp://]user:password@IP:port
open scp://root:111111@jenkinsIP:22
###远程linux工作目录
cd /opt/projectVersion/avsVersion
###本地windows工作目录
lcd E:\AVS版本
###下载：get，上传：put
get *
###option synchdelete on|off这里原本想把文件下载后自动删除，但是不生效
option synchdelete on
###在远程linux服务器上执行shell命令删除已经同步的内容
call rm -rf ./*
###关闭WinSCP软件连接
close
###关闭WinSCP软件运行窗口，调试过程中可以暂时把这个参数去掉查看传输过程，确认是否有报错
exit
```

## 三、前置条件

```
1、这里jenkins服务器同时作为生产环境ansible配置中心服务器，所以与各个生产环境网络互通，并且使用key验证，把所有服务器的包拉到统一的位置再传输到windows服务器进行存档
2、windows上安装WinSCP软件，windows与linux间传输方式有很多，这里采用scp脚本方式，sz是会报错而退出的，还可以使用sftp以及ftp方式需要改写WinSCP脚本
3、WinSCP安装后需要配置Windows环境变量，在WinSCP软件上配置jenkins服务器的登录方式并保存，首次需要交互验证
4、在jenkins上配置windows的agent采用LaunchAgentViaJavaWebStart方式保持在线
```

## 四、参考文档

1、Jenkins高级篇之Pipeline-补充篇-如何添加一个windows节点的jenkins agent 服务：https://blog.csdn.net/u011541946/article/details/83591148

2、用脚本实现windows与linux之间文件的传输：https://blog.csdn.net/shufac/article/details/51966276