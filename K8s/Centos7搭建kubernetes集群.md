# Centos7搭建kubernetes集群及主要配置

本文介绍在两台Centos7上安装kubernetes集群，一台作为master，另一台作为node。并介绍安装过程中遇到的问题，和跑简单nginx服务。

> 注：如果用于生产环境，应该搭建高可用集群。

Kubernetes包提供了一些服务：kube-apiserver，kube-scheduler，kube-controller-manager，kubelet，kube-proxy。 这些服务由systemd管理，配置位于：`/etc/kubernetes`。

Kubernetes master 将会跑这些服务：kube-apiserver, kube-controller-manager ，kube-scheduler和etcd。 kubernates工作节点跑的服务有：kubelet, proxy, cadvisor and docker。 所有节点都会起flanneld实现跨主机网络。

## 一、 安装前准备

现有两台机器：

```
操作系统：Centos7.4 x86_64
10.90.2.14 master
10.90.2.6  node
```

## 二、安装配置master

不做详细介绍，直接使用脚本安装

```
sh -x k8s-master.sh 10.90.2.14
```

k8s-master.sh

```
#!/usr/bin/env bash
set -e

MASTER_IP=$1
if [ ! $MASTER_IP ]
then
	echo "MASTER_IP is null"
	exit 1
fi

echo "=================install ntpd==================="
yum -y install ntp
systemctl start ntpd
systemctl enable ntpd

echo "=================install docker, k8s, etcd, flannel==================="
cat <<EOF > /etc/yum.repos.d/virt7-docker-common-release.repo
[virt7-docker-common-release]
name=virt7-docker-common-release
baseurl=http://cbs.centos.org/repos/virt7-docker-common-release/x86_64/os/
gpgcheck=0
EOF

yum -y install --enablerepo=virt7-docker-common-release kubernetes etcd flannel

echo "=================config kubernetes==================="
mv /etc/kubernetes/config /etc/kubernetes/config.bak
cat <<EOF >/etc/kubernetes/config
# logging to stderr means we get it in the systemd journal
KUBE_LOGTOSTDERR="--logtostderr=true"

# journal message level, 0 is debug
KUBE_LOG_LEVEL="--v=0"

# Should this cluster be allowed to run privileged docker containers
KUBE_ALLOW_PRIV="--allow-privileged=false"

# How the replication controller and scheduler find the kube-apiserver
KUBE_MASTER="--master=http://${MASTER_IP}:8080"
EOF

setenforce 0
#systemctl disable iptables-services firewalld
#systemctl stop iptables-services firewalld

echo "================= config etcd======================"
sed -i s#'ETCD_LISTEN_CLIENT_URLS="http://localhost:2379"'#'ETCD_LISTEN_CLIENT_URLS="http://0.0.0.0:2379"'#g /etc/etcd/etcd.conf
sed -i s#'ETCD_ADVERTISE_CLIENT_URLS="http://localhost:2379"'#'ETCD_ADVERTISE_CLIENT_URLS="http://0.0.0.0:2379"'#g /etc/etcd/etcd.conf 

echo "================= config apiserver==================="
mv /etc/kubernetes/apiserver /etc/kubernetes/apiserver.bak 
cat <<EOF >/etc/kubernetes/apiserver
# The address on the local server to listen to.
KUBE_API_ADDRESS="--address=0.0.0.0"

# The port on the local server to listen on.
KUBE_API_PORT="--port=8080"

# Port kubelets listen on
KUBELET_PORT="--kubelet-port=10250"

# Comma separated list of nodes in the etcd cluster
KUBE_ETCD_SERVERS="--etcd-servers=http://${MASTER_IP}:2379"

# Address range to use for services
KUBE_SERVICE_ADDRESSES="--service-cluster-ip-range=10.254.0.0/16"

# Add your own!
KUBE_API_ARGS=""
EOF

echo "=================start and set etcd==============="
systemctl start etcd
etcdctl mkdir /kube-centos/network
etcdctl mk /kube-centos/network/config "{ \"Network\": \"172.30.0.0/16\", \"SubnetLen\": 24, \"Backend\": { \"Type\": \"vxlan\" } }"

echo "=================config flannel==================="
mv /etc/sysconfig/flanneld /etc/sysconfig/flanneld.bak
cat <<EOF >/etc/sysconfig/flanneld
# Flanneld configuration options

# etcd url location.  Point this to the server where etcd runs
FLANNEL_ETCD_ENDPOINTS="http://${MASTER_IP}:2379"

# etcd config key.  This is the configuration key that flannel queries
# For address range assignment
FLANNEL_ETCD_PREFIX="/kube-centos/network"

# Any additional options that you want to pass
#FLANNEL_OPTIONS=""
EOF

echo "=================start etcd k8s ==================="
for SERVICES in etcd kube-apiserver kube-controller-manager kube-scheduler flanneld ; do
	systemctl restart $SERVICES
	systemctl enable $SERVICES
	systemctl status $SERVICES
done
```

