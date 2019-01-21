#!/bin/bash
jobConfigPath=/var/lib/jenkins/jobs
jobName=2
configName=config.xml
cd $jobConfigPath/$jobName
if grep "<doGenerateSubmoduleConfigurations>" $configName &> /dev/null;then
	echo "$jobName is freestyle jenkins job"
	sed -i 's/git@gitlabIP/git@gitlab.kq300061.com/' $configName
	sed -i '/doGenerateSubmoduleConfigurations/a\    <gitTool>git2.14.1</gitTool>' $configName
	curl -u devops:devops123 -X POST http://jenkinsIP:8080/view/jtbank_dev_bj/job/1/reload
else
cd $jobConfigPath/$jobName
	echo "$jobName is pipeline jenkins job"
        sed -i 's/git@gitlabIP/git@gitlab.kq300061.com/g' $configName
        sed -i '/拉取gitlab代码/a\tools { git &apos;git2.14.1&apos; }' $configName
	curl -u devops:devops123 -X POST http://jenkinsIP:8080/view/jtbank_dev_bj/job/2/reload
fi
