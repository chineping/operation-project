pipeline {
    agent any
    tools {
        maven 'maven3'
        jdk 'jdk1.8'
    }
    parameters {
        string(name:'serverIP', defaultValue: 'IP', description: '部署代码所在ip地址')
        string(name:'pomPath', defaultValue: 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/pom.xml', description: 'maven构建pom.xml')
        string(name:'jarPack', defaultValue: 'plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/target/plusplatform-product-harb-1.0.0-SNAPSHOT.jar', description: 'maven构建出的jar包')
        string(name: 'proPath',defaultValue:'/opt/plusplatform', description: '服务路径')
}
    stages {
        stage ('GetCode') {
            steps {
                git branch: 'bj_dev', credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: 'git@gitlabIP:plusplatform/plusplatform.git'
            }
        }

        stage ('Build') {
            steps {
                sh "mvn -f ${params.pomPath} -Dmaven.test.skip=true clean install"
            }
        }
    }
}