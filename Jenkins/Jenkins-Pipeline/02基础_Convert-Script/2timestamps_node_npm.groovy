// Powered by Infostretch 

timestamps {

node () {

	stage ('harbin_dev_H5_npm - Checkout') {
 	 checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '06c8476d-ef81-4c84-b1eb-71d341abf97b', url: 'git@gitlabIP:BJ/haerbin-web.git']]]) 
	}
	stage ('harbin_dev_H5_npm - Build') {
 	
withEnv(["JAVA_HOME=${ tool '"+JDK+"' }", "PATH=${env.JAVA_HOME}/bin"]) { 

// Unable to convert a build step referring to "jenkins.plugins.nodejs.NodeJSBuildWrapper". Please verify and convert manually if required.
// Unable to convert a build step referring to "org.jvnet.hudson.plugins.SSHBuilder". Please verify and convert manually if required.		// Shell build step
sh """ 
echo $PATH
node -v
npm -v

npm install
npm run build


scp -r /var/lib/jenkins/workspace/harbin_dev_H5_npm/dist/* root@172.26.21.11:/data/harbin 
 """ 
	}
}
}
}