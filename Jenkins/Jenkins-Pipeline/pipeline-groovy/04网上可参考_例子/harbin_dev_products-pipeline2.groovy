pipeline {
//在任何可用的代理上执行Pipeline
    agent any
//参数化变量，目前只支持[booleanParam, choice, credentials, file, text, password, run, string]这几种参数类型，其他高级参数化类型还需等待社区支持。
    parameters {
//git代码路径【参数值对外隐藏】
    string(name:'repoUrl', defaultValue: 'git@gitlabIP:plusplatform/plusplatform.git', description: 'git代码路径')
//repoBranch参数后续替换成git parameter不再依赖手工输入,JENKINS-46451【git parameters目前还不支持pipeline】
    string(name:'repoBranch', defaultValue: 'bj_dev', description: 'git分支名称')
//部署代码所在IP
    string(name:'serverIP', defaultValue: 'IP', description: '部署代码所在ip地址')
//maven构建pom.xml
	string(name:'pomPath', defaultValue: 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb', description: 'maven构建pom.xml')
//maven构建出的jar包
	string(name:'jarPack', defaultValue: 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/target/plusplatform-product-harb-1.0.0-SNAPSHOT.jar', description: 'maven构建出的jar包')
//服务路径
	string(name: 'proPath',defaultValue:'/opt/plusplatform', description: '服务路径')
}
//常量参数，初始确定后一般不需更改
    environment{
//git服务全系统只读账号cred_id【参数值对外隐藏】
    CRED_ID='06c8476d-ef81-4c84-b1eb-71d341abf97b'
    }

//获取包名
script{
def split=params.jarPack.split("/")
    jarPackname=split[-1]
}

stage('编译') {
withEnv(["JAVA_HOME=${ tool '"+JDK+"' }", "PATH=${env.JAVA_HOME}/bin"]) { 
		// Maven build step
	withMaven(jdk: 'jdk1.8', maven: 'maven3') { 
 		sh "mvn -f ${params.pomPath}/pom.xml clean
install
-Dmaven.test.skip=true
 " 
 		}
}
	}

stage('推送测试包'){
    steps {
    echo "starting deploy to ${params.serverIP}......"
//发布jar包到指定服务器
    sh "scp ${params.jarPack} root@${params.serverIP}:${params.proPath}"
    sh "scp /var/lib/ms root@${params.serverIP}:${params.proPath} ${params.proPath}"
    }
}

stage('重启服务'){
	steps{
//执行sh文件重启项目
	sh "ssh -f -n root@${params.serverIP} chmod +x ${params.proPath}/ms"
	sh "ssh -f -n root@${params.serverIP} ${params.proPath}/ms ${params.jarPackname}"
	}
}
}