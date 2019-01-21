# Linux6.5全新安装并配置Jenkins2.107.3

## 一、背景

```
1、公司资源整合，全新安装jenkins，由于目前SIT/UAT环境使用Jenkins为2.107.3，生产环境为了与测试环境统一，指定版本2.107.3
2、需要把原windows上jenkins-jobs迁移到新jenkins上
```

## 二、Jenkins安装

### 1、yum安装

```
vim /etc/yum.repos.d/jenkins.repo
[jenkins]
name=Jenkins-stable
baseurl=http://pkg.jenkins.io/redhat-stable
gpgcheck=1
```

目前稳定版为2.150，版本太高了不采用

```
[root@jenkins-prod ~]# yum list |grep jenkins
jenkins.noarch                                 2.150.1-1.1                  jenkins
```

### 2、war包安装

```
先安装jdk1.8
useradd jenkins
passwd jenkins(用户名密码：jenkins/jenkins)
mkdir /opt/jenkins2.107
chown jenkins. /opt/jenkins2.107
下载jenkins.war(版本：2.107.3)注意要选择稳定版stable的pastVersion
su -s /bin/sh -c /opt/jenkins2.107/start_jenkins.sh jenkins
```

### 3、初始化安装插件

```
装插件就会支持相应功能，想要完全了解每个插件实现的功能及使用方法很难，只要安装我们常用的插件，并且在必要的时候安装所需的插件即可。
避免安装过多冗余的插件，每次安装完插件需要重启才能生效，因此使用thinbackup自动备份插件是十分必要的。
必备配置工具：jdk1.7 jdk1.8 maven nodejs
```

### 4、安装并配置thinbackup插件

```
http://jenkinsIP:8080/thinBackup/backupsettings
Backup directory：/opt/jenkins_bak
Backup schedule for full backups：H 12 * * 1-5
Max number of backup sets：7
勾选 Backup next build number file
勾选 Backup plugins archives
勾选 Move old backups to ZIP files
```

### 5、ssh_scp的key认证

```
windows服务器上pscp.exe支持参数：-l root -pw "密码"，但是linux服务器的scp很死心眼不支持直接加参数暴露密码的方式，需要pssh或者key认证否则就要交互式输入密码，此时key认证就尤为重要咯~
ansible推key到目标服务器
ansible-playbook push.ssh.yaml -e "{'host':MPS}" --syntax-check
ansible-playbook push.ssh.yaml -e "{'host':MPS}"
scp首次需要交互输入yes，为了避免jenkins交互scp加参数：-o StrictHostKeyChecking=no
scp要先验证一下，打包要验证一下
```

### 6、迁移原jenkins家目录有用配置

```
jobs：包括所有的jenkins上构建项目的数据，迁移后job在ALL中需要手动勾选到对应view
node：包括jenkins上的所有的从节点的数据，配置label时需要
plugins：包括所有需要使用的插件，插件需要升级，jenkins版本需一致，可以对比不同的*.jpi文件
users：包括了jenkins上所有用户数据
config.xml: 构建项目，用户权限配置文件
```

### 7、常见问题

```
scp常见问题：
对端没有scp命令，强制安装openssh：
rpm -ivh openssh-clients-5.3p1-123.el6_9.x86_64.rpm --force --nodeps
远程ssh验证失败，失去连接：
sed -ri 's/.*UseDNS\s+yes/UseDNS\tno/g;s/GSSAPIAuthentication\s+no/GSSAPIAuthentication yes/g' /etc/ssh/sshd_config 
reload_sshd服务,UseDNS导致连接特别慢
```

如有一些打包细节问题，可以参考Windows上Jenkins迁移到Linux服务器这一文档