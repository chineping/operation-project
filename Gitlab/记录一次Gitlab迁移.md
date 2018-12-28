# 记录一次Gitlab迁移

## 一、背景环境

```
原gitlab地址：IP1
新gitlab地址：IP2
迁移方式：线下服务器镜像复制
```

## 二、迁移前准备

```
gitlab-rake gitlab:env:info
System information
System:         CentOS 6.9
Proxy:          no
Current User:   git
Using RVM:      no
Ruby Version:   2.4.4p296
Gem Version:    2.7.6
Bundler Version:1.16.2
Rake Version:   12.3.1
Redis Version:  3.2.11
Git Version:    2.17.1
Sidekiq Version:5.1.3
Go Version:     unknown

GitLab information
Version:        11.1.2-ee
Revision:       f5babb0
Directory:      /opt/gitlab/embedded/service/gitlab-rails
DB Adapter:     postgresql
DB Version:     9.6.8
URL:            http://gitlab.*.com
HTTP Clone URL: http://gitlab.*.com/some-group/some-project.git
SSH Clone URL:  git@gitlab.*.com:some-group/some-project.git
Elasticsearch:  no
Geo:            no
Using LDAP:     no
Using Omniauth: no

GitLab Shell
Version:        7.1.4
Repository storage paths:
- default:      /var/opt/gitlab/git-data/repositories
Hooks:          /opt/gitlab/embedded/service/gitlab-shell/hooks
Git:            /opt/gitlab/embedded/bin/git
```

## 三、需要考虑因素

```
1、网络问题（gitlab、jenkins线上线下、备份服务器网络连通性）
2、IP变更，配置文件随之变更（主要是gitlab及nginx配置文件）
3、迁移后与jenkins服务器的权限验证
4、迁移后数据备份恢复问题（当前每天凌晨2点有一个全量备份）
```

## 四、实际操作

在网络环境打通的情况下

```
1、新gitlab服务器的远程ssh（22端口）
2、gitlab页面web页面可以打开（nginx80端口）
3、jenkins服务器需要与gitlab进行api token认证：jenkins访问gitlab：80，jenkins到gitlab的远程ssh key验证拉代码：jenkins访问gitlab：22
4、gitlab服务器到备份服务器远程ssh连接：gitlab访问backup：22
```

### 修改新gitlab配置文件

```
find / -name gitlab.yml 
/opt/gitlab/embedded/service/gitlab-rails/config/gitlab.yml 
/var/opt/gitlab/gitlab-rails/etc/gitlab.yml

# find / -name gitlab.rb 
/etc/gitlab/gitlab.rb

1.修改 gitlab.yml（/opt/gitlab/embedded/service/gitlab-rails/config/gitlab.yml）文件中的host地址 
2.修改 gitlab.rb（/etc/gitlab/gitlab.rb）文件中的external_url  [将`external_url = 'http://gitlab.*.com'`修改为自己的IP地址]
```

最重要的命令，它会把很多地方的原IP重新配置成输入的新external_url地址

```
gitlab-ctl reconfigure
```

此时配置文件已经修改完毕，修改external_url主要是git拉代码的地址

```
1、拉代码地址
git@gitlab.*.com:plusplatform/plusplatform.git
2、web页面访问地址
http://gitlab.*.com/ gitlab域名内网访问不到（填写本地hosts即可访问）
http://IP2/         访问IP正常显示gitlab登录页面
```

此时配置gitlab与jenkins服务器，测试拉代码

```
1、jenkins页面上：点击系统管理--系统设置，找到gitlab配置
添加新的gitlab信息
Connection name：gitlab-new
Gitlab host URL：http://gitlab.*.com
Credentials：使用原gitlab Api验证信息即可
2、jenkins页面上：点击系统管理--全局工具配置，找到git配置
	2.1添加新的git自动安装，起个新名字
	2.2在jenkins服务器上源码包安装一个git客户端，
	Name：git2.14.1
	Path to Git executable：/opt/git/libexec/git-core/git （此处要写到git命令，不是path）	
```

注意：

1、此处在gitlab页面上：点击User Settings--Access Tocken api类型，由于是克隆的环境此处无需新建token，但是这里有个坑，由于是个人私有token，如果使用admin用户创建的，即使其他用户有admin权限也无法看到admin的私有token；

