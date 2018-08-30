#!/bin/bash
app_path=/opt/filebeat-6.2.4-linux-x86_64
nohup $app_path/filebeat -e -c $app_path/filebeat.yml > $app_path/nohup.out 2>&1 &
