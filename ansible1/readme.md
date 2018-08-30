# 运维常用工具Ansible-playbook编写

## 一、安装及主要配置

1、yum安装ansible即可，根本不用费劲去研究源码包安装哦，没有什么技术含量

2、修改主要配置文件

```
[root@VM_4_5_centos ansible]# grep -vP '^$|^#' ansible.cfg 
[defaults]
host_key_checking = False
command_warnings = False
[inventory]
[privilege_escalation]
[paramiko_connection]
[ssh_connection]
[persistent_connection]
[accelerate]
[selinux]
[colors]
[diff]
```

3、在hosts内加入自己的主机或者主机组

4、新建一个playbooks路径用于存储

```
---
- hosts: "{{ host }}"
  remote_user: root
  roles:
    - filebeat
```

5、在roles内创建filebeat路径,目录结构如下

```
[root@VM_4_5_centos roles]# tree -a filebeat/
filebeat/
|-- defaults
|   `-- main.yml
|-- files
|   `-- filebeat-5.6.9-x86_64.rpm
|-- handlers
|   `-- main.yml
|-- meta
|-- tasks
|   |-- main.yml
|   |-- rpm_install.yml
|   `-- yum_install.yml
|-- templates
|   |-- filebeat.repo.j2
|   |-- filebeat.yml.j2
|   `-- filebeat.yml.j2_bak
|       |-- filebeat.yml.j2
|       `-- filebeat.yml.j2.bak
`-- vars
```

## 二、操作及使用方法

使用命令

```
cd /etc/ansible/playbooks
ansible-playbook install_node_exporter.yml --extra-vars "{'host':filebeat}" -vv
```

具体处理流程大致：ansible-playbook通过额外输入的主机host参数，去指定roles内主要执行tasks下面的yml脚本。

```
 defaults：可以指定一些自定义变量
 files：源码包或rpm包
 handlers：主要定义服务的启停
 tasks：最主要的操作脚本
 templates：一些模板的配置文件，这里一定要写成.j2命名，可以根据ansible的setup模块，自动替换ip等， 例如安装配置redis时bind ip：bind {{ ansible_all_ipv4_addresses | list | join ("\",\"")  }}
```



常用模板参考文档：http://www.yfshare.vip/2017/04/05/Ansible%E5%B8%B8%E7%94%A8%E6%A8%A1%E5%9D%97/