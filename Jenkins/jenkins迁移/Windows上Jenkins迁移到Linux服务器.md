# Windows上Jenkins迁移到Linux服务器上

## 一、迁移背景

```
1、由于历史原因，windows上搭建了一台jenkins服务，现资源整合，需要把windows上jenkins的job迁移到linux服务器上，在不影响当前服务器上job的可用性下，快捷迁移很重要；
2、很庆幸当时搭建windows服务器上jenkins版本与linux服务器jenkins版本保持一致2.107.3，迁移后少了很多插件不兼容的麻烦；
3、目标jenkins环境
所在服务器IP：jenkinsOldIP
操作系统：Linux filapp01 2.6.32-431.el6.x86_64
jenkins服务版本：2.107.3
4、源jenkins环境
所在服务器IP：jenkinsOldIP
操作系统：windows7
jenkins服务版本：2.107.3
```

## 二、迁移前准备

```
1、服务器间网络连通性（详情参考附件excel）
迁移后Jenkins目标服务器--> 源服务器jenkins上job所涉及远程ssh的服务器（默认端口22）
迁移后Jenkins目标服务器--> svn代码服务器/gitlba代码服务器（默认端口80）
迁移后Jenkins目标服务器--> maven私服服务器（默认端口80）
2、添加密码或key认证
http://jenkinsNewIP:8080/credentials/store/system/domain/_/ --> Add Credentials
源服务器jenkins上job所涉及远程ssh的服务器的登录用户/密码/key，以及svn登录用户/密码
3、系统管理--> 系统配置
http://jenkinsNewIP:8080/configure --> SSH remote hosts --> SSH sites that projects will want to connect增加
4、新建视图--> 简单视图
迁移后Jenkins目标服务器view上添加新job的view名，不需要与源服务器jenkins上view名保持一致
5、Jenkins源服务器打包工具
jdk、node、maven相同版本的工具安装
6、Jenkins目标服务器配置备份
由于迁移后Jenkins目标服务器不是空目录，原job、插件、用户、nodes等不能受到影响，而迁移后Jenkins目标服务器需要重启才能生效，重启Jenkins前一定要备份呀！
http://jenkinsNewIP:8080/thinBackup/backupsettings
本环境迁移后Jenkins目标服务器安装了thinbackup插件，settings每个工作日中午12点自动备份插件
另外需要备份的最重要的莫过于/var/lib/jenkins/jobs/中除了builds以及/var/lib/jenkins/config.xml
tar --exclude="/var/lib/jenkins/jobs/*/builds" -zcvf /opt/`date +%Y-%m-%d-%H_%M_%S`jenkins_job.tar.gz /var/lib/jenkins/jobs/
7、scp需要key认证
windows服务器上pscp.exe支持参数：-l root -pw "密码"，但是linux服务器的scp很死心眼不支持直接加参数暴露密码的方式，需要pssh或者key认证否则就要交互式输入密码，此时key认证就尤为重要咯~
8、ansible推key到目标服务器
ansible-playbook push.ssh.yaml -e "{'host':MPS}" --syntax-check
ansible-playbook push.ssh.yaml -e "{'host':MPS}"
```

## 三、正式迁移操作

```
1、把源jenkins：E:\jenkins2.107.3\Jenkins\jobs打一个zip包35M
2、放到目标jenkins上，把所需迁移的job信息：copy /var/lib/jenkins/jobs
3、重启目标jenkins：http://jenkinsNewIP:8080/reload
4、编辑新建的view，勾选迁移过来的job名
5、修改需要注意的地方
```

```
mps项目以先打一个总包，然后其他job分别来拉取各自的服务包的方式
1、除了打总包的job，其余job无需设置“丢弃旧的构建”以及“在必要的时候并发构建”，因此这些项目根本不会拉代码、构建、打包因此不会占用任何磁盘空间
2、把执行windows cmd命令，修改为Execute shell，并且把pscp修改为scp
pscp：echo y|E:\jenkins2.107.3\pscp.exe -l root -pw password
scp：scp -o "StrictHostKeyChecking no" -P22 test.txt root@serverIP:/tmp
# 此处加-o "StrictHostKeyChecking no"，ssh首次连接不提示yes不会强制检测key认证
3、注意修改pom.xml位置
4、修改settings.xml位置
```

其实还有更简单的迁移方式，使用Job Import插件，详情参考链接，本环境由于源jenkins环境与目标jenkins环境网络无法打通，因此无法使用该插件。

出现一个坑scp的时候报错：

```
Warning: Permanently added 'serverIP' (RSA) to the list of known hosts.
bash: scp: command not found
lost connection
scp有一个特性，需要源端和对端scp命令均可以使用，serverIP上最小化安装，而且无法使用yum，没有scp命令，强制安装(否则就会出现很多依赖报错)rpm -ivh openssh-clients-5.3p1-123.el6_9.x86_64.rpm --force --nodeps
```

打个包试试吧，打包成功才算基本迁移完成呢

报错1：

