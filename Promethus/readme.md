# 最新Prometheus2.3.2+Alertmanager0.15.2+webhook+Grafana实现钉钉和邮件监控告警平台

## 一、二进制包安装Prometheus  

我们可以到 Prometheus 二进制安装包[下载页面](https://prometheus.io/download/)，根据自己的操作系统选择下载对应的安装包。

### 环境准备

- linux centos7.4 (3.10.0-693.el7.x86_64)
- prometheus-2.3.2

### 下载 Prometheus Server

创建下载目录,以便安装过后清理掉

```
使用 wget 下载 Prometheus 的安装包，但是限于网络问题，先下载，再上传
wget https://github.com/prometheus/prometheus/releases/download/v2.3.2/prometheus-2.3.2.linux-amd64.tar.gz
```

使用 tar 解压缩 prometheus-1.6.2.linux-amd64.tar.gz

```
tar -xvzf ~/Download/prometheus-1.6.2.linux-amd64.tar.gz
cd prometheus-1.6.2.linux-amd64

```

当解压缩成功后，可以运行 version 检查运行环境是否正常

```
./prometheus --version
```

如果你看到类似输出，表示你已安装成功:

```
prometheus, version 2.3.2 (branch: HEAD, revision: 71af5e29e815795e9dd14742ee7725682fa14b7b)
  build user:       root@5258e0bd9cc1
  build date:       20180712-14:02:52
  go version:       go1.10.3
```

### 启动 Prometheus Server

```
./prometheus

```

如果 prometheus 正常启动，你将看到如下信息：

```
INFO[0000] Starting prometheus (version=2.3.2, branch=master, revision=b38e977fd8cc2a0d13f47e7f0e17b82d1a908a9a)  source=main.go:88
INFO[0000] Build context (go=go1.8.1, user=root@c99d9d650cf4, date=20170511-13:03:00)  source=main.go:89
INFO[0000] Loading configuration file prometheus.yml     source=main.go:251
INFO[0000] Loading series map and head chunks...         source=storage.go:421
INFO[0000] 0 series loaded.                              source=storage.go:432
INFO[0000] Starting target manager...                    source=targetmanager.go:61
INFO[0000] Listening on :9090                            source=web.go:259
```

通过启动日志，可以看到 Prometheus Server 默认端口是 9090。

当 Prometheus 启动后，你可以通过浏览器来访问 `http://IP:9090`，将看到如下页面

![prometheus-graph.png](http://7o512j.com1.z0.glb.clouddn.com/prometheus-graph.png)

在默认配置中，我们已经添加了 Prometheus Server 的监控，所以我们现在可以使用 `PromQL`（Prometheus Query Language）来查看，比如：

![prometheus-console.png](http://7o512j.com1.z0.glb.clouddn.com/Screen%20Shot%202017-05-17%20at%202.29.19%20PM.png)

### 配置prometheus.yml

```
vim /opt/prometheus-2.3.2/prometheus.yml  #增加以下配置
scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['IP:9090']
  - job_name: 'localhost'
    static_configs:
      - targets: ['localhost:9100']
```

### 把prometheus配置成服务

```
vim /usr/lib/systemd/system/prometheus.service
[Unit]
Description=Prometheus
Documentation=https://prometheus.io/
After=network.target

[Service]
Type=simple
User=root
ExecStart=/opt/prometheus-2.3.2/prometheus --config.file="/opt/prometheus-2.3.2/prometheus.yml" --storage.tsdb.path="/opt/prometheus-2.3.2/data/prometheus" --storage.t
sdb.retention=15d --web.enable-lifecycle
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

systemctl status prometheus.service

### 把prometheus配置成supervisor管理

```
python 第三方包的安装方法：
pip install supervisor
echo_supervisord_conf > /etc/supervisord.conf
[root@VM_4_5_centos ~]# grep -vP '^$|^#|^;' /etc/supervisord.conf
[unix_http_server]
file=/etc/supervisor/supervisor.sock   ; the path to the socket file
username=root              ; default is no username (open server)
password=***               ; default is no password (open server)
[inet_http_server]         ; inet (TCP) server disabled by default
port=IP:9001        ; ip_address:port specifier, *:port for all iface
username=root              ; default is no username (open server)
password=***              ; default is no password (open server)
[supervisord]
logfile=/etc/supervisor/supervisord.log ; main log file; default $CWD/supervisord.log
logfile_maxbytes=50MB        ; max main logfile bytes b4 rotation; default 50MB
logfile_backups=10           ; # of main logfile backups; 0 means none, default 10
loglevel=info                ; log level; default info; others: debug,warn,trace
pidfile=/etc/supervisor/supervisord.pid ; supervisord pidfile; default supervisord.pid
nodaemon=false               ; start in foreground if true; default false
minfds=65535                  ; min. avail startup file descriptors; default 1024
minprocs=200                 ; min. avail process descriptors;default 200
user=root                ; default is current user, required if root
[rpcinterface:supervisor]
supervisor.rpcinterface_factory = supervisor.rpcinterface:make_main_rpcinterface
[supervisorctl]
serverurl=unix:///etc/supervisor/supervisor.sock ; use a unix:// URL  for a unix socket
serverurl=http://IP:9001 ; use an http:// url to specify an inet socket
username=root              ; should be same as in [*_http_server] if set
password=***                ; should be same as in [*_http_server] if set
[include]
files = /etc/supervisor/*.ini
```

注意：不要忘记去掉[include]前面的;

### 配置prometheus.ini 

```
[root@VM_4_5_centos supervisor]# cat /etc/supervisor/prometheus.ini 
[program:prometheus]
command=/opt/prometheus-2.3.2/prometheus --config.file=/opt/prometheus-2.3.2/prometheus.yml --storage.tsdb.path=/opt/prometheus-2.3.2/data/prometheus
directory=/opt/prometheus-2.3.2
user=root
autorestart=true  
redirect_stderr=true
stdout_logfile = /var/log/supervisor/prometheus.log
loglevel=info
```

systemctl start supervisor.service

supervisorctl update

systemctl status prometheus.service

### 总结

1. 可以看出 Prometheus 二进制安装非常方便，没有依赖，自带查询 web 界面。
2. 在生产环境中，我们可以将 Prometheus 添加到 init 配置里，或者使用 supervisord 作为服务自启动。

## 二、Promethus的主要配置

参考：https://github.com/yunlzheng/prometheus-book

### Prometheus 2.0的新特性

```
2.0的性能得到了巨大的提升
与Prometheus 1.8对比，CPU使用率降低了20%~40%
与Prometheus 1.8对比，磁盘空间占用降低了33%~50%
低负载查询的磁盘I/O平均小于1%
Prometheus 2.0内置了数据库快照备份功能
告警规则从自定义格式迁移到yaml格式，使用2.0的promtool工具可以完成格式的转换

Prometheus适用的场景
Prometheus在记录纯数字时间序列方面表现非常好。它既适用于面向服务器等硬件指标的监控，也适用于高动态的面向服务架构的监控。对于现在流行的微服务，Prometheus的多维度数据收集和数据筛选查询语言也是非常的强大。Prometheus是为服务的可靠性而设计的，当服务出现故障时，它可以使你快速定位和诊断问题。它的搭建过程对硬件和服务没有很强的依赖关系。

Prometheus不适用的场景
Prometheus它的价值在于可靠性，甚至在很恶劣的环境下，你都可以随时访问它和查看系统服务各种指标的统计信息。 如果你对统计数据需要100%的精确，它并不适用，例如：它不适用于实时计费系统。

Prometheus服务过程大概是这样：
Prometheus Daemon负责定时去目标上抓取metrics(指标)数据，每个抓取目标需要暴露一个http服务的接口给它定时抓取。Prometheus支持通过配置文件、文本文件、Zookeeper、Consul、DNS SRV Lookup等方式指定抓取目标。Prometheus采用PULL的方式进行监控，即服务器可以直接通过目标PULL数据或者间接地通过中间网关来Push数据。
Prometheus在本地存储抓取的所有数据，并通过一定规则进行清理和整理数据，并把得到的结果存储到新的时间序列中。
Prometheus通过PromQL和其他API可视化地展示收集的数据。Prometheus支持很多方式的图表可视化，例如Grafana、自带的Promdash以及自身提供的模版引擎等等。Prometheus还提供HTTP API的查询方式，自定义所需要的输出。
PushGateway支持Client主动推送metrics到PushGateway，而Prometheus只是定时去Gateway上抓取数据。
Alertmanager是独立于Prometheus的一个组件，可以支持Prometheus的查询语句，提供十分灵活的报警方式。
```

### 配置prometheus.yml

```
vim /opt/prometheus-2.3.2/prometheus.yml  
#全局配置
global:
  scrape_interval: 15s     #Server端抓取数据的时间间隔
  scrape_timeout: 10s      #数据抓取的超时时间 
  evaluation_interval: 15s #评估报警规则的时间间隔

  external_labels:
    monitor: 'prometheus-monitor' #邮件告警时可以显示
#prometheus连接alertmanagers告警配置
alerting:
  alertmanagers:
  - static_configs:
    - targets:
      - localhost:9093            #localhost也可以替换成ip或hostname(配本地/etc/hosts)
#自定义告警规则
rule_files:
#  - '/opt/prometheus-2.3.2/rules/1.yml'
  - /opt/prometheus-2.3.2/rules/*.rules
#pull抓取客户端配置
scrape_configs:
  - job_name: 'prometheus'        #客户端分组
    scrape_interval: 5s           #抓取间隔 
    static_configs:
      #- targets: ['IP:9100'] #可以写ip
      - targets: ['IP_yunwei:9100'] #配本地/etc/hosts支持hostname,grafana监控/alertmanager告警中会显示，起到备注作用

  - job_name: 'jxbank'
    scrape_interval: 5s
    static_configs:
      - targets: ['IP1_hostname:9100','IP2_hostname:9100']
```

### 配置prometheus告警规则hostPerformanceAlert.rules

如果指定特定一个规则文件*.yml(不能匹配多个)，如果匹配多个*.rules

```
cat /opt/prometheus-2.3.2/rules/hostPerformanceAlert.rules 
groups:
- name: hostPerformanceAlert    #自定义规则组名
  rules:
  - alert: hostCpuUsageAlert    #自定义alertname
    expr: (sum(avg without (cpu)(irate(node_cpu{mode!='idle'}[5m]))) by (instance)) * 100 > 75
    #expr: (100 - (avg by (instance) (irate(node_cpu{job="kubernetes-node-exporter",mode="idle"}[5m])) * 100)) > 75
    for: 5m                     #告警持续5min
    labels:
      severity: page
    annotations:
      summary: "Instance {{ $labels.instance }} CPU usgae high"
      description: "{{ $labels.instance }} CPU usage above 75% (current value: {{ $value }})"
  - alert: hostMemUsageAlert
    #expr: (node_memory_MemTotal - node_memory_MemAvailable)/node_memory_MemTotal > 0.7
    expr: (node_memory_MemTotal - (node_memory_MemFree+node_memory_Buffers+node_memory_Cached )) / node_memory_MemTotal * 100 > 70
    for: 5m
    labels:
      severity: page
    annotations:
      summary: "Instance {{ $labels.instance }} MEM usgae high"
      description: "{{ $labels.instance }} MEM usage above 70% (current value: {{ $value }})"
  - alert: hostDiskUsageAlert
    expr: ceil(node_filesystem_avail_bytes{mountpoint="/", device="/dev/vda1"} /1024 / 1024 / 1024) < 10
    for: 5m
    labels:
      severity: page
    annotations:
      description: "{{$labels.instance}}: Disk Usage is below 10GB (current value is: {{ $value }}" 
  - alert: hostFilesystemUsage
    expr: (node_filesystem_size{device="rootfs"} - node_filesystem_free{device="rootfs"}) / node_filesystem_size{device="rootfs"} * 100 > 80
    for: 5m
    labels:
      severity: page
    annotations:
      description: "{{$labels.instance}}: Filesystem usage is above 80% (current value is: {{ $value }}"
```

检测rules规则的准确性

```
[root@VM_4_5_centos prometheus-2.3.2]# ./promtool check rules rules/hostStatsAlert.rules 
Checking rules/hostStatsAlert.rules
  SUCCESS: 2 rules found
```

### 配置启动脚本start_prometheus.sh

prometheus没有日志记录，对生产上问题排查有很大的影响，因此不建议使用上面的服务形式启动，而是用nohup，可以输出nohup.out日志。

```
vim /opt/prometheus-2.3.2/start_prometheus.sh
#!/bin/bash
nohup /opt/prometheus-2.3.2/prometheus --config.file="/opt/prometheus-2.3.2/prometheus.yml" --storage.tsdb.path="/opt/prometheus-2.3.2/data/prometheus" --storage.tsdb.retention=20h &
```

命令详情

```
./prometheus -h
--config.file="prometheus.yml"  Prometheus configuration file path.
--storage.tsdb.path="data/"     Base path for metrics storage.
--storage.tsdb.retention=15d    How long to retain samples in storage.
```

启动后，会把数据存储在/opt/prometheus-2.3.2/data/prometheus，默认的数据保留时间为15天，此处自定义20h。如果修改了监控的host主机信息，会有延时。

可以访问url：http://IP:9090/graph

```
1、Prometheus/同Graph：可以点击Execute旁边的下拉菜单选择默认查询--> 执行Execute--> Execute下面的Graph为图表显示 --> Console 可以看到具体Value
2、Alerts：显示告警列表，状态active(表示当前告警处于活动状态)|pending(indicated active)|firing 如果有告警，会在此处显示有几个被激活的
3、Status：Runtime Information
```

### 主要信息解释扩展

```
1. pending：警报被激活，但是低于配置的持续时间。这里的持续时间即rule里的FOR字段设置的时间。改状态下不发送报警。
2. firing：警报已被激活，而且超出设置的持续时间。该状态下发送报警。
3. inactive：既不是pending也不是firing的时候状态变为inactive
```

```
1. Runtime Information：
为了避免时区的混乱，prometheus所有的组件内部都强制使用Unix时间，对外展示使用UTC时间。如果想改变时区，可以在UI改为合适的时区时间。
Version：2.3.2
GoVersion：go1.10.3(此处为Prometheus安装的go版本，服务器上需另外安装go，版本可以略高此处安装最新go version go1.11 linux/amd64)
2. Command-Line Flags：
alertmanager.notification-queue-capacity：10000，告警通知队列
log.level：info, One of: [debug, info, warn, error]
storage.tsdb.path：/opt/prometheus-2.3.2/data/prometheus 存储数据
storage.tsdb.retention：15d 存储数据保留时间,这一参数主要取决于本地存储磁盘空间限制,Prometheus 2.x 采用自定义的存储格式将样本数据保存在本地磁盘当中，为了确保此期间如果Prometheus发生崩溃或者重启时能够恢复数据，Prometheus启动时会以写入日志(WAL)的方式来实现重播，从而恢复数据。此期间如果通过API删除时间序列，删除记录也会保存在单独的逻辑文件当中(tombstone)。
web.enable-lifecycle：true 支持reload配置文件，采用 Prometheus 提供的热更新方法实现在不停服务的情况下实现配置文件的重新加载。

热更新加载方法有两种：
1）kill -HUP pid
2）curl -X POST http://IP:9090/-/reload
3. Configuration：即prometheus.yml配置文件，包含一些默认项配置
4. Rules：即*.rules配置文件
5. Targets：查看所有监控url状态
6. Service Discovery：服务自动发现
```

```
[root@VM_4_5_centos prometheus]# tree -a  #数据存储结构
.
|-- 01CP09X49V7Z1HAN5ZXAQ51AMP
|   |-- chunks
|   |   `-- 000001
|   |-- index
|   |-- meta.json
|   `-- tombstones
|-- 01CP15C19XJSMVYEFD51HSXMM2
|   |-- chunks
|   |   `-- 000001
|   |-- index
|   |-- meta.json
|   `-- tombstones
|-- 01CP1C7RHYW071GJB8B1VGWAAG
|   |-- chunks
|   |   `-- 000001
|   |-- index
|   |-- meta.json
|   `-- tombstones
|-- 01CP1K3FYGNSEZJNMP5T78WJSE
|   |-- chunks
|   |   `-- 000001
|   |-- index
|   |-- meta.json
|   `-- tombstones
|-- 01CP1MNJ4VDX3N66EK530RTF03
|   |-- chunks
|   |   `-- 000001
|   |-- index
|   |-- meta.json
|   `-- tombstones
|-- lock
`-- wal
    |-- 000001
    |-- 000004
    |-- 000005
    `-- 000006
```

在一般情况下，Prometheus中存储的每一个样本大概占用1-2字节大小。如果需要对Prometheus Server的本地磁盘空间做容量规划。由于本环境不具备数据备份到远程数据库做持久化，不进行配置，如需数据持久化参考

```
可以通过以下公式计算：
needed_disk_space = retention_time_seconds * ingested_samples_per_second * bytes_per_sample
保留时间(retention_time_seconds)和样本大小(bytes_per_sample)不变的情况下，如果想减少本地磁盘的容量需求，只能通过减少每秒获取样本数(ingested_samples_per_second)的方式。因此有两种手段，一是减少时间序列的数量，二是增加采集样本的时间间隔。考虑到Prometheus会对时间序列进行压缩，因此减少时间序列的数量效果更明显。
经过实践13小时产生240M数据，按照上面的计算公式，经计算：13h*3600sX*19(台)*1.5(1-2个字节)=240M*1000千字节 X=180(ingested_samples_per_second)/s/台

目前数据保留15天，15d*24h*3600s*180(ingested_samples_per_second)*19台*1-2(字节)/1000/1000=4.5G-8.9G 左右
```

## 三、Alertmanager的主要配置

### 官网下载最新源码包，解压后创建一些配置文件

```
[root@VM_4_5_centos alertmanager-0.15.2]# tree -a
.
|-- alertmanager
|-- alertmanager.yml
|-- alertmanager.yml.email
|-- amtool
|-- data
|   |-- nflog
|   `-- silences
|-- LICENSE
|-- nohup.out
|-- NOTICE
|-- start_alertmanager.sh
`-- template
    |-- default.tmpl
```

### 邮件告警配置文件

```
vim /opt/alertmanager-0.15.2/alertmanager.yml
global:
  resolve_timeout: 5m
  smtp_require_tls: false             #smtp不开启tls验证
  smtp_smarthost: IP:587  #公司邮件服务器ip
  smtp_from: zhangxiying@kq300061.com #发件人
  smtp_auth_username: zhangxiying     #不加后缀
  smtp_auth_identity: zhangxiying
  smtp_auth_password: ****

templates: 
- 'template/*.tmpl'                   #主要为邮件告警模板

route:
  group_by: ['alertname']             #仅以告警名进行分组，/opt/prometheus-2.3.2/rules/hostPerformanceAlert.rules文件groups:- name: hostPerformanceAlert
  group_wait: 10s                     #发送一组新的警报的初始等待时间,也就是初次发警报的延时
  group_interval: 10s                 #初始警报组如果已经发送，需要等待多长时间再发送同组新产生的其他报警
  repeat_interval: 1h                 #如果警报已经成功发送，间隔多长时间再重复发送 
  receiver: 'default-receiver'        #接收器名
receivers:
  - name: default-receiver
    email_configs:
      - to: 865747339@qq.com          #告警发给谁

inhibit_rules:                        #抑制规则：如果告警等级：critical，再有warning会先不发告警
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'dev', 'instance','test']
```

新建一个/opt/alertmanager-0.15.2/template，模板采用官方默认default.tmpl

 ![邮件告警](邮件告警.png)

### 钉钉告警配置文件

```
vim /opt/alertmanager-0.15.2/alertmanager.yml
global:
  resolve_timeout: 5m

templates: 
- 'template/*.tmpl'

route:
  group_by: ['alertname', 'instance', 'type']
  group_wait: 30s     #初次发警报的延时
  group_interval: 5m  #初始警报组如果已经发送，需要等待多长时间再发送同组新产生的其他报警
  repeat_interval: 3h #如果警报已经成功发送，间隔多长时间再重复发送
  receiver: 'default-receiver'

receivers:
  - name: default-receiver
    webhook_configs:
      - url: 'http://localhost:8080/webhook'   #需要安装alertmanaer-dingtalk-webhook插件

inhibit_rules:
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'instance', 'mountpoint']
```

### Alertmanaer启动脚本

```
vim /opt/alertmanager-0.15.2/start_alertmanager.sh
#!/bin/bash
nohup /opt/alertmanager-0.15.2/alertmanager --config.file=/opt/alertmanager-0.15.2/alertmanager.yml --storage.path=/opt/alertmanager-0.15.2/data/ --log.level=debug --web.listen-address=0.0.0.0:9093 --data.retention=120h &
```

./alertmanager -h 查看命令参数，这里定义log.level=debug有利于前期通过日志排查问题，告警数据保存时间默认120h，经实践告警数据存储空间很小，因此采用该默认值即可。

查看：http://IP:9093/#/alerts

### 安装alertmanaer-dingtalk-webhook插件

主要有2个插件可以实现，转发到钉钉告警功能，此文采用alertmanaer-dingtalk-webhook，还可以了解https://github.com/timonwong/prometheus-webhook-dingtalk

```
1、参考：http://ylzheng.com/2018/03/01/alertmanager-webhook-dingtalk/
首先安装GO软件，由于Prometheus内置go1.10.3，我们这里服务器上需另外安装go，版本可以略高此处安装最新go version go1.11 linux/amd64)

2、参考：http://www.flysnow.org/2017/01/05/install-golang.html
源码包安装1.10以上的，需要依赖go1.4，有很多报错；
yum安装版本最高到1.9
此处选择二进制包安装https://dl.google.com/go/go1.11.linux-amd64.tar.gz

3、git拉代码到/data
git clone https://github.com/yunlzheng/alertmanaer-dingtalk-webhook.git

4、配置环境变量vim /etc/profile，实际工作空间/root/go/src/github.com/yunlzheng/alertmanaer-dingtalk-webhook/cmd/webhook
export GOROOT=/usr/local/go
export PATH=$PATH:$GOROOT/bin

5、路径一定要注意在go下面的src里，否则go会因找不到各种路径而构建失败
参考：https://github.com/yunlzheng/alertmanaer-dingtalk-webhook
tar -C /usr/local/ -zxvf go1.11.linux-amd64.tar.gz
cd /usr/local/go/src
mkdir github.com/yunlzheng/alertmanaer-dingtalk-webhook -pv
cp -a /data/alertmanaer-dingtalk-webhook/* /usr/local/go/src/github.com/yunlzheng/alertmanaer-dingtalk-webhook
cd /usr/local/go/src/github.com/yunlzheng/alertmanaer-dingtalk-webhook/cmd/webhook
go build  #很快就构建成功后，出现webhook命令

6、启动webhook服务，默认使用8080端口
/usr/local/go/src/github.com/yunlzheng/alertmanaer-dingtalk-webhook/vendor/github.com/gin-gonic/gin/utils.go #修改端口不生效
vim start_webhook.sh
#!/bin/bash
nohup go run /usr/local/go/src/github.com/yunlzheng/alertmanaer-dingtalk-webhook/cmd/webhook/webhook.go -defaultRobot=https://oapi.dingtalk.com/robot/send?access_token=47c8f721f4fc362bf751112e40b12894aec3c6b6027d7fe1c320587976694def &
```

## 四、Grafana主要配置

### yum安装即可，主要配置文件

```
grep -vP '^$|^#|^;' /etc/grafana/grafana.ini
http_port = 3001                 #3000服务器已经占用
root_url = http://IP:3001
```

数据配置及存储：/var/lib/grafana/grafana.db 

启动服务：systemctl start grafana-server.service 

### 登陆url，按照Home提示顺序创建数据源

```
Name：prometheus
Type：Prometheus
URL：http://localhost:9090 
其他选项默认即可
```

### 创建仪表盘Dashboard

```
点击左边的＋号--> Import--> 1860--> 数据源选择prometheus
```

模板就会显示了，点击Home下拉列表即可找到模板样式。

注意，这个过程我还遇到一个坑，就是URL路径末尾不能加/，即不能写成`http://IP:9090/`，否则还是无法加载数据，通过追踪它的网络请求可以发现已经被301重定向了，并且会抛出`blocked by CORS policy`错误。

## 五、客户端安装node_export

```
1、安装方式
cd /etc/ansible/playbooks
ansible-playbook install_node_exporter.yml --extra-vars "{'host':jx}  #/etc/ansible/hosts内配置

2、ansible-playbook配置
cat /etc/ansible/playbooks/install_node_exporter.yml 
---
- hosts: "{{ host }}"
  remote_user: root
  roles:
    - node_exporter

3、playbook目录树
[root@VM_4_5_centos roles]# tree /etc/ansible/roles/node_exporter/
node_exporter/
|-- defaults
|   `-- main.yml
|-- files
|   `-- node_exporter-0.16.0.linux-amd64.tar.gz
|-- handlers
|   `-- main.yml
|-- meta
|-- tasks
|   |-- main.yml
|-- templates
|   |-- node_exporter.conf.j2
|   `-- node_exporter.service.j2
`-- vars
    
4、默认路径，注意path的编写格式，经测试nodeexporter中间不允许有中杠或底杠
[root@VM_4_5_centos roles]# cat node_exporter/defaults/main.yml 
---
nodeexporter_path: /etc/ansible/roles/node_exporter
pro_path: /opt/node_exporter-0.16.0
sys_path: /usr/lib/systemd/system

5、node_exporter配置文件模板
[root@VM_4_5_centos roles]# cat node_exporter/templates/node_exporter.conf.j2    
OPTIONS="--collector.textfile.directory /opt/node_exporter-0.16.0/data/textfile_collector"

6、node_exporter配置成服务
[root@VM_4_5_centos roles]# cat node_exporter/templates/node_exporter.service.j2 
[Unit]
Description=Node Exporter

[Service]
User=root
EnvironmentFile=/opt/node_exporter-0.16.0/node_exporter.conf
ExecStart=/opt/node_exporter-0.16.0/node_exporter $OPTIONS

[Install]
WantedBy=multi-user.target

7、安装node_exporter
- name: tar remote package
  unarchive:
    src: "{{ nodeexporter_path }}/files/node_exporter-0.16.0.linux-amd64.tar.gz"
    dest: /opt
    copy: yes
    mode: 0755

- name: rename node_exporter
  shell: mv /opt/node_exporter-0.16.0.linux-amd64 "{{ pro_path }}"

- name: make data dir
  shell: mkdir -pv "{{ pro_path }}/data/textfile_collector"

- name: configure node-exporter
  copy:
    src: "{{ nodeexporter_path }}/templates/node_exporter.conf.j2"
    dest: "{{ pro_path }}"
    mode: 0755

- name: configure start service
  copy:
    src: "{{ nodeexporter_path }}/templates/node_exporter.service.j2"
    dest: "{{ sys_path }}"
    mode: 0755

- name: start service
  service:
    name: node-exporter.service
    state: started
    enabled: yes
```



## 参考文档

1、go安装http://www.flysnow.org/2017/01/05/install-golang.html
2、dingtalk插件的安装：https://github.com/yunlzheng/alertmanaer-dingtalk-webhook
3、插件的配置：http://ylzheng.com/2018/03/01/alertmanager-webhook-dingtalk/
4、Q&A：https://cdn2.jianshu.io/p/3cecf738b172
5、本地化数据存储：http://ylzheng.com/2018/03/06/promethus-local-storage/
6、远端存储：http://ylzheng.com/2018/03/07/promethues-remote-storage/
7、prometheus文档；https://yunlzheng.gitbook.io/prometheus-book/part-iii-prometheus-shi-zhan/references
https://www.bookstack.cn/read/prometheus_practice/README.md

https://yunlzheng.gitbook.io/prometheus-book/part-iii-prometheus-shi-zhan/references
8、热更新配置文件：https://www.bookstack.cn/read/prometheus_practice/qa-hotreload.md
9、promethus入门：https://www.hi-linux.com/posts/25047.html
10、alertmanager安装版本：https://github.com/prometheus/alertmanager/releases/download/v0.15.2/alertmanager-0.15.2.linux-amd64.tar.gz
11、安装配置：https://www.cnblogs.com/iiiiher/p/8277040.html



