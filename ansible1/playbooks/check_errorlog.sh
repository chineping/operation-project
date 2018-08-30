#!/bin/bash
Logdir=`sed -n '23p' /etc/filebeat/filebeat.yml|awk -F' ' '{print $2}'`
tail -n200 $Logdir |grep ERROR
tail -n200 $Logdir |grep exception
uptime | awk '{print $NF}'
df -h
free -m


