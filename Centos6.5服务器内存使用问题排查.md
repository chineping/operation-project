# Centos6.5服务器内存使用问题排查

## 一、更换阿里云yum源

```
1、备份:
mv /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo.backup
2、下载新的CentOS-Base.repo 到/etc/yum.repos.d/
wget -O /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-6.repo
或者
curl -o /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-6.repo
3、之后运行生成缓存： yum makecache
--------------------- 
作者：kozazyh 
来源：CSDN 
原文：https://blog.csdn.net/kozazyh/article/details/79432394 
版权声明：本文为博主原创文章，转载请附上博文链接！
```

## 二、下载memstat源码包

```
1、yum安装失败
2、rpm安装需要一些依赖库so
3、源码包安装 #提前yum -y install gcc，用于编译
wget http://ftp.debian.org/debian/pool/main/m/memstat/memstat_1.1.tar.gz
tar -zxvf memstat_1.1.tar.gz
cd memstattool/      #这时看到debian不要被吓到了，在centos也可以正常安装使用
make && make install
```

## 三、memstat查看内存并分析

```
memstat -w | sort -rn|more
```