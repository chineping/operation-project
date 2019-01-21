#!/bin/bash
jobConfigPath=/data/jenkins_home/jenkins/jobs
jobName=2
configName=config.xml
cd $jobConfigPath/$jobName
if grep "<doGenerateSubmoduleConfigurations>" $configName &> /dev/null;then
        echo "$jobName is freestyle jenkins job"
        sed -i 's/git@gitlabOldIP/git@gitlabNewIP/g' $configName
        sed -i 's#<gitTool>Default</gitTool>#<gitTool>git1.8.3</gitTool>#g' $configName
        curl -u jenkinsUser:jenkinsPassword -X POST http://jenkinsIP:8081/view/hhbank_prod/job/1/reload
else
cd $jobConfigPath/$jobName
        echo "$jobName is pipeline jenkins job"
        sed -i 's/git@gitlabOldIP/git@gitlabNewIP/g' $configName
        sed -i '/拉取gitlab代码/a\tools { git &apos;git1.8.3&apos; }' $configName
        curl -u jenkinsUser:jenkinsPassword -X POST http://jenkinsIP:8081/view/hhbank_prod/job/2/reload
fi
