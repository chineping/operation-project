#!/bin/bash
jobConfigPath=/data/jenkins_home/jenkins/jobs

configName=config.xml
for jobName in `ls $jobConfigPath`
do cd $jobConfigPath/$jobName
if grep "<doGenerateSubmoduleConfigurations>" $configName &> /dev/null;then
        echo "$jobName is freestyle jenkins job"
else
        echo "$jobName is pipeline jenkins job"
fi
done
echo "需要重启jenkinsIP"
