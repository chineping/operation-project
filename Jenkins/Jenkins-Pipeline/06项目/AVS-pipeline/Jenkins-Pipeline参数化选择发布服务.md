# Jenkins-Pipeline参数化选择发布服务

## 一、脚本解析

直接上脚本，脚本中相应位置会添加比较详细的标注，如需看原脚本看附件

```
pipeline {
###默认在jenkins所在服务器上执行，这个与系统管理--管理节点--master配置从节点中(尽可能使用这个节点有关)http://jenkinsIP:8080/computer/(master)/configure
  agent any
###jenkins自带时间戳函数
  options { 
    timestamps () 
  }
###参数化构建：自定义字符串与布尔值类型参数
  parameters {
###后面会优化：把repoUrl定义成可选择的参数choices
    string(name:'repoUrl', defaultValue: 'http://svnIP/svn/ems/trunk@HEAD', description: 'SVN代码路径')
###指定pom.xml配置文件，所在的操作目录为jenkins-jobHome-workspace所以直接写相对路径即可
    string(name:'pomPath', defaultValue: 'ems-pom/pom.xml', description: 'maven构建pom.xml')
###由于jenkins会构建很多项目，pom不统一：单独定义maven-settings.xml，单独定义maven的.m2/repository_mps，使得各个项目的依赖互不干扰
    string(name:'mavenSettingPath', defaultValue: '/var/lib/jenkins/conf/mps/settings.xml', description: 'maven构建settings.xml')
###此项目有5个服务如果全部参数化，string会太多很难维护很难看，把一个服务的信息写在一起，后面script切割：取服务所在IP、服务所在路径、jenkins-maven构建后打包
    string(name:'server', defaultValue: 'serverIP,/usr/local/ems-server/webapps,ems-server/target/ems-server.war', description: '老SIT环境服务器ems-server')
    string(name:'backend', defaultValue: 'serverIP,/usr/local/ems-web/webapps,ems-backend/target/ems-backend.war', description: '老SIT环境服务器ems-backend')
###取服务所在IP、服务所在路径、jenkins-maven构建后打包、远程连接port
    string(name:'web', defaultValue: 'webIP,/usr/local/tomcat/webapps,ems-web/target/ems-web.war,7980', description: '老SIT环境服务器ems-web')
    string(name:'upload', defaultValue: 'serverIP,/usr/local/ems-envelope/webapps,upload/target/upload.war', description: '老SIT环境服务器ems-upload')
    string(name:'envelope', defaultValue: 'serverIP,/usr/local/ems-envelope/webapps,ems-envelope/target/ems-envelope.war', description: '老SIT环境服务器ems-envelope')
###布尔值默认不勾选，名称与上面string要区分，后面用于when判断服务是否勾选，stage是否运行
    booleanParam(defaultValue: false, name: 'ems_server', description: 'Process ems-server')
    booleanParam(defaultValue: false, name: 'ems_backend', description: 'Process ems-backend')
    booleanParam(defaultValue: false, name: 'ems_web', description: 'Process ems-web')
    booleanParam(defaultValue: false, name: 'ems_upload', description: 'Process ems_upload')
    booleanParam(defaultValue: false, name: 'ems_envelope', description: 'Process ems-envelope')
  }
###看！整个项目中都没有关于svn密码认证的代码，原因在此，自动生成代码片段Checkout代码的时候生成的CRED_ID就是svn认证
  environment {
    CRED_ID='67486f7c-f59b-45b0-87b2-f063a8b570ad'
  }
###整体stages注意这里有s，下面是很多个stage，每个stages中至少必须有一个stage，一个stage下面又必须有一个steps，这就是Directives类pipeline的格式要求，不然就会报错
  stages {
    stage ('Checkout') {
      steps {
###这里有自定义变量所以要使用脚本script，svn的代码Checkout要这么写，这里可以输出当前拉的代码版本号
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
###maven构建的时候首先在系统管理--全局工具配置中安装好jdk以及maven工具：一种是在jenkins所在服务器上安装，全局工具配置中声明工具所在家目录，另一种是通过jenkins-web页面直接在线安装，这里有一个-Pdev是指定代码中application-dev.yml配置文件采用开发环境的服务器及数据库连接地址
    stage ('maven构建') {
      steps {
       withMaven(jdk: 'jdk1.7', maven: 'maven3.3.9') { 
         sh "mvn -f ${params.pomPath} -s ${params.mavenSettingPath} -Dmaven.test.skip=true clean install -U -Pdev" 
       }
     }
   }
###when判断表达式：参数化构建中布尔值类型的服务是true还是false，这里是固定的写法，如果直接写一个true会报错，如果满足when条件则执行此stage下的steps则进行发布操作
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
        echo 'Deploying to old-sit_ems-server'
###这里定义一个切割的变量是groovy(类java)写法
        script {
          def split=params.server.split(",")
          proPath=split[1]
          mavenPath=split[2]
###for循环为了生产环境双机情况，只需在string服务信息中第二个位置加入第二台机器的IP这里改成for i<2即可，双机除了服务器IP其余皆是一模一样，所以不要写重复代码
          for (int i = 0;i < 1;++i) {
              // scp ${mavenPath} root@\"${split[i]}\":/tmp
###这里要注意一下，有$,""这些特殊字符需要\转义
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-server|grep -v grep|awk '{print \$2}'"
###这里如果ps -ef中出现了多个pid进程默认是分行的，这里把换行转成空格groovy(类java)写法
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
###一堆shell命令，建议用三引号
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/ems-server.war /usr/local/ems-server/war_bak/`date +%Y%m%d+`ems-server.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/ems-server*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-server/bin/startup.sh
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
        echo 'Deploying to old-sit_ems-backend'
        script {
          def split=params.backend.split(",")
          proPath=split[1]
          mavenPath=split[2]
          for (int i = 0;i < 1;++i) {
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-web|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/ems-backend.war /usr/local/ems-web/war_bak/`date +%Y%m%d+`ems-backend.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/ems-backend*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-web/bin/startup.sh
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
        echo 'Deploying to old-sit_ems-web'
        script {
          def split=params.web.split(",")
          proPath=split[1]
          mavenPath=split[2]
          port=split[3]
###这里服务器的远程ssh端口非22，所以多一个port
          for (int i = 0;i < 1;++i) {
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" -p \"${port}\" ps -ef|grep /usr/local/tomcat|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" -p \"${port}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" -p \"${port}\" mv ${proPath}/ems-web.war /data/bak/`date +%Y%m%d+`ems-web.war
              ssh -f -n root@\"${split[i]}\" -p \"${port}\" rm -fR ${proPath}/ems-web*
              scp -P \"${port}\" ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" -p \"${port}\" bash /usr/local/tomcat/bin/startup.sh
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
        echo 'Deploying to old-sit_ems-upload'
        script {
          def split=params.upload.split(",")
          proPath=split[1]
          mavenPath=split[2]
          for (int i = 0;i < 1;++i) {
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-envelope|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/upload.war /usr/local/ems-envelope/war_bak/`date +%Y%m%d+`upload.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/upload*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-envelope/bin/startup.sh
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
        // echo "${params.ems_server}"
        echo 'Deploying to old-sit_ems-envelope'
        script {
          def split=params.envelope.split(",")
          proPath=split[1]
          mavenPath=split[2]
          for (int i = 0;i < 1;++i) {
              // scp ${mavenPath} root@\"${split[i]}\":/tmp
              def pid = sh returnStdout: true ,script: "ssh -f -n root@\"${split[i]}\" ps -ef|grep ems-envelope|grep -v grep|awk '{print \$2}'"
              pid = pid.replaceAll("(\r\n|\r|\n|\n\r)", " ");
              echo "you input pid is ${pid},to do sth"
              sh """              
              ssh -f -n root@\"${split[i]}\" kill -9 ${pid}
              ssh -f -n root@\"${split[i]}\" mv ${proPath}/ems-envelope.war /usr/local/ems-envelope/war_bak/`date +%Y%m%d+`ems-envelope.war
              ssh -f -n root@\"${split[i]}\" rm -fR ${proPath}/ems-envelope*
              scp ${mavenPath} root@\"${split[i]}\":${proPath}
              ssh -f -n root@\"${split[i]}\" bash /usr/local/ems-envelope/bin/startup.sh
              """
            }
          }
        }
      }
    }
  }
```

## 二、注意事项

1、以上代码在jenkins2.107.3版本适用，在不同版本的jenkins中pipeline写法略有不同，网络上的一些代码没有说明jenkins的版本我的版本运行一些脚本经常会报错

2、上面使用的是booleanParam还可以选择复选框，感觉会更难一点而且自定义函数我的jenkins上不支持需要加白名单



## 三、参考文档

https://zhuanlan.zhihu.com/p/51533506

https://stackoverflow.com/questions/46680573/how-to-make-sure-list-of-parameters-are-updated-before-running-a-jenkins-pipelin

https://jenkins.io/blog/2017/01/19/converting-conditional-to-pipeline/

https://jenkins.io/doc/book/pipeline/syntax/#when

https://stackoverflow.com/questions/48898928/how-to-pass-multi-select-value-parameter-in-jenkins-filegroovy