2、此处为什么要新安装一个git客户端呢？如果不安装新的git插件会导致原来的gitlab地址无法使用，报错如下：

```
Failed to connect to repository : Command "git ls-remote -h git@gitlab.*.com:plusplatform/plusplatform.git HEAD" returned status code 128:
stdout: 
stderr: Host key verification failed. 
fatal: Could not read from remote repository.

Please make sure you have the correct access rights
and the repository exists.
```

如何解决？

```
[root@VM_4_5_centos jenkins]# mkdir git1.8.3
[root@VM_4_5_centos jenkins]# cd git1.8.3/
[root@VM_4_5_centos git1.8.3]# pwd
/data/jenkins_home/jenkins/git1.8.3
[root@VM_4_5_centos git1.8.3]# git ls-remote -h git@gitlab.*.com:plusplatform/plusplatform.git HEAD
The authenticity of host 'gitlab.*.com (IP2)' can't be established.
RSA key fingerprint is SHA256:0yIYW3ghqZNPk44TDQe7V31EONGIw4w82eUcIfWwAB0.
RSA key fingerprint is MD5:89:07:86:3f:eb:17:00:dd:25:86:84:ad:ad:b3:18:83.
Are you sure you want to continue connecting (yes/no)? yes
Warning: Permanently added 'gitlab.*.com,IP2' (RSA) to the list of known hosts.
[root@VM_4_5_centos git1.8.3]# git config --global user.name "jenkins_proIP"
[root@VM_4_5_centos git1.8.3]# git config --global user.email "jenkins_proIP@kq300061.com"    
[root@VM_4_5_centos git1.8.3]# ls -a
.  ..  .git
[root@VM_4_5_centos git1.8.3]# git config -l
user.name=jenkins_proIP
user.email=jenkins_proIP@kq300061.com
core.repositoryformatversion=0
core.filemode=true
core.bare=false
core.logallrefupdates=true
[root@VM_4_5_centos git1.8.3]# git clone git@gitlab.*.com:plusplatform/plusplatform.git
Cloning into 'plusplatform'...
remote: Counting objects: 14480, done.
remote: Compressing objects: 100% (5176/5176), done.
remote: Total 14480 (delta 5651), reused 14479 (delta 5651)
Receiving objects: 100% (14480/14480), 10.63 MiB | 495.00 KiB/s, done.
Resolving deltas: 100% (5651/5651), done.
[root@VM_4_5_centos git1.8.3]# ls -a
.  ..  .git  plusplatform
```

