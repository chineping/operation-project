// Powered by Infostretch 

timestamps {

node () {

	stage ('harbin_dev_products - Checkout') {
 	 checkout([$class: 'GitSCM', branches: [[name: '*/bj_dev']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: 'git@gitlabIP:plusplatform/plusplatform.git']]]) 
	}
	stage ('harbin_dev_products - Build') {
 	
withEnv(["JAVA_HOME=${ tool '"+JDK+"' }", "PATH=${env.JAVA_HOME}/bin"]) { 
		// Maven build step
	withMaven(jdk: 'jdk1.8', maven: 'maven3') { 
 			if(isUnix()) {
 				sh "mvn -f /var/lib/jenkins/workspace/harbin_dev_products/plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/pom.xml clean
install
-Dmaven.test.skip=true
 " 
			} else { 
 				bat "mvn -f /var/lib/jenkins/workspace/harbin_dev_products/plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/pom.xml clean
install
-Dmaven.test.skip=true
 " 
			} 
 		}		// Shell build step
sh """ 
scp /var/lib/jenkins/workspace/harbin_dev_products/plusplatform-fronts/plusplatform-front-products/plusplatform-product-harb/target/plusplatform-product-harb-1.0.0-SNAPSHOT.jar root@IP:/opt/plusplatform
scp /var/lib/ms root@IP:/opt/plusplatform 
 """
// Unable to convert a build step referring to "org.jvnet.hudson.plugins.SSHBuilder". Please verify and convert manually if required. 
	}
}
}
}