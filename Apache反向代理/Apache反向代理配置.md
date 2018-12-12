# Apache反向代理配置

模拟浙商生产环境，银联使用apache反向代理到我们业务前端入口。

## 一、部署环境

```
操作系统：el7.x86_64 10.89.1.4
真正业务：10.11.73.11:8180
apache版本：2.4.6
安装方式：源码包（模拟银联）
安装路径：/alidata/httpd
域名：czbank.wowfintech.net绑定在前端clb：211.159.243.79
ssl：放在前端clb
数据流：
域名-->clb:80-->服务器:80-->apache反向代理-->真正业务：8180
域名-->clb:443-->服务器:8180-->apache反向代理-->真正业务：8180
```

## 二、apache主配置文件

```
[root@VM_1_4_centos vhosts]# grep -vP '^#|^$|^    #' /alidata/httpd/conf/httpd.conf
ServerRoot "/alidata/httpd"
Listen 80
Listen 8180
LoadModule authn_file_module modules/mod_authn_file.so
LoadModule authn_core_module modules/mod_authn_core.so
LoadModule authz_host_module modules/mod_authz_host.so
LoadModule authz_groupfile_module modules/mod_authz_groupfile.so
LoadModule authz_user_module modules/mod_authz_user.so
LoadModule authz_core_module modules/mod_authz_core.so
LoadModule access_compat_module modules/mod_access_compat.so
LoadModule auth_basic_module modules/mod_auth_basic.so
LoadModule reqtimeout_module modules/mod_reqtimeout.so
LoadModule filter_module modules/mod_filter.so
LoadModule mime_module modules/mod_mime.so
LoadModule log_config_module modules/mod_log_config.so
LoadModule env_module modules/mod_env.so
LoadModule headers_module modules/mod_headers.so
LoadModule setenvif_module modules/mod_setenvif.so
LoadModule version_module modules/mod_version.so
LoadModule proxy_module modules/mod_proxy.so
LoadModule proxy_connect_module modules/mod_proxy_connect.so
LoadModule proxy_http_module modules/mod_proxy_http.so
LoadModule slotmem_shm_module modules/mod_slotmem_shm.so
LoadModule unixd_module modules/mod_unixd.so
LoadModule status_module modules/mod_status.so
LoadModule autoindex_module modules/mod_autoindex.so
LoadModule dir_module modules/mod_dir.so
LoadModule alias_module modules/mod_alias.so
LoadModule rewrite_module modules/mod_rewrite.so
<IfModule unixd_module>
User www
Group www
</IfModule>
ServerAdmin you@example.com
ServerName czbank.wowfintech.net:80
DocumentRoot "/"
<Directory "/">
    Options Indexes FollowSymLinks
    AllowOverride None
    Require all granted
</Directory>
<IfModule dir_module>
    DirectoryIndex index.html
</IfModule>
<Files ".ht*">
    Require all denied
</Files>
ErrorLog "logs/error_log"
LogLevel warn
<IfModule log_config_module>
    LogFormat "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"" combined
    LogFormat "%h %l %u %t \"%r\" %>s %b" common
    <IfModule logio_module>
      # You need to enable mod_logio.c to use %I and %O
      LogFormat "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" %I %O" combinedio
    </IfModule>
    CustomLog "| /alidata/httpd/bin/rotatelogs logs/%Y_%m_%d_access.log 86400 480" combined
</IfModule>
<IfModule alias_module>
    ScriptAlias /cgi-bin/ "/alidata/httpd/cgi-bin/"
</IfModule>
<IfModule cgid_module>
</IfModule>
<Directory "/alidata/httpd/cgi-bin">
    AllowOverride None
    Options None
    Require all granted
</Directory>
<IfModule mime_module>
    TypesConfig conf/mime.types
    AddType application/x-compress .Z
    AddType application/x-gzip .gz .tgz
</IfModule>
Include conf/extra/httpd-mpm.conf
Include conf/extra/httpd-vhosts.conf
<IfModule proxy_html_module>
</IfModule>
<IfModule ssl_module>
SSLRandomSeed startup builtin
SSLRandomSeed connect builtin
</IfModule>
HostnameLookups off
```

## 三、反向代理配置文件

```
[root@VM_1_4_centos vhosts]# cat /alidata/httpd/conf/vhosts/czbank.conf 
<VirtualHost 10.89.1.4:80>
ProxyRequests Off
ProxyPreserveHost On
ProxyPass / http://10.11.73.11:8180/
ProxyPassReverse / http://10.11.73.11:8180/
ServerName czbank.wowfintech.net
  <Directory />
    Options FollowSymLinks
    AllowOverride None
    Order allow,deny
    Allow from all
  </Directory>
</VirtualHost>

<VirtualHost 10.89.1.4:8180>
ProxyRequests Off
ProxyPreserveHost On
ProxyPass  / http://10.11.73.11:8180/ timeout=90
ProxyPassReverse  / http://10.11.73.11:8180/
ServerName czbank.wowfintech.net
  <Directory />
    Options FollowSymLinks
    AllowOverride None
    Order allow,deny
    Allow from all
  </Directory>
</VirtualHost>
```