```
[ERROR] [ERROR] Some problems were encountered while processing the POMs:
[FATAL] Non-resolvable parent POM for com.macroflag:ems-backend:[unknown-version]: Could not find artifact com.macroflag:ems-pom:pom:0.0.1 in mirrorId (http://172.26.9.12:8081/repository/maven-central/) and 'parent.relativePath' points at wrong local POM @ line 4, column 12
[ERROR] The build could not read 8 projects -> [Help 1]
[ERROR]   
[ERROR]   The project com.macroflag:ems-backend:[unknown-version] (/var/lib/jenkins/workspace/ems_sit/ems-backend/pom.xml) has 1 error
[ERROR]     Non-resolvable parent POM for com.macroflag:ems-backend:[unknown-version]: Could not find artifact com.macroflag:ems-pom:pom:0.0.1 in mirrorId (http://172.26.9.12:8081/repository/maven-central/) and 'parent.relativePath' points at wrong local POM @ line 4, column 12 -> [Help 2]
[ERROR]   
```

jobs/builds中上次构建的pom信息，直接去/var/lib/jenkins/.m2/repository_mps找的时候发现目录为空找不到，所以报错。那么我们把jobs中除了config.xml之外的目录都删掉呢，这样就会从原始项目pom.xml去下载，如果那些依赖在私服或者官网可以下载到，就没问题，只会增加新的下载时间和磁盘使用量，但是如果之前下载的依赖位置变动或者官网下载不到了呢

报错2：

```
[INFO] ems-pom ............................................ SUCCESS [  1.432 s]
[INFO] ems-core ........................................... SUCCESS [ 27.469 s]
[INFO] ems-client ......................................... SUCCESS [  5.495 s]
[INFO] app-central Maven Webapp ........................... SUCCESS [  9.764 s]
[INFO] ems-server Maven Webapp ............................ FAILURE [ 31.844 s]
[INFO] ems-web Maven Webapp ............................... SKIPPED
[INFO] ems-business Maven Webapp .......................... SKIPPED
[INFO] ems-envelope Maven Webapp .......................... SKIPPED
[INFO] upload Maven Webapp ................................ SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 01:16 min
[INFO] Finished at: 2019-01-07T09:55:55+08:00
[INFO] Final Memory: 44M/511M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal on project ems-server: Could not resolve dependencies for project com.macroflag:ems-server:war:0.0.1: Could not find artifact jd.open.api:sdk:jar:2.0 in Clojars (http://clojars.org/repo/) -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/DependencyResolutionException
[ERROR] 
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <goals> -rf :ems-server
```

这时候我们考虑把原repository目录全部打包过来，再把/var/lib/jenkins/workspace/mps_sit清空，这次就打包成功啦~

```
mps原仓库路径：C:\users\taojh\.m2\repository
mps现仓库路径：/var/lib/jenkins/.m2/repository_mps
mis原仓库路径：E:\maven\repository
mis现仓库路径：/var/lib/jenkins/.m2/repository_mis
```

报错3：

```
Fatal Error: Unable to find package java.lang in classpath or bootclasspath
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR : 
[INFO] -------------------------------------------------------------
[ERROR] An unknown compilation problem occurred
[INFO] 1 error
[INFO] -------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] mis ................................................ SUCCESS [  0.659 s]
[INFO] mis-model .......................................... FAILURE [  2.162 s]
[INFO] mis-common ......................................... SKIPPED
[INFO] mis-service Maven Webapp ........................... SKIPPED
[INFO] mis-web Maven Webapp ............................... SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.005 s
[INFO] Finished at: 2019-01-07T17:04:09+08:00
[INFO] Final Memory: 26M/164M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.1:compile (default-compile) on project mis-model: Compilation failure
[ERROR] An unknown compilation problem occurred
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
[ERROR] 
[ERROR] After correcting the problems, you can resume the build with the command
```

解决方式

```
1、主要报错为：Fatal Error: Unable to find package java.lang in classpath or bootclasspath
2、找到pom.xml对应代码：<bootclasspath>${java.home}/lib/rt.jar;${java.home}/lib/jce.jar</bootclasspath>
3、linux中rt.jar和jce.jar在jre中，copy到lib中，并且chmod +x rt.jar
4、可以使用从windows服务器copy过来的apache-maven（chmod +x mvn 否则无法使用，需要在jenkins-job中指定settings：/var/lib/jenkins/conf/mis/settings.xml），
也可以在jenkins上插件管理中直接安装出对应版本的maven（需要修改/opt/apache-maven-3.2.3/conf/settings.xml，插件中取消勾选自动安装即可指定自己maven的家目录）效果相同
5、<bootclasspath>${java.home}/lib/rt.jar;${java.home}/lib/jce.jar</bootclasspath>替换为
<bootclasspath>${java.home}/lib/rt.jar${path.separator}${java.home}/lib/jce.jar</bootclasspath>
```

## 四、参考文档

https://www.duanlian.tech/2017/06/09/migrates-jenkins/

https://www.jianshu.com/p/dd289538a0eb

https://blog.csdn.net/u012759397/article/details/52099291

https://blog.csdn.net/x_lord/article/details/74311764