```
Started by user 账号
Building on master in workspace /var/lib/jenkins/workspace/1
Cloning the remote Git repository
Cloning repository git@gitlab.*.com:plusplatform/plusplatform.git
 > git init /var/lib/jenkins/workspace/1 # timeout=10
ERROR: Error cloning remote repo 'origin'
hudson.plugins.git.GitException: Could not init /var/lib/jenkins/workspace/1
	at org.jenkinsci.plugins.gitclient.CliGitAPIImpl$5.execute(CliGitAPIImpl.java:787)
	at org.jenkinsci.plugins.gitclient.CliGitAPIImpl$2.execute(CliGitAPIImpl.java:579)
	at hudson.plugins.git.GitSCM.retrieveChanges(GitSCM.java:1146)
	at hudson.plugins.git.GitSCM.checkout(GitSCM.java:1186)
	at hudson.scm.SCM.checkout(SCM.java:504)
	at hudson.model.AbstractProject.checkout(AbstractProject.java:1208)
	at hudson.model.AbstractBuild$AbstractBuildExecution.defaultCheckout(AbstractBuild.java:574)
	at jenkins.scm.SCMCheckoutStrategy.checkout(SCMCheckoutStrategy.java:86)
	at hudson.model.AbstractBuild$AbstractBuildExecution.run(AbstractBuild.java:499)
	at hudson.model.Run.execute(Run.java:1727)
	at hudson.model.FreeStyleBuild.run(FreeStyleBuild.java:43)
	at hudson.model.ResourceController.execute(ResourceController.java:97)
	at hudson.model.Executor.run(Executor.java:429)
Caused by: hudson.plugins.git.GitException: Error performing command: git init /var/lib/jenkins/workspace/1
	at org.jenkinsci.plugins.gitclient.CliGitAPIImpl.launchCommandIn(CliGitAPIImpl.java:2023)
	at org.jenkinsci.plugins.gitclient.CliGitAPIImpl.launchCommandIn(CliGitAPIImpl.java:1984)
	at org.jenkinsci.plugins.gitclient.CliGitAPIImpl.launchCommandIn(CliGitAPIImpl.java:1980)
	at org.jenkinsci.plugins.gitclient.CliGitAPIImpl.launchCommand(CliGitAPIImpl.java:1612)
	at org.jenkinsci.plugins.gitclient.CliGitAPIImpl$5.execute(CliGitAPIImpl.java:785)
	... 12 more
Caused by: java.io.IOException: Cannot run program "git" (in directory "/var/lib/jenkins/workspace/1"): error=20, Not a directory
	at java.lang.ProcessBuilder.start(ProcessBuilder.java:1048)
	at hudson.Proc$LocalProc.<init>(Proc.java:249)
	at hudson.Proc$LocalProc.<init>(Proc.java:218)
	at hudson.Launcher$LocalLauncher.launch(Launcher.java:929)
	at hudson.Launcher$ProcStarter.start(Launcher.java:449)
	at org.jenkinsci.plugins.gitclient.CliGitAPIImpl.launchCommandIn(CliGitAPIImpl.java:2012)
	... 16 more
Caused by: java.io.IOException: error=20, Not a directory
	at java.lang.UNIXProcess.forkAndExec(Native Method)
	at java.lang.UNIXProcess.<init>(UNIXProcess.java:247)
	at java.lang.ProcessImpl.start(ProcessImpl.java:134)
	at java.lang.ProcessBuilder.start(ProcessBuilder.java:1029)
	... 21 more
ERROR: Error cloning remote repo 'origin'
Finished: FAILURE
```

为什么会产生这样的报错？

```
1、对于jenkins服务器而言，新的gitlab服务器是一台新的服务器，通过Credentials key去验证的时候，新gitlab的服务器信息（主机名|IP）不在/root/.ssh/known_hosts内，而jenkins无法交互输入yes，把该主机信息加入known_hosts
2、原jenkins的git插件内包含config内初始化保存原gitlab信息了
3、如果是在客户端上更改了远程remote地址，可以执行命令
客户端 git remote set-url origin gitlab.*.com
```

### 迁移计划

```
1、预计在2019年1月2日停止gitlab服务器连接，开启一个全量备份，由于目前每日备份的全量数据量523M，并以每日1M左右的速度增长，全量备份在1min以内；
2、代码在新gitlab上同步后，启动服务，停止原gitlab服务；
3、修改原凌晨2点自动备份计划，并测试网络连通性；
4、修改本地客户端代码库配置；
5、修改jenkins自动化发版代码仓库配置
```

### 全量备份以及恢复命令

```
全量备份：/opt/gitlab/bin/gitlab-rake gitlab:backup:create
全量恢复：  
# 停止相关数据连接服务
gitlab-ctl stop unicorn
gitlab-ctl stop sidekiq
# 注意恢复文件名需要按照实际修改
chmod 777 1545847246_2018_12_27_11.1.2-ee_gitlab_backup.tar
vim /opt/gitlab/embedded/service/gitlab-rails/lib/backup/files.rb 
# 取消--unlink-first --recursive-unlink参数
gitlab-rake gitlab:backup:restore BACKUP=1545933665_2018_12_28_11.1.2-ee --trace force=yes
启动服务：gitlab-ctl start
打开web页面验证：gitlab.*.com（此处需加windows本地hosts解析）
```

### 迁移之后客户端操作

其实质就是在你本地git仓库路径下，有一个E:/账号/.git/config配置文件，修改其中配置即可，重新推拉代码会让你再次输入密码，输入与原gitlab相同用户名及密码即可

```
$ git remote -v
origin  http://IP1/taojh/账号.git (fetch)
origin  http://IP1/taojh/账号.git (push)

git config -e
把fetch及push路径修改为当前新的gitlab地址：
origin  http://gitlab.*.com/taojh/账号.git (fetch)
origin  http://gitlab.*.com/taojh/账号.git (push)

还有很多文档建议直接使用命令更改：
git remote set-url origin http://gitlab.*.com/taojh/账号.git
git remote set-url --push origin http://gitlab.*.com/taojh/账号.git
验证一下：git remote -v
```

