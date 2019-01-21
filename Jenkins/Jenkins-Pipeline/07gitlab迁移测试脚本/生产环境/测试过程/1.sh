#!/bin/bash
jobConfigPath=/data/jenkins_home/jenkins/jobs
echo "备份jobs/*/config.xml"
tar --exclude="/data/jenkins_home/jenkins/jobs/*/builds" -zcvf /opt/`date +%Y-%m-%d-%H_%M_%S`jenkins_job.tar.gz /data/jenkins_home/jenkins/jobs/

configName=config.xml
for jobName in `ls $jobConfigPath`
do cd $jobConfigPath/$jobName
if grep "<doGenerateSubmoduleConfigurations>" $configName &> /dev/null;then
        echo "$jobName is freestyle jenkins job"
        sed -i 's/git@gitlabIP/git@gitlabNewIP/g' $configName
        sed -i 's#<gitTool>Default</gitTool>#<gitTool>git1.8.3</gitTool>#g' $configName
else
        echo "$jobName is pipeline jenkins job"
        sed -i 's/git@gitlabIP/git@gitlabNewIP/g' $configName
        sed -i '/拉取gitlab代码/a\tools { git &apos;git1.8.3&apos; }' $configName
fi
done
echo "需要重启jenkinsIP"
