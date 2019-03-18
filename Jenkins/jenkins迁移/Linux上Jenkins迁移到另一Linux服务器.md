# Linux上Jenkins迁移到另一Linux服务器

## 一、背景

```
资源整合，搭建在不同Linux上的Jenkins的job迁移，这里注意测试与生产环境需要开通2套权限
操作系统环境源&目标：CentOS release 6.8 & CentOS release 6.5
Jenkins版本源&目标：2.138 & 2.107
```

## 二、迁移操作

### 2.1 开通网络环境

```
源：运维人员内网IP    目标：源Jenkins服务器（jenkins8080、远程ssh22）
源：目标Jenkins服务器 目标：源Jenkins服务器job所涉及管理服务器IP
源：目标Jenkins服务器 目标：源Jenkins服务器（单向通即可用于job import）
```

### 2.2 配置权限认证以及源Jenkins工具

```
这一步一定要在迁移前先做呀,否则job import之后为空，还要一个个勾选或者修改job配置文件~
目标Jenkins：
远程登录：系统管理->系统配置->远程ssh 或 SSH Server
Credentials：源Jenkins服务器登录用户密码、远程ssh服务器用户密码、svn/gitlab拉代码用户密码
maven/jdk/nodejs工具安装：与源Jenkins上版本保持一致
```

### 2.3 目标Jenkins安装并配置插件

```
job import plugin 并重启
Jenkins->系统配置->Job Import Plugin->Jenkins登录用户密码 配置
Jenkins->Job Import Plugin-> 什么都不选直接Query，勾选需要迁移的job
新建简单类型view，勾选job
修改maven的setting.xml配置文件：/var/lib/jenkins/conf/isb/settings.xml(注意<localRepository>/var/lib/jenkins/.m2/repository_isb</localRepository>)
```

### 2.4 测试

```
拉代码 && 构建 && 传输 OK~
```