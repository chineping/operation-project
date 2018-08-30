#!/bin/bash
nohup /opt/alertmanager-0.15.2/alertmanager --config.file=/opt/alertmanager-0.15.2/alertmanager.yml --storage.path=/opt/alertmanager-0.15.2/data/ --log.level=debug --web.listen-address=0.0.0.0:9093 --data.retention=120h &
