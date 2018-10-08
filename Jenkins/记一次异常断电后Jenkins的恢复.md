# 记一次异常断电后Jenkins的恢复

## 背景

```
Jenkins安装环境：原为开发人员安装和维护，服务器为dev开发环境
Jenkins启动方式：java -jar jenkins.war
Jenkins安装路径：/home/jenkins
服务器环境：3.10.0-514.el7.x86_64
war包路径：/home/jenkins/jenkins.war
```

以上信息均在history找到，运维与开发人员交流一般都不太顺畅。唉、还是努力提高个人的沟通能力吧！

## Jenkins启动方式

service jenkins start

```
cat /etc/init.d/jenkins
#! /bin/sh  
# chkconfig: 2345 10 90   
# description: jenkins ....  
# This script will be executed *after* all the other init scripts.    
# You can put your own initialization stuff in here if you don't    
# want to do the full Sys V style init stuff.    
#prefix=/home/lanmps/jenkins  
#nohup $prefix/start_jenkins.sh >> $prefix/jenkins.log 2>&1 &  
  
JENKINS_ROOT=/home/jenkins  
JENKINSFILENAME=jenkins.war  
  
#停止方法  
stop(){  
    echo "Stoping $JENKINSFILENAME "  
    ps -ef|grep $JENKINSFILENAME |awk '{print $2}'|while read pid  
    do  
       kill -9 $pid  
       echo " $pid kill"  
    done  
}  
  
case "$1" in  
start)  
    echo "Starting $JENKINSFILENAME "  
    nohup $JENKINS_ROOT/start_jenkins.sh >> $JENKINS_ROOT/jenkins.log 2>&1 &  
  ;;  
stop)  
  stop  
  ;;  
restart)  
  stop  
  start  
  ;;  
status)  
  ps -ef|grep $JENKINSFILENAME  
  ;;  
*)  
  printf 'Usage: %s {start|stop|restart|status}\n' "$prog"  
  exit 1  
  ;;  
esac
```

cat /home/jenkins/start_jenkins.sh

```
#!/bin/bash  
JENKINS_ROOT=/home/jenkins  
export JENKINS_HOME=/home/jenkins/.jenkins
java -jar $JENKINS_ROOT/jenkins.war --httpPort=8081
```

看起来好简单，但是排查的过程，各种报错信息，真心累，主要是对这个环境比较陌生。

## Jenkins启动报错

### 1、jdk版本不匹配无法启动？

该服务器上安装了2个版本jdk1.7和jdk1.8，该版本的jenkins须为jdk1.8以上，因此需要在本路径/home/jenkins/.bash_profile，指定java版本只对特定用户生效。

```
#export PATH
export JAVA_HOME=/home/jenkins/java/jdk1.8.0_131
export JRE_HOME=${JAVA_HOME}/jre
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib
export PATH=${JAVA_HOME}/bin:$PATH
```

### 2、家目录指定清楚

原来的job存储路径：/home/jenkins/.jenkins/workspace，因此家目录的位置即为/home/jenkins/.jenkins，如果指定错了位置，就像第一次启动jenkins服务一样，会在你所指定的家路径下，再创建工作空间，原来的job信息就都没有了，其实只是家目录的指定有误，原来的信息并不会因为指定错了家目录而被覆盖，只是会重新启动一个全新的jenkins服务。

### 3、原账号密码未知怎么办？

找到/home/jenkins/.jenkins/users路径下，有一些可登录服务器的用户，找到管理员用户/home/jenkins/.jenkins/users/zhangxy/config.xml配置文件，找到passwordHash部分，后者密码是111111的hash值，重启jenkins生效。

```
改
#jbcrypt:$2a$10$QqzUz7kc8U8wkc5vexlxnuw5umudQj7A.0NmuZPrGMaq3qaV5m7hi
为
#jbcrypt:$2a$10$DdaWzN64JgUtLdvxWIflcuQu2fgrrMSAMabF5TSrGK5nXitqK9ZMS
```

### 4、启动时一些插件报错怎么办？

如果指定错了工作空间，就会有插件报错的问题，服务会正常启动，但是插件的配置信息都在原工作路径下，这个未尝试把原工作路径全部拷贝到新workspace这种方法。一方面怕丢失信息，另一方面也workspace一般也会比较大。

