## [CentOS 7 yum安装Zabbix]

## 一、安装LAMP环境

```
yum install php php-mysql php-common php-gd php-mbstring php-mcrypt php-devel php-xml httpd
```

## 二、安装zabbix-server

```
rpm --import http://repo.zabbix.com/RPM-GPG-KEY-ZABBIX
cd /data/
wget http://repo.zabbix.com/zabbix/3.5/rhel/7/x86_64/zabbix-release-3.5-1.el7.noarch.rpm
rpm -ivh zabbix-release-3.5-1.el7.noarch.rpm 
yum install zabbix-server-mysql zabbix-web-mysql zabbix-agent zabbix-java-gateway
cd /etc/httpd/conf.d/
vim zabbix.conf 
systemctl restart httpd.service 
```

## 三、安装Mariadb

```
cd /etc/yum.repos.d/
vim Mariadb.repo
yum -y install MariaDB-server MariaDB-client
systemctl start mariadb.service
systemctl enable mariadb.service
mysql -uroot -p
cd /etc/my.cnf.d/
vim server.cnf
vim client.cnf
systemctl restart mariadb.service
mysql -uroot -p
```

配置Mariadb：

```
MariaDB [(none)]> create database zabbix character set utf8;
Query OK, 1 row affected (0.00 sec)

MariaDB [(none)]> grant all privileges on zabbix.* to 'zabbix'@'localhost' identified by 'zabbix';
Query OK, 0 rows affected (0.00 sec)

MariaDB [(none)]> flush privileges;
Query OK, 0 rows affected (0.00 sec)
```

配置DB-Database：

```
cd /usr/share/doc/zabbix-server-mysql-4.0.0/
cp -a create.sql.gz /data/
cd /data/
gunzip create.sql.gz
mysql -uzabbix -hlocalhost -p zabbix < create.sql
vim /etc/zabbix/zabbix_server.conf
systemctl restart zabbix-server.service
grep Asia /etc/httpd/conf.d/zabbix.conf
yum install zabbix-server-mysql zabbix-web-mysql
vim /etc/zabbix/zabbix_server.conf
systemctl restart zabbix-server.service
```

设置访问路径为跟路径：

```
cp -a /usr/share/zabbix/* /var/www/html/
vim /etc/php.ini
systemctl restart httpd.service
```

测试访问：

```
http://10.90.4.5
账号：admin 密码：zabbix
```

修改mysql data数据路径

主要参考：

https://blog.csdn.net/xiaoyiaoyou/article/details/52420855

出现socket连接报错：

```
[root@VM_4_5_centos mysql]# mysql_config --socket
/var/lib/mysql/mysql.sock
解决：
ln -s /data/mysql/mysql.sock /var/lib/mysql/mysql.sock
```



参考：

https://blog.csdn.net/OH_ON/article/details/78696782

http://club.oneapm.com/t/zabbix-server-zabbix-agent/428

https://www.cnblogs.com/lclq/p/5760966.html

http://www.cnblogs.com/xqzt/p/5124894.html