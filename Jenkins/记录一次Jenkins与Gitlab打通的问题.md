# 记录一次Jenkins与Gitlab打通的问题

## 一、背景

```
由于测试环境的资源还未统一，不同项目可能使用了不同的自建gitlab代码库（这里暂且不谈svn），而运维环境的jenkins使用同一套，那么在相同的jenkins服务器上拉不同url的gitlab代码就有了一点冲突和问题。例如我们在自己的电脑上配置git客户端的远端gitlab代码库url、用户名、密码等信息后，很少变动，每次推拉代码就会自动去找已经存储的账号密码实现免密，如果需要存储多个gitlab的账号密码url等就需要一些软件来区分存储。那么jenkins上如何实现村存储多个远程代码库的url、用户以及密码实现正常的拉取代码并打包构建呢？环境已知如下：
jenkins服务器jenkinsIP：linux centos6.5 
gitlab1服务器gitlab1IP：linux centos6.9          nginx配置在本地80
gitlab2服务器gitlab2IP：linux Ubuntu 18.04 LTS   nginx配置在本地80，但是前面有一层域名+ssl解析（在统一的域名解析nginxnginxIP服务器上）转发到gitlab的nginx
```

## 二、实现Jenkins与Gitlab打通

### 2.1 传统方式

### 2.1.1 Jenkins系统配置

```
系统设置->管理插件：git plugin，gitlab plugin，gitlab hook plugin
系统设置->Configure Global Security

配置gitlab授权
系统设置->系统设置->Gitlab：
token 由admin生成API Token并加入Jenkins授信

Test Connection 为Success即为授权通过
```

#### 2.1.2 Job配置

```
Jenkins->Credentials->System->Global credentials (unrestricted)->root/****** (mpsRootGitlabPass)

job配置->源码管理->Git->Repositories	
Repository URL：
http://git.****.com/username/Third-wxh.git 或者 ssh类型
Credentials：这里选择刚刚创建好的mpsRootGitlabPass（无法识别系统配置里的api token类型的认证，一般采用root密码或key认证方式）
```

### 2.2 当前环境

```
由于当前环境比较复杂，不知道gitlab2服务器的root密码，并且jenkins上已经认证并存在gitlab1代码库信息，那么想拉gitlab2代码库的一个私有库的用户名和密码如何实现拉取该私有库的代码并打包呢？
```

#### 2.2.1 坑1：认证不通过

```
Connection name->git-avs
Gitlab host URL->http://git.****.com
Credentials：API Token

点击Test Connection，超时报错
```

##### 排错过程

```
jenkins服务器：
1、ping www.baidu.com        通，证明jenkins服务器可以连外网
2、nslookup git.****.com 解析结果：nslookupIP
3、traceroute nslookupIP  只到了网关
外网不通只能从内网了，nslookupIP->neiwang_eth0IP_eth0（即域名服务器nginxIP_eth1）->真正的gitlab2服务器gitlab2IP
4、添加本地host解析/etc/hosts绕过ssl
gitlab2IP git.****.com

此时点击Test Connection，返回Success
```

#### 2.2.2 坑2：拉代码报错error fetching remote repo origin

```
 > /opt/git20190121/libexec/git-core/git fetch --tags --progress git@git.****.com:username/Third-wxh.git +refs/heads/*:refs/remotes/origin/*
ERROR: Error fetching remote repo 'origin'
hudson.plugins.git.GitException: Failed to fetch from git@git.****.com:username/Third-wxh.git
	at hudson.plugins.git.GitSCM.fetchFrom(GitSCM.java:888)
	at hudson.plugins.git.GitSCM.retrieveChanges(GitSCM.java:1155)
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
Caused by: hudson.plugins.git.GitException: Command "/opt/git20190121/libexec/git-core/git fetch --tags --progress git@git.****.com:username/Third-wxh.git +refs/heads/*:refs/remotes/origin/*" returned status code 128:
stdout: 
stderr: Permission denied, please try again.
Permission denied, please try again.
Permission denied (publickey,gssapi-keyex,gssapi-with-mic,password).
fatal: Could not read from remote repository.

Please make sure you have the correct access rights
and the repository exists.
```

##### 排错过程

