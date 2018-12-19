#!groovy
pipeline {
//在任何可用的代理上执行Pipeline
    agent any
//参数化变量，目前只支持[booleanParam, choice, credentials, file, text, password, run, string]这几种参数类型，其他高级参数化类型还需等待社区支持。
    parameters {
//部署代码所在IP
    string(name:'serverIP', defaultValue: 'IP', description: '部署代码所在ip地址')
//maven构建pom.xml
    string(name:'pomPath', defaultValue: 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb', description: 'maven构建pom.xml')
//maven构建出的jar包
    string(name:'jarPack', defaultValue: 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/target/plusplatform-product-harb-1.0.0-SNAPSHOT.jar', description: 'maven构建出的jar包')
//服务路径
    string(name: 'proPath',defaultValue:'/opt/plusplatform', description: '服务路径')
}
    //丢弃旧的构建.构建天数：3,最大个数：10,发布包保留天数：3,发布包保留个数：1
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '3', artifactNumToKeepStr: '1', daysToKeepStr: '3', numToKeepStr: '10')
    }

    stages {
        stage('get code') {
            git branch: 'bj_dev', credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: 'git@gitlabIP:plusplatform/plusplatform.git'
        }

        stage('build') {
            withEnv(["JAVA_HOME=${ tool '"+JDK+"' }", "PATH=${env.JAVA_HOME}/bin"]) { 
        // Maven build step
            withMaven(jdk: 'jdk1.8', maven: 'maven3') { 
                sh "mvn -f ${params.pomPath}/pom.xml clean 
                install 
                -Dmaven.test.skip=true" 
                }
            }
        }
    }
}