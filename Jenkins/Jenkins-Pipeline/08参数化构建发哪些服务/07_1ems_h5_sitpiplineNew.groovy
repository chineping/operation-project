pipeline {
  agent any
  options { 
    timestamps () 
  }
  parameters {
    string(name:'repoUrl', defaultValue: 'http://svnIP/svn/ems/branches/H5_wuxinghui@HEAD', description: 'SVN代码路径')
    string(name:'serverIP', defaultValue: 'serverIP', description: '部署代码所在ip地址')
    string(name: 'proPath',defaultValue:'/usr/local/tomcat/webapps/ROOT', description: '服务路径')
    string(name: 'bakPath',defaultValue:'/data/bak', description: '备份路径')
  }
  environment {
    CRED_ID='6d6075b3-1250-410a-93a9-0e12d671cd4d'
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
      ssh -f -n root@${params.serverIP} -p 7980 cp -a ${params.proPath} ${params.bakPath}/ROOT`date +%Y%m%d`
      ssh -f -n root@${params.serverIP} -p 7980 rm -rf ${params.proPath}/*
      """

      }
    }

    stage ('推送测试包'){
      steps {
        sh "scp -P 7980 dist/* root@${params.serverIP}:${params.proPath}"
      }
    }
  }
}