https://blog.csdn.net/rrrrrr123rrr/article/details/50684245

https://help.github.com/articles/changing-a-remote-s-url/

https://confluence.atlassian.com/bitbucket/change-the-remote-url-to-your-repository-794212774.html

### 修改jenkins代码仓库地址

第一类：采用传统自由风格创建的job，例如sjbank_dev_products

```
点击配置--源码管理--
1、Repository URL：git@IP1:plusplatform/plusplatform.git 修改为git@gitlab.*.com:plusplatform/plusplatform.git
2、Git executable修改为：git2.14.1
（此处为服务器上源码包安装的git，并已经配置了仓库源为gitlab.*.com，通过了初次验证git ls remote）
```

第二类：采用pipeline流水线风格创建的job，例如jtbank_dev_H5_npm-pipeline

```
        stage ('拉取gitlab代码') {
        ========================================》第一种
            // steps {
            //     checkout([$class: 'GitSCM', branches: [[name: '*/bj_dev']], doGenerateSubmoduleConfigurations: false, 
            //         xtensions: [], gitTool: 'git2.14.1', submoduleCfg: [], 
            //         userRemoteConfigs: [[credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', 
            //         url: 'git@gitlab.*.com:plusplatform/plusplatform.git']]])
            //     }
         ========================================》第二种
            // steps {
            //     checkout([$class: 'GitSCM', branches: [[name: '*/bj_dev']], gitTool: 'git2.14.1',  
            //         userRemoteConfigs: [[credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', 
            //         url: 'git@gitlab.*.com:plusplatform/plusplatform.git']]])
            //     }
         ========================================》第三种
                tools {
                    git 'git2.14.1'
                }
                steps {
                    git branch: 'master', credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: 'git@gitlab.*.com:plusplatform/plusplatform.git'
                }
            }
```

以上第一种是jenkins里pipeline-checkout的标准写法；第二种是第一种的简写，去除了一些我们用不到的参数，看起来更简洁一些，第三种是最简单的修改方式，只需修改原job的url，并且添加tools指定git版本即可。

### 使用shell脚本批量修改jenkins代码仓库地址

统计了测试环境jenkins的job个数生产环境共计73，测试环境共计114

```
# 生产环境
[root@VM_4_5_centos ~]# wc -l jobName.txt
73 jobName.txt
#pipeline+svn+回滚脚本类型job个数
[root@VM_4_5_centos ~]# grep pipeline jobName.txt |wc -l
49
#freestyle类型job个数
[root@VM_4_5_centos ~]# grep freestyle jobName.txt |wc -l       
24
```

```
# 测试环境
[root@filapp01 ~]# wc -l jobNameStyle.txt
114 jobNameStyle.txt
#pipeline+svn类型job个数
[root@filapp01 ~]# grep pipeline jobNameStyle.txt |wc -l
55
#freestyle类型job个数
[root@filapp01 ~]# grep freestyle jobNameStyle.txt |wc -l        
59
```

共计200个左右，除去svn及回滚脚本的不用修改，其余的重复操作高达100次？what！我们是做技术的，怎么能手动操作这种重复的没技术的工作？上脚本

测试环境jenkins：

```
#!/bin/bash
jobConfigPath=/var/lib/jenkins/jobs
echo "备份jobs/*/config.xml"
tar --exclude="/var/lib/jenkins/jobs/*/builds" -zcvf /opt/`date +%Y-%m-%d-%H_%M_%S`jenkins_job.tar.gz /var/lib/jenkins/jobs/

configName=config.xml
for jobName in `ls $jobConfigPath`
do cd $jobConfigPath/$jobName
if grep "<doGenerateSubmoduleConfigurations>" $configName &> /dev/null;then
        echo "$jobName is freestyle jenkins job"
        sed -i 's/git@IP1/git@gitlab.*.com/' $configName
        sed -i '/doGenerateSubmoduleConfigurations/a\    <gitTool>git2.14.1</gitTool>' $configName
else
        echo "$jobName is pipeline jenkins job"
        sed -i 's/git@IP1/git@gitlab.*.com/g' $configName
        sed -i '/拉取gitlab代码/a\tools { git &apos;git2.14.1&apos; }' $configName
fi
done
echo "需要重启jenkinsjenkins_devIP"
```

