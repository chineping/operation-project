# ELK+Filebeat日志分析平台环境部署

本日志平台主要用于收集、传输、存储和分析主要应用程序日志，对收集到的日志进行格式化输出和图形化展示，并且对检测到的“ERROR”字段进行邮件告警。

### 一、完整的日志数据具有非常重要的作用

1）信息查找。通过检索日志信息，定位相应的bug，找出解决方案。
2）服务诊断。通过对日志信息进行统计、分析，了解服务器的负荷和服务运行状态，找出耗时请求进行优化等等。
3）数据分析。如果是格式化的log，可以做进一步的数据分析，统计、聚合出有意义的信息，比如根据请求中的商品id，找出TOP10用户感兴趣商品。

###  二、采用FiletBeats 作为日志搜集器

这种架构解决了 Logstash 在各服务器节点上占用系统资源高的问题。相比 Logstash，Beats 所占系统的 CPU 和内存几乎可以忽略不计。

![filebeat+elk](E:\个人文件\elk搭建\filebeat+elk.png)

因为免费的 ELK 没有任何安全机制，所以这里使用了 Nginx 作反向代理，避免用户直接访问 Kibana 服务器。加上配置 Nginx 实现简单的用户认证，一定程度上提高安全性。另外，Nginx 本身具有负载均衡的作用，能够提高系统访问性能。

### 三、系统信息

#### 基础环境

本环境采用Centos 7.4 x64，OpenJDK 1.8

#### 软件版本

- Filebeat：5.6.9；
- Logstash：5.6.9；
- Elasticsearch：5.6.9；
- Kibana：5.6.9；
- Nginx：1.12.2。

### 四、Filebeat+ELK安装过程

#### 1）playbook批量安装Filebeat

本环境已经在运维服务器部署hosts、playbook：

/etc/ansible/hosts

/etc/ansible/playbooks/install_filebeat.yaml 

使用方法如下（需要把'host':*变更为具体的host配置）：

ansible-playbook install_filebeat.yaml --extra-vars "{'host':*}"

```
[root@VM_4_5_centos filebeat]# tree -a /etc/ansible/roles/filebeat/
|-- defaults
|   `-- main.yml
|-- files
|-- handlers
|   `-- main.yml
|-- meta
|-- tasks
|   `-- main.yml
|-- templates
|   |-- filebeat.repo.j2
|   |-- filebeat.yml.j2
`-- vars
```

##### 主要配置文件

[root@VM_2_15_centos ~]# grep -vP '^$|^#|^  #' /etc/filebeat/filebeat.yml

```
filebeat.prospectors:
- input_type: log
  paths:
    #- /var/log/*.log
     - /opt/jboss-4.2.3.GA/bin/ifcsmg.out
  fields: 
      service: "xxx"  
output.logstash:
  hosts: ["x.x.x.x:5044"]
```

##### 测试配置文件语法是否正确

```
[root@VM_2_15_centos bin]# cd /usr/bin/
[root@VM_2_15_centos bin]# ./filebeat.sh -configtest -e
2018/05/18 02:07:58.588928 beat.go:297: INFO Home path: [/usr/share/filebeat] Config path: [/etc/filebeat] Data path: [/var/lib/filebeat] Logs path: [/var/log/filebeat]
2018/05/18 02:07:58.588954 beat.go:192: INFO Setup Beat: filebeat; Version: 5.6.9
2018/05/18 02:07:58.589030 metrics.go:23: INFO Metrics logging every 30s
2018/05/18 02:07:58.589042 logstash.go:91: INFO Max Retries set to: 3
2018/05/18 02:07:58.589094 outputs.go:108: INFO Activated logstash as output plugin.
2018/05/18 02:07:58.589150 publish.go:300: INFO Publisher name: VM_2_15_centos
2018/05/18 02:07:58.589242 async.go:63: INFO Flush Interval set to: 1s
2018/05/18 02:07:58.589252 async.go:64: INFO Max Bulk Size set to: 2048
Config OK
```

##### 服务命令路径、日志路径

/usr/share/filebeat

/var/log/filebeat/filebeat

##### 服务启动命令

systemctl start filebeat.service

#### 2）ELK服务端系统环境

最大文件描述符(如果用户级别的1024太小要求是65536以上)，本环境默认为100001无需修改：

```
[root@VM_4_8_centos ~]# tail /etc/security/limits.conf -n4
* soft nofile 100001
* hard nofile 100002
root soft nofile 100001
root hard nofile 100002
```

#### 3）ELK服务端yum安装Logstash

[root@VM_4_8_centos yum.repos.d]# cat logstash.repo 

```
[logstash-5.x]
name=Elastic repository for 5.x packages
baseurl=https://artifacts.elastic.co/packages/5.x/yum
gpgcheck=1
gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
enabled=1
autorefresh=1
type=rpm-md
```

##### 主要配置文件（Port：5044）

[root@VM_4_8_centos ~]# grep -vP '^$|^#|^  #' /etc/logstash/logstash.yml

```
path.data: /var/lib/logstash
path.config: /etc/logstash/conf.d
http.host: "x.x.x.x"
path.logs: /var/log/logstash
```

[root@VM_4_8_centos bin]# cat /usr/share/logstash/bin/all.conf.email

```
input {
   beats {
        port => 5044
         }
}
output {
      if [message] =~ "ERROR" {
        email {
           port => "587"
           #address => "smtp.***.com"
           address => "ip"
           authentication => "plain"
           subject => "警告:%{[service]} 出现 Jboss Log Error"
           username => "username"
           password => "password"
           from => "username@macroflag.com"
           to => "username1@macroflag.com,username2@macroflag.com"
           via => "smtp"
           body => "主机:%{[service]} \n 日志类型:%{type} \n error_message:%{message}"
                }
                              }
 }
```

##### 服务命令路径、日志路径

/usr/share/logstash/

/var/log/logstash/logstash-plain.log

##### 服务启动命令

bash /usr/share/logstash/bin/start_logstash.sh

#### 4）ELK服务端tar包安装Elasticsearch

```
tar -zxvf elasticsearch-5.6.9.tar.gz -C /opt/
groupadd elasticsearch
useradd elasticsearch -g elasticsearch
chown elasticsearch. /opt/elasticsearch-5.6.9/ -R
ln -s /etc/logstash/ /usr/share/logstash/config
```

##### 主要配置文件（Port：9200，9300）

grep -vP '^$|^#|^  #' /opt/elasticsearch-5.6.9/config/elasticsearch.yml

```
network.host: x.x.x.x
```

##### 服务启动命令（使用elasticsearch用户启动）

bash /opt/elasticsearch-5.6.9/bin/start_elasticsearch.sh

```
#!/bin/bash
nohup su - elasticsearch -s /bin/sh /opt/elasticsearch-5.6.9/bin/elasticsearch >> nohup.out 2>&1 &
```

#### 5）ELK服务端yum安装Kibana

[root@VM_4_8_centos yum.repos.d]# cat kibana.repo	

```
[kibana-5.x]
name=Kibana repository for 5.x packages
baseurl=https://artifacts.elastic.co/packages/5.x/yum
gpgcheck=1
gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
enabled=1
autorefresh=1
type=rpm-md
```

##### 主要配置文件（Port：5601）

 grep -vP '^$|^#|^  #' /etc/kibana/kibana.yml 

```
server.host: "127.0.0.1"
elasticsearch.url: "http://x.x.x.x:9200"
```

##### 服务命令路径、日志路径

/usr/share/kibana/

/var/log/kibana/kibana.stdout

##### 服务启动命令

service kibana start

