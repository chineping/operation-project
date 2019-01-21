pipeline {
  agent any
  options { 
    timestamps () 
  }
  parameters {
    string(name:'repoUrl', defaultValue: 'http://svnIP/svn/ems/branches/H5_wuxinghui@HEAD', description: 'SVN代码路径')
    string(name:'serverIP', defaultValue: 'serverIP', description: '部署代码所在ip地址')
    string(name: 'proPath',defaultValue:'/usr/local/ems-web-www/webapps/ROOT', description: '服务路径')
    string(name: 'bakPath',defaultValue:'/usr/local/ems-web-www/war_bak', description: '备份路径')
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

    stage ('nodejs构建') {
      tools {
        nodejs 'NodeJS_V8.11.2'
      }
      steps {
        sh """
        rm -rf dist
        npm --version && node --version
        npm install && npm run build 
        """
      }
    }

    stage ('备份原代码'){
     steps{
      sh """
      ssh -f -n root@${params.serverIP} cp -a ${params.proPath} ${params.bakPath}/ROOT`date +%Y%m%d`
      ssh -f -n root@${params.serverIP} rm -rf ${params.proPath}/*
      """

      }
    }

    stage ('推送测试包'){
      steps {
        sh "scp dist/* root@${params.serverIP}:${params.proPath}"
      }
    }
  }
}