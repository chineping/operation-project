pipeline {
    //在任何可用的代理上执行Pipeline
    agent any
    //参数化变量，目前只支持[booleanParam, choice, credentials, file, text, password, run, string]这几种参数类型，其他高级参数化类型还需等待社区支持。
    parameters {
    //git代码路径【参数值对外隐藏】
    string(name:'repoUrl', defaultValue: 'ssh://****@git.wowocai.com:29418/spring/java', description: 'git代码路径')
    //repoBranch参数后续替换成git parameter不再依赖手工输入,JENKINS-46451【git parameters目前还不支持pipeline】
    string(name:'repoBranch', defaultValue: 'master', description: 'git分支名称')
    //编译目录的相对路径
    string(name:'buildPath', defaultValue: 'spring3-websites/', description: '编译目录')
    //war包的名称
    string(name:'warName', defaultValue: 'spring3-websites', description: 'war包的名称')
    //部署代码所在IP
    string(name:'serverIP', defaultValue: '192.168.10.113', description: '部署代码所在ip地址')
//目标服务
    string(name:'proName', defaultValue: 'spring_vip_mobile', description: '目标服务')
//需要清理的文件
    //string(name: 'cleanPath',defaultValue:'/home/spring_vip_mobile/work/*,\n/home/spring_vip_mobile/temp/*', description: '需要清理的文件')
string(name: 'cleanPath',defaultValue:'/home/spring_vip/webapps/spring3-websites,\n/home/spring_vip/webapps/spring3-websites.war', description: '需要清理的文件')
    //目标服务的绝对路径
    string(name:'proPath', defaultValue: '/home/spring_vip/webapps/', description: '目标服务的绝对路径')
//重启脚本路径(备份文件地址、备份文件名称、执行目录)
string(name:'shellpath', defaultValue: '/home/spring_vip/shback/,\nspring3-member.sh,\n/home/spring_vip/webapps/spring3-websites/spring3-member/build/libs/', description: '备份文件地址、备份文件名称、执行目录')
//邮件接收人
string(name:'email', defaultValue: 'alina.zou,\nsable.song,\napril.he', description: '邮件接收人')
    }
    //常量参数，初始确定后一般不需更改
    environment{
        //git服务全系统只读账号cred_id【参数值对外隐藏】
        CRED_ID='********-***********-**********-**********'
    }
    options {
        //保持构建的最大个数
        buildDiscarder(logRotator(numToKeepStr: '10')) 
    }
    
    //pipeline的各个阶段场景
    stages {
        stage('清理工作空间') { 
            steps {
                cleanWs()
                  }
        }
        
        stage('代码获取') {
            steps {
            //根据param.server分割获取参数,包括IP,jettyPort,username,password
            script {
                def split=params.cleanPath.split(",")
                cleanPathOne=split[0]
cleanPathTwo=split[1]
            }
//获取备份文件地址、备份文件名称、执行目录
script{
def split=params.shellpath.split(",")
                shbackPath=split[0]
shbackName=split[1]
shfile=split[2]
}
//获取email接收人
script{
def split=params.email.split(",")
                emailOne=split[0]
emailTwo=split[1]
emailThree=split[2]
}
              // Get some code from a GitHub repository
              git credentialsId:CRED_ID, url:params.repoUrl, branch:params.repoBranch
            }
        }
        
        stage('编译') {
            steps {
              //根据编译路径打包
              echo "starting build in ${workspace}/${params.buildPath} ......"
              // Get some code from a GitHub repository
              sh "cd ${params.buildPath} && gradle clean build"
  sh "jar -cvfM ${params.warName}.war ${params.warName}"
            }
        }


stage('预处理目标服务器服务环境'){
steps{
//停止目标tomcat服务
sh "ssh -f -n root@${params.serverIP} 'ps -ef|grep ${params.proName}/|grep -v grep|cut -c 9-15|xargs kill -9'"
//清除待部署项目缓存及代码
sh "ssh -f -n root@${params.serverIP} rm -rf ${cleanPathOne} ${cleanPathTwo}"
}
}
            
        stage('推送测试包'){ 
            steps {
             echo "starting deploy to ${params.serverIP}......"
             //发布jar包到指定服务器
             sh "scp ${params.warName}.war root@${params.serverIP}:${params.proPath}"
            }
        }


stage('部署服务'){
steps{
//解压war包
sh "ssh -f -n root@${params.serverIP} unzip ${params.proPath}${params.warName}.war -d ${params.proPath}"
}
steps{
//复制sh文件
sh "ssh -f -n root@${params.serverIP} cp ${shbackPath}${shbackName} ${shfile}"
}
steps{
//执行sh文件重启项目
sh "ssh -f -n root@${params.serverIP} ${shfile}${shbackName} -p preprod"
}
}

stage('发送邮件通知'){
steps{
emailext body：'''**************************************<br>
Spring3_VIP会员系统测试环境已提测部署<br>
**************************************<br>''', subject: "Spring3_VIP部署", to: "${emailOne} ${emailTwo} ${emailThree}"
}
}

    }
}