生产环境jenkins：

```
#!/bin/bash
jobConfigPath=/data/jenkins_home/jenkins/jobs
echo "备份jobs/*/config.xml"
tar --exclude="/data/jenkins_home/jenkins/jobs/*/builds" -zcvf /opt/`date +%Y-%m-%d-%H_%M_%S`jenkins_job.tar.gz /data/jenkins_home/jenkins/jobs/

configName=config.xml
for jobName in `ls $jobConfigPath`
do cd $jobConfigPath/$jobName
if grep "<doGenerateSubmoduleConfigurations>" $configName &> /dev/null;then
        echo "$jobName is freestyle jenkins job"
        sed -i 's/git@IP1/git@gitlab.*.com/' $configName
        sed -i 's#<gitTool>Default</gitTool>#<gitTool>git1.8.3</gitTool>#g' $configName
else
        echo "$jobName is pipeline jenkins job"
        sed -i 's/git@IP1/git@gitlab.*.com/g' $configName
        sed -i '/拉取gitlab代码/a\tools { git &apos;git1.8.3&apos; }' $configName
fi
done
echo "需要重启jenkinsjenkins_proIP"
```

做了什么操作？测试环境及生产环境有哪些差异？

```
1、主要目的就是修改/data/jenkins_home/jenkins/jobs/jobName/config.xml
2、修改前先备份一下
3、修改完需要重启服务器配置文件才能生效
4、参考文档：https://wiki.jenkins.io/display/JENKINS/Administering+Jenkins
https://blog.csdn.net/intelrain/article/details/79651389
注意差异：
测试环境jenkins2.107、操作系统centos7、git2.4.1
生产环境的jenkins2.155、操作系统centos6.5、git1.8.3，生产环境本身就包含<gitTool>Default</gitTool>
```

测试环境测试脚本

```
#!/bin/bash
jobConfigPath=/var/lib/jenkins/jobs
jobName=2
configName=config.xml
cd $jobConfigPath/$jobName
if grep "<doGenerateSubmoduleConfigurations>" $configName &> /dev/null;then
        echo "$jobName is freestyle jenkins job"
        sed -i 's/git@IP1/git@gitlab.*.com/' $configName
        sed -i '/doGenerateSubmoduleConfigurations/a\    <gitTool>git2.14.1</gitTool>' $configName
        curl -u 账号:账号123 -X POST http://jenkins_devIP:8080/view/jtbank_dev_bj/job/1/reload
else
cd $jobConfigPath/$jobName
        echo "$jobName is pipeline jenkins job"
        sed -i 's/git@IP1/git@gitlab.*.com/g' $configName
        sed -i '/拉取gitlab代码/a\tools { git &apos;git2.14.1&apos; }' $configName
        curl -u 账号:账号123 -X POST http://jenkins_devIP:8080/view/jtbank_dev_bj/job/2/reload
fi
```

生产环境测试脚本

```
#!/bin/bash
jobConfigPath=/data/jenkins_home/jenkins/jobs
jobName=2
configName=config.xml
cd $jobConfigPath/$jobName
if grep "<doGenerateSubmoduleConfigurations>" $configName &> /dev/null;then
        echo "$jobName is freestyle jenkins job"
        sed -i 's/git@IP1/git@gitlab.*.com/g' $configName
        sed -i 's#<gitTool>Default</gitTool>#<gitTool>git1.8.3</gitTool>#g' $configName
        curl -u 账号:密码 -X POST http://jenkins_proIP:8081/view/hhbank_prod/job/1/reload
else
cd $jobConfigPath/$jobName
        echo "$jobName is pipeline jenkins job"
        sed -i 's/git@IP1/git@gitlab.*.com/g' $configName
        sed -i '/拉取gitlab代码/a\tools { git &apos;git1.8.3&apos; }' $configName
        curl -u 账号:密码 -X POST http://jenkins_proIP:8081/view/hhbank_prod/job/2/reload
fi
```