> 注： 上面脚本并没有启动docker和kublet，如果测试时需要在master上运行服务，请启动docker，并按照node的kublet配置并启动kublet。

## 三、安装配置nodes

执行脚本

```
sh install-k8s-node.sh 10.90.2.14 10.90.2.6 # master_ip node_ip
```

install-k8s-node.sh 脚本内容

```
#/usr/bin/env bash
set -e

MASTER_IP=$1
NODE_IP=$2
if [ ! $MASTER_IP ] || [ ! $NODE_IP ]
then
	echo "MASTER_IP or NODE_IP is null"
	exit 1
fi

echo '=================install ntpd==================='
yum -y install ntp
systemctl start ntpd
systemctl enable ntpd

echo "=================install docker, k8s, etcd, flannel==================="
cat <<EOF > /etc/yum.repos.d/virt7-docker-common-release.repo
[virt7-docker-common-release]
name=virt7-docker-common-release
baseurl=http://cbs.centos.org/repos/virt7-docker-common-release/x86_64/os/
gpgcheck=0
EOF

yum -y install --enablerepo=virt7-docker-common-release kubernetes etcd flannel

setenforce 0

echo "===============config kubernetes================"
mv /etc/kubernetes/config /etc/kubernetes/config.bak
cat <<EOF >/etc/kubernetes/config
# logging to stderr means we get it in the systemd journal
KUBE_LOGTOSTDERR="--logtostderr=true"

# journal message level, 0 is debug
KUBE_LOG_LEVEL="--v=0"

# Should this cluster be allowed to run privileged docker containers
KUBE_ALLOW_PRIV="--allow-privileged=false"

# How the replication controller and scheduler find the kube-apiserver
KUBE_MASTER="--master=http://${MASTER_IP}:8080"
EOF

echo "===============config kublet================"
mv /etc/kubernetes/kubelet  /etc/kubernetes/kubelet.bak
cat <<EOF >/etc/kubernetes/kubelet
# The address for the info server to serve on
KUBELET_ADDRESS="--address=0.0.0.0"

# The port for the info server to serve on
KUBELET_PORT="--port=10250"

# You may leave this blank to use the actual hostname
# Check the node number!
KUBELET_HOSTNAME="--hostname-override=${NODE_IP}"

# Location of the api-server
KUBELET_API_SERVER="--api-servers=http://${MASTER_IP}:8080"

# Add your own!
KUBELET_ARGS=""
EOF

echo "===============config flanneld================"
mv /etc/sysconfig/flanneld /etc/sysconfig/flanneld.bak
cat <<EOF >/etc/sysconfig/flanneld
# Flanneld configuration options

# etcd url location.  Point this to the server where etcd runs
FLANNEL_ETCD_ENDPOINTS="http://${MASTER_IP}:2379"

# etcd config key.  This is the configuration key that flannel queries
# For address range assignment
FLANNEL_ETCD_PREFIX="/kube-centos/network"

# Any additional options that you want to pass
#FLANNEL_OPTIONS=""
EOF

echo "==========start kube-proxy kubelet flanneld docker==========="
for SERVICES in kube-proxy kubelet flanneld docker; do
    systemctl restart $SERVICES
    systemctl enable $SERVICES
    systemctl status $SERVICES
done

echo "==============set kubectl================"
kubectl config set-cluster default-cluster --server=http://${MASTER_IP}:8080
kubectl config set-context default-context --cluster=default-cluster --user=default-admin
kubectl config use-context default-context
```

