#!/bin/bash
jobConfigPath=/var/lib/jenkins/jobs
echo "备份jobs/*/config.xml"
tar --exclude="/var/lib/jenkins/jobs/*/builds" -zcvf /opt/`date +%Y-%m-%d-%H_%M_%S`jenkins_job.tar.gz /var/lib/jenkins/jobs/

configName=config.xml
for jobName in `ls $jobConfigPath`
do cd $jobConfigPath/$jobName
if grep "<doGenerateSubmoduleConfigurations>" $configName &> /dev/null;then
        echo "$jobName is freestyle jenkins job"
        sed -i 's/git@gitlabIP/git@gitlab.kq300061.com/' $configName
        sed -i '/doGenerateSubmoduleConfigurations/a\    <gitTool>git2.14.1</gitTool>' $configName
else
        echo "$jobName is pipeline jenkins job"
        sed -i 's/git@gitlabIP/git@gitlab.kq300061.com/g' $configName
        sed -i '/拉取gitlab代码/a\tools { git &apos;git2.14.1&apos; }' $configName
fi
done
echo "需要重启jenkinsIP"
