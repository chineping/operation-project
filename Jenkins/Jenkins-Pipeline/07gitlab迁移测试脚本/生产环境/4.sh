#!/bin/bash
for i in 1 hhbank_pom_pro jxbank_admin_h5 jxbank_admin_h5_npm jxbank_plus_admin jxbank_plus_auth jxbank_plus_datainner jxbank_plus_eureka jxbank_plus_frontmanager jxbank_plus_gateway jxbank_plus_product jxbank_plus_schedule jxbank_pom_pro jxbank_product_h5 jxbank_product_h5_npm sjbank_plus_product sjbank_product_h5 sjbank_product_h5_npm trainingcamp_prod_666H5_npm trainingcamp_prod_externalservice trainingcamp_prod_frontservice trainingcamp_prod_h5 trainingcamp_prod_h5_npm xkbank_pom_pro
do
#echo $i
cd /data/jenkins_home/jenkins/jobs/$i
#pwd
#sed -i '/doGenerateSubmoduleConfigurations/a\    <gitTool>git1.8.3</gitTool>' config.xml
grep "<gitTool>git1.8.3</gitTool>" config.xml
done
