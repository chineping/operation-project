# Ubuntu 16.04下Shadowsocks服务器安装

## 背景

由于我大长城防火墙和公司网络的限制，既无法使用Google搜索，也无法查询到一些博客类科技性文章，于是自建ss服务器fanqiang成了一个刚需。

## 基础环境

```
国外服务器：阿里云 新加坡A  #选择阿里云是因为腾讯云的境外服务器还需使用国际站点注册，实在糟心
操作系统：Ubuntu 16.04
```

## 搭建过程

主要参考：https://segmentfault.com/a/1190000012910949

## 安全问题考虑

##### 1、被端口扫描？

服务器初始化的时候，更改远程登录端口，不使用默认的22号端口；

在设置shadowsocks.json时也设置成不常用的10000+以上端口，防止被批量扫描；

并且通过阿里云自带的安全组配置，仅放开远程登录端口及shadowsocks使用端口。

##### 2、登录账号和密码太简单，被暴力破解？

服务器初始化的时候，更改远程登录账号，不要使用默认的root用户，而且最好使用key登录的方式。直接禁止远程密码认证，就算紧急情况下，也可以通过阿里云自带的控制台登录进行处理。

##### 3、shadowsocks无故down掉？

一定要配置开机自启动和使用自启动的服务管理工具了，supervisor或systemctl均可。

```
cat /etc/supervisor.d/conf.d/shadowsocks.ini
[program:shadowsocks]
command=ssserver -c /etc/shadowsocks.json
autostart=true
autorestart=true
user=root
log_stderr=true
logfile=/var/log/shadowsocks.log
```



```
cat /etc/systemd/system/shadowsocks-server.service
[Unit]
Description=Shadowsocks Server
After=network.target

[Service]
ExecStart=/usr/local/bin/ssserver -c /etc/shadowsocks/config.json
Restart=on-abort

[Install]
WantedBy=multi-user.target
```

## 主要参考

https://segmentfault.com/a/1190000012910949

https://www.polarxiong.com/archives/Ubuntu-16-04%E4%B8%8BShadowsocks%E6%9C%8D%E5%8A%A1%E5%99%A8%E7%AB%AF%E5%AE%89%E8%A3%85%E5%8F%8A%E4%BC%98%E5%8C%96.html