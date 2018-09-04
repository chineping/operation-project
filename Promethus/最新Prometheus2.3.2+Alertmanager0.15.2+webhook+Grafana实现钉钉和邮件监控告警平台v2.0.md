# 最新Prometheus2.3.2+Alertmanager0.15.2+webhook+Grafana实现钉钉和邮件监控告警平台v2.0

## 一、背景

上一篇文档readme介绍了监控告警平台的搭建详细过程，此文档为迁移-搭建v2.0，有了一些改进，主要介绍其区别，细节此处不再赘述。

## 二、主要区别

##### 迁移后均采用源码包安装，安装路径比较集中，服务均为systemctl管理，缺点是没有日志输出

```
[root@VM_4_12_centos opt]# ls
alertmanager-0.15.2  go  grafana-5.2.3  node_exporter-0.16.0  prometheus-2.3.2
```

##### 迁移前路径比较分散，服务为自定义start脚本启动，可输出nohup.out日志，用于故障排查

```
[root@VM_4_5_centos opt]# ls
alertmanager-0.15.2  node_exporter-0.16.0  prometheus-2.3.2
[root@VM_4_5_centos go]# pwd
/usr/local/go
[root@VM_4_5_centos grafana]# pwd    #grafana为yum安装，比较简单
/etc/grafana
```

## 三、迁移过程

```
1、prometheus、alertmanager、node_exporter-0.16.0、go直接scp -r
2、修改prometheus、alertmanager服务绑定地址
3、node_exporter-0.16.0不涉及服务绑定地址无需修改
4、添加/etc/hosts
5、添加go的环境变量，webhook的启动脚本路径
6、源码包安装grafana
```

## 四、配置systemctl启动服务

##### 1、prometheus.service

```
[root@VM_4_12_centos ~]# cd /usr/lib/systemd/system
[root@VM_4_12_centos system]# cat prometheus.service 
[Unit]
Description=Prometheus
Documentation=https://prometheus.io/
After=network.target

[Service]
Type=simple
User=root
ExecStart=/opt/prometheus-2.3.2/prometheus --config.file=/opt/prometheus-2.3.2/prometheus.yml --storage.tsdb.path=/opt/prometheus-2.3.2/data/prometheus --storage.tsdb.retention=30d --web.enable-lifecycle
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

##### 2、alertmanager.service 

```
[root@VM_4_12_centos system]# cat alertmanager.service 
[Unit]
Description=Prometheus
Documentation=https://prometheus.io/
After=network.target

[Service]
Type=simple
User=root
ExecStart=/opt/alertmanager-0.15.2/alertmanager --config.file=/opt/alertmanager-0.15.2/alertmanager.yml --storage.path=/opt/alertmanager-0.15.2/data/ --log.level=info --web.listen-address=0.0.0.0:9093 --data.retention=120h
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

##### 3、grafana-server.service

```
[root@VM_4_12_centos system]# cat grafana-server.service 
[Unit]
Description=Grafana instance
Documentation=http://docs.grafana.org
Wants=network-online.target
After=network-online.target
After=postgresql.service mariadb.service mysql.service

[Service]
EnvironmentFile=/etc/sysconfig/grafana-server
User=root
Group=root
Type=notify
Restart=on-failure
WorkingDirectory=/opt/grafana-5.2.3
RuntimeDirectory=grafana-5.2.3
RuntimeDirectoryMode=0750
ExecStart=/opt/grafana-5.2.3/bin/grafana-server                                     \
                            --config=${CONF_FILE}                                   \
                            --pidfile=${PID_FILE_DIR}/grafana-server.pid            \
                            cfg:default.paths.logs=${LOG_DIR}                       \
                            cfg:default.paths.data=${DATA_DIR}                      \
                            cfg:default.paths.plugins=${PLUGINS_DIR}                \
                            cfg:default.paths.provisioning=${PROVISIONING_CFG_DIR}  

LimitNOFILE=10000
TimeoutStopSec=20

[Install]
WantedBy=multi-user.target
```

##### 4、node_exporter.service

```
[root@VM_4_12_centos system]# cat node_exporter.service 
[Unit]
Description=Node Exporter

[Service]
User=root
EnvironmentFile=/opt/node_exporter-0.16.0/node_exporter.conf
ExecStart=/opt/node_exporter-0.16.0/node_exporter $OPTIONS

[Install]
WantedBy=multi-user.target
```

## 五、迁移前考虑因素

```
1、prometheus数据量大小，是否保留   #/opt/prometheus-2.3.2/data/prometheus
2、alertmanager数据量大小，是否保留 #/opt/alertmanager-0.15.2/data
3、grafana配置数据大小，是否保留    #/var/lib/grafana/grafana.db
4、迁移工具的选择(scp/nc/rsync)，主要取决于网络环境、数据量大小
5、迁移后与被监控机器的网络连通性
```

## 六、简单介绍源码包安装grafana

##### 1、解压源码包之后

```
[root@VM_4_12_centos conf]# pwd
/opt/grafana-5.2.3/conf
cp -a defaults.ini grafana.ini   #主要修改绑定ip和端口
[server]
http_port = 3000
domain = ip
[smtp]
enabled = true
host = smtp.163.com:465
user = 邮箱用户
password = 邮箱密码
skip_verify = true
from_address = 邮箱账号
from_name = Grafana
[emails]
welcome_email_on_sign_up = true
```

##### 2、新建系统配置文件

```
[root@VM_4_12_centos conf]# cat /etc/sysconfig/grafana-server 
GRAFANA_USER=root
GRAFANA_GROUP=root
GRAFANA_HOME=/opt/grafana-5.2.3
LOG_DIR=/opt/grafana-5.2.3/data/log
DATA_DIR=/opt/grafana-5.2.3/data
MAX_OPEN_FILES=10000
CONF_DIR=/opt/grafana-5.2.3/conf
CONF_FILE=/opt/grafana-5.2.3/conf/grafana.ini
RESTART_ON_UPGRADE=true
PLUGINS_DIR=/opt/grafana-5.2.3/data/plugins
PROVISIONING_CFG_DIR=/opt/grafana-5.2.3/conf/provisioning
# Only used on systemd systems
PID_FILE_DIR=/opt/grafana-5.2.3/data/pid    #此处为自定义，需要建立这个路径
```

##### 3、新建服务启动配置文件

```
[root@VM_4_12_centos system]# cat grafana-server.service 
[Unit]
Description=Grafana instance
Documentation=http://docs.grafana.org
Wants=network-online.target
After=network-online.target
After=postgresql.service mariadb.service mysql.service

[Service]
EnvironmentFile=/etc/sysconfig/grafana-server
User=root
Group=root
Type=notify
Restart=on-failure
WorkingDirectory=/opt/grafana-5.2.3
RuntimeDirectory=grafana-5.2.3
RuntimeDirectoryMode=0750
ExecStart=/opt/grafana-5.2.3/bin/grafana-server                                     \
                            --config=${CONF_FILE}                                   \
                            --pidfile=${PID_FILE_DIR}/grafana-server.pid            \
                            cfg:default.paths.logs=${LOG_DIR}                       \
                            cfg:default.paths.data=${DATA_DIR}                      \
                            cfg:default.paths.plugins=${PLUGINS_DIR}                \
                            cfg:default.paths.provisioning=${PROVISIONING_CFG_DIR}  

LimitNOFILE=10000
TimeoutStopSec=20

[Install]
WantedBy=multi-user.target
```

4、启动服务

```
systemctl start grafana-server
```