至此，集群就算搭建完成，查看节点状态。

```
[root@VM_2_14_centos ~]# kubectl get node
NAME        STATUS    AGE
10.90.2.6   Ready     3h
127.0.0.1   Ready     33m
```

## 四、测试服务

测试通过master部署两个nginx到node，在master上新建文件nginx-deployment.yml。

```
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: nginx-deployment
spec:
  replicas: 1
  selector:
    name: nginx 
  template: 
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: hub.c.163.com/library/nginx
        #image: docker.io/nginx:latest
        ports:
        - containerPort: 80
```

创建deployment

```
$ kubectl create -f nginx-deployment.yml
deployment "nginx-deployment" created
```

查看pod：

```
$ kubectl get pods -o wide
NAME                                READY     STATUS    RESTARTS   AGE       IP            NODE
nginx-deployment-4087004473-kbbgs   1/1       Running   0          1h        172.30.41.2   172.31.25.80
nginx-deployment-4087004473-m47bg   1/1       Running   0          1h        172.30.93.2   172.31.16.52

# 访问nginx
$curl 172.30.41
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
    body {
        width: 35em;
        margin: 0 auto;
        font-family: Tahoma, Verdana, Arial, sans-serif;
    }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>
```

## 五、常见问题

1、如果发现`STATUS`一直处于`ContainerCreating`状态，可能是正在拉取镜像。可以查看详细信息，看到缺少gcr.io/google_containers/*pause*-*amd64*:*3.0*而报错

```
$  kubectl describe pod <pod-name> #pod-name 即nginx-deployment-4087004473-
docker pull registry.aliyuncs.com/archon/pause-amd64:3.0
docker tag 99e59f495ffa gcr.io/google_containers/pause-amd64:3.0
```

2、出现以下报错yum install  rhsm

```
FailedSync      Error syncing pod, skipping: failed to "StartContainer" for "POD" with ErrImagePull: "image pull failed for registry.access.redhat.com/rhel7/pod-infrastructure:latest, this may be because there are no credentials on this request.  details: (open /etc/docker/certs.d/registry.access.redhat.com/redhat-ca.crt: no such file or directory)"
```

解决方案：

```
参考：http://blog.51cto.com/12482328/2120035

查看/etc/docker/certs.d/registry.access.redhat.com/redhat-ca.crt （该链接就是上图中的说明） 是一个软链接，但是链接过去后并没有真实的/etc/rhsm，所以需要使用yum安装：

yum install *rhsm*

安装完成后，执行一下docker pull registry.access.redhat.com/rhel7/pod-infrastructure:latest
如果依然报错，可参考下面的方案：

wget http://mirror.centos.org/centos/7/os/x86_64/Packages/python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm
rpm2cpio python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm | cpio -iv --to-stdout ./etc/rhsm/ca/redhat-uep.pem | tee /etc/rhsm/ca/redhat-uep.pem

这两个命令会生成/etc/rhsm/ca/redhat-uep.pem文件.
顺得的话会得到下面的结果。
docker pull registry.access.redhat.com/rhel7/pod-infrastructure:latest
```

3、确认docker镜像

```
docker images     #查看本地docker镜像，为nginx-deployment.yml中定义的镜像使用
docker pull nginx #默认拉取位置docker.io
```

阿里云和道云镜像仓库：
https://dev.aliyun.com/search.html?spm=5176.1972343.0.1.53fb5aaaxbSFl1
https://hub.daocloud.io/

4、修改2个参数

--selinux-enabled=false 			#减少selinux报错

--insecure-registry 10.90.2.14:5000  #自建仓库时使用http协议

[root@VM_2_14_centos ~]# grep -vP '^$|^#' /etc/sysconfig/docker

```
OPTIONS='--selinux-enabled=false --log-driver=journald --signature-verification=false --insecure-registry 10.90.2.14:5000'
```

或者：

```
/etc/docker/daemon.json这个文件中
添加insecure-registries，ip地址自己更改：
{
"insecure-registries":["10.90.2.14:5000"]
}
```

5、注释redhat仓库，减少证书验证报错

[root@VM_2_14_centos ~]# grep -vP '^$|^#' /etc/kubernetes/kubelet

```
KUBELET_ADDRESS="--address=0.0.0.0"
KUBELET_PORT="--port=10250"
KUBELET_HOSTNAME="--hostname-override=127.0.0.1"
KUBELET_API_SERVER="--api-servers=http://10.90.2.14:8080"
#KUBELET_POD_INFRA_CONTAINER="--pod-infra-container-image=registry.access.redhat.com/rhel7/pod-infrastructure:latest"
KUBELET_ARGS="--cluster-dns=10.254.10.2 --cluster-domain=sky --allow-privileged=true"
```

## 六、flannel网段变更

flannel网络主要受etcd服务影响

```
[root@VM_2_14_centos sysconfig]# ip a
3: flannel.1: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1450 qdisc noqueue state UNKNOWN 
    link/ether 72:ff:40:49:5d:5e brd ff:ff:ff:ff:ff:ff
    inet 192.168.72.0/32 scope global flannel.1
       valid_lft forever preferred_lft forever
4: docker0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1450 qdisc noqueue state UP 
    link/ether 02:42:c3:5b:55:8b brd ff:ff:ff:ff:ff:ff
    inet 192.168.72.1/24 scope global docker0
       valid_lft forever preferred_lft forever
```

变更方式

```
1、查看当前etcd网络配置
[root@VM214_centos etcd]# etcdctl get /kube-centos/network/config
{ "Network": "192.168.0.0/16", "SubnetLen": 24, "Backend": { "Type": "vxlan" } }
2、更新etcd网络配置
etcdctl update /kube-centos/network/config "{ \"Network\": \"172.30.0.0/16\", \"SubnetLen\": 24, \"Backend\": { \"Type\": \"vxlan\" } }"
3、查看子网
[root@VM_2_14_centos sysconfig]# etcdctl ls /kube-centos/network/subnets 
/kube-centos/network/subnets/192.168.72.0-24
/kube-centos/network/subnets/192.168.81.0-24
4、重启服务
systemctl restart etcd.service flanneld.service
5、删除废弃flannel和docker的ip
ip link delete docker0
ip link delete flannel.1
kubectl delete node 10.90.2.14
```

如果在主机上进行了多次k8s的配置，则需要对网卡进行清理。未启动flanneld和docker服务的情形下，通过 ifconfig 查看网卡，如果存在docker0、flannel.0或flannel.1

kubernet集群网络主要受apiserver服务配置影响

参数：KUBE_SERVICE_ADDRESSES="--service-cluster-ip-range=10.254.0.0/16"

[root@VM_2_14_centos sysconfig]# grep -vP '^$|^#' /etc/kubernetes/apiserver

```
KUBE_API_ADDRESS="--insecure-bind-address=0.0.0.0"
KUBE_API_PORT="--port=8080"
KUBELET_PORT="--kubelet-port=10250"
KUBE_ETCD_SERVERS="--etcd-servers=http://10.90.2.14:2379"
KUBE_SERVICE_ADDRESSES="--service-cluster-ip-range=10.254.0.0/16"
KUBE_API_ARGS=""
```

## 七、建立私有仓库registry

主要参考文档

https://blog.csdn.net/u010397369/article/details/42422243

http://www.cnblogs.com/davicelee/articles/4045687.html

如需证书验证：

https://www.jianshu.com/p/4317a56a3fae

```
docker pull registry
docker run -d --name registry -p 5000:5000 --restart=always -v /opt/data/registry:/tmp/registry registry
参数：--restart=always 重启docker自动启动该镜像
```

测试上传镜像

```
docker pull busybox
docker tag busybox 10.90.2.14:5000/busybox
docker images
docker push 10.90.2.14:5000/busybox
```

## 八、构建自有镜像

参考文档：

https://yeasy.gitbooks.io/docker_practice/image/build.html

```
$ mkdir mynginx
$ cd mynginx
$ touch Dockerfile
```

其内容为

```
FROM nginx
RUN echo '<h1>Hello, Docker!</h1>' > /usr/share/nginx/html/index.html
```

构建镜像并推到自建仓库

```
docker build /root/mynginx/ -t mynginx
docker tag mynginx 10.90.2.14:5000/mynginx
docker push 10.90.2.14:5000/mynginx
docker images
```

创建mynginx-rc.yaml，其内容为

```
apiVersion: v1
kind: ReplicationController
metadata:
  name: mynginx-rc
spec:
  replicas: 1
  template: 
    metadata:
      labels:
        app: mynginx
    spec:
      containers:
      - name: mynginx
        image: 10.90.2.14:5000/mynginx:latest
        ports:
        - containerPort: 80
```

创建并查看rc、pod

```
[root@VM214_centos ~]# kubectl create -f mynginx-rc.yaml
replicationcontroller "mynginx-rc" created
[root@VM214_centos ~]# kubectl get rc -owide
NAME         DESIRED   CURRENT   READY     AGE       CONTAINER(S)   IMAGE(S)                         SELECTOR
mynginx-rc   1         1         0         23s       mynginx        10.90.2.14:5000/mynginx:latest   app=mynginx
[root@VM_2_14_centos ~]# kubectl get pod -owide
NAME               READY     STATUS    RESTARTS   AGE       IP             NODE
mynginx-rc-8hszx   1/1       Running   0          1m        192.168.72.5   k8s-node-1
```

创建mynginx-svc.yaml，其内容为

```
apiVersion: v1
kind: Service
metadata:
  name: mynginx
spec:
  type: NodePort
  ports:
   - port: 80
     nodePort: 32765
  selector:
    app: mynginx
```

创建并查看svc

```
[root@VM_2_14_centos ~]# kubectl create -f mynginx-svc.yaml 
service "mynginx" created
[root@VM_2_14_centos ~]# kubectl get svc -owide
NAME         CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE       SELECTOR
kubernetes   10.254.0.1       <none>        443/TCP        3d        <none>
mynginx      10.254.141.242   <nodes>       80:32765/TCP   36s       app=mynginx
```

kubectl port-forward nginx 32765:80

访问http://10.90.2.14:32765 端口

## 九、两个副本挂载在本地磁盘（nfs方式）

主要参考文档

https://blog.csdn.net/cuipengchong/article/details/71650011

https://blog.frognew.com/2017/01/kubernetes-volumes-emptydir-and-hostpath.html

创建nginx_pod_volume_nfs.yaml

```
apiVersion: v1 
kind: ReplicationController 
metadata: 
  name: nginx 
spec: 
  replicas: 2 
  selector: 
    app: web01 
  template: 
    metadata: 
      name: nginx 
      labels: 
        app: web01 
    spec: 
      containers: 
      - name: nginx 
        image: 10.90.2.14:5000/nginx
        ports: 
        - containerPort: 80 
        volumeMounts: 
        - mountPath: /usr/share/nginx/html 
          readOnly: false 
          name: nginx-data 
      volumes: 
      - name: nginx-data 
        nfs: 
          server: 10.90.2.14
          path: "/data/www-data"
```



以下为hostpath方式（仅提供本地挂载）

```
[root@VM_2_14_centos ~]# cat dir-nginx.yml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: nginx
spec:
  replicas: 2
  template:
    metadata:
      labels:
        run: nginx
    spec:
      containers:
      - name: nginx
        image: docker.io/nginx:latest
        ports:
        - containerPort: 80
        volumeMounts:
        - mountPath: /etc/nginx
          name: nginx-config
      volumes:
      - name: nginx-config
        hostPath:
          path: /mnt/data
```