```
1、由于原本已经存在一个gitlab的连接信息认证了，在jenkins安装的全局工具git中存储，直接在jenkins所在服务器上是找不到这个git客户端的，因此在jenkins服务器上源码包安装一个新的git客户端，并且配置到jenkins-web全局工具界面中
2、源码安装git参考
https://blog.csdn.net/ld326/article/details/78034441
3、Jenkins->全局工具配置
git2.20：/var/lib/jenkins/git2.20/libexec/git-core/git
4、jenkins服务器：/etc/profile
#set Git enviroment
export PATH=/var/lib/jenkins/git2.20/libexec/git-core:$PATH
5、命令行配置全局用户
https://blog.csdn.net/themagickeyjianan/article/details/79683980
[root@filapp01 git2.20]# git config --list --global
user.name=username
user.email=username@****.com
user.password=password
credential.helper=store
[root@filapp01 git2.20]# git version
git version 2.20.GIT
6、全局已经配置但是在非root路径下拉代码还需要输入用户密码，是因为命令行生成的全局配置文件在root家目录下，
[root@filapp01 git2.20]# cat /root/.gitconfig
[user]
        name = username
        email = username@****.com
        password = password
[credential]
        helper = store
可以copy一份到当前路径git clone即可免密克隆代码了
https://www.cnblogs.com/kb342/p/5566793.html
[root@filapp01 git2.20]# cat 
bin/              .git/             .gitconfig        .git-credentials  libexec/          share/            Third-wxh/        
[root@filapp01 git2.20]# cat .gitconfig 
[credential]
name=username
email=username@****.com
password=password
helper = store
[root@filapp01 git2.20]# cat .git-credentials
http://password:username@git.****.com
```

##### 注意：

如果自己配置.git-credentials，在拉代码的时候需要手动加上用户名密码，这样明文肯定不好，建议配置.gitconfig认证

http://password:username@git.****.com

到此为止已经可以使用http协议拉代码了，这时还无法使用ssh协议，因为没有做过任何key认证

#### 2.2.3 坑3：非root的key认证

```
1、由于绕过域名+ssl的nginx服务器，直接到gitlab2_gitlab2IP,如果未进行配置时，git用户@nginx服务器的内网IP，这个是永远无法ssh登录的
[root@filapp01 git2.20]# git clone git@git.****.com:username/Third-wxh.git
Cloning into 'Third-wxh'...
git@neiwang_eth0IP's password: 
2、把当前jenkins登录用户的key打到目标gitlab2服务器的管理员用户authorized_keys
3、编写ssh的config配置文件，位置是在当前jenkins服务器登录用户的家目录.ssh下新建一个config
[root@filapp01 git2.20]# cat /root/.ssh/config
host gitlab2IP   			#目标gitlab2_IP
hostname git.****.com	#与hosts中一致
user mps					#注意如果不写这个user配置默认是与当前jenkins登录用户保持一致
port 22
4、git命令测试通过
[root@filapp01 git2.20]# ssh -T git@git.****.com
Welcome to GitLab, username!
此时已经实现ssh协议免密拉代码了
```

如果遇到如下报错：

```
[root@filapp01 git2.20]# ssh -T git@git.****.com
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@       WARNING: POSSIBLE DNS SPOOFING DETECTED!          @
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
The RSA host key for git.****.com has changed,
and the key for the corresponding IP address gitlab2IP
is unchanged. This could either mean that
DNS SPOOFING is happening or the IP address for the host
and its host key have changed at the same time.
Offending key for IP in /root/.ssh/known_hosts:64
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!     @
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
IT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY!
Someone could be eavesdropping on you right now (man-in-the-middle attack)!
It is also possible that the RSA host key has just been changed.
The fingerprint for the RSA key sent by the remote host is
7e:9c:6e:84:bc:ae:82:da:c1:d1:7c:60:9c:f6:2f:5f.
Please contact your system administrator.
Add correct host key in /root/.ssh/known_hosts to get rid of this message.
Offending key in /root/.ssh/known_hosts:63
RSA host key for git.****.com has changed and you have requested strict checking.
Host key verification failed.
```

到/root/.ssh/known_hosts找到对应的63行，删除该行即可，意思是之前已经登录认证过一次了登录时hostname：git.****.com会解析到nginx服务器上去，造成报错。

总之基本只要第一次使用一个新的git工具拉代码成功，以后也会成功，但是一个git工具最好不要保存多个不同的git的url地址，之间会互相冲突影响，造成2边代码都无法拉取的问题，以后我们更加了解git的配置会更加灵活的配置和使用。

## 三、参考文档

##### 1.同一客户端多个git账号的配置：https://www.jianshu.com/p/98c7be684c1a

##### 2.git源码安装(linux)：https://blog.csdn.net/ld326/article/details/78034441

##### 3.linux下每次git clone不需输入账号密码的方法：https://www.cnblogs.com/kb342/p/5566793.html

##### 4.Jenkins的错误“error fetching remote repo origin”的问题解决：https://www.cnblogs.com/EasonJim/p/6266892.html



