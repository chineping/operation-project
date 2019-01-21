pipeline {
      agent any
      options { 
            timestamps () 
      }
      parameters {
            string(name:'repoUrl', defaultValue: 'http://svnIP/svn/ems/trunk@HEAD', description: 'SVN代码路径')
            string(name:'pomPath', defaultValue: 'ems-pom/pom.xml', description: 'maven构建pom.xml')
            string(name:'mavenSettingPath', defaultValue: '/var/lib/jenkins/conf/mps/settings.xml', description: 'maven构建settings.xml')
            booleanParam(defaultValue: true, name: 'ALL', description: 'Process all')
            booleanParam(defaultValue: false, name: 'OPTION_1', description: 'Process option 1')
            booleanParam(defaultValue: false, name: 'OPTION_2', description: 'Process options 2')
      }
      environment {
            CRED_ID='67486f7c-f59b-45b0-87b2-f063a8b570ad'
      }
      stages {
            stage ('Checkout') {
                  steps {
                        script {
                        def scmVars = checkout ([$class: 'SubversionSCM', 
                            filterChangelog: false, 
                            ignoreDirPropChanges: false, 
                            locations: [[credentialsId: CRED_ID, 
                            depthOption: 'infinity', 
                            ignoreExternalsOption: true, 
                            local: '.', 
                            remote: params.repoUrl]], 
                            workspaceUpdater: [$class: 'UpdateUpdater']])
                      svnversion = scmVars.SVN_REVISION
                        }
                        sh "echo 当前版本：${svnversion}"
                  }
            }
            stage ('maven构建') {
                  steps {
                        withMaven(jdk: 'jdk1.7', maven: 'maven3.3.9') { 
                              sh "mvn -f ${params.pomPath} -s ${params.mavenSettingPath} -Dmaven.test.skip=true clean install -U -Pnsit" 
                        }
                  }
            }
      }
}