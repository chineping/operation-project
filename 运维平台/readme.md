# Django2.0.7+Python3.6.3+AdminLTE部署运维堡垒机方法

以下为Windows平台下部署步骤

## 1、创建虚拟环境

```
mkvirtualenv ironfort3
```

## 2、安装以下软件

```
pip install django pymysql argparse gevent gevent-websocket greenlet paramiko django-import-export python-jenkins
```

也可以使用django内File-->Setting-->Project-->Interpreter进行安装

## 3、新建Django项目

### 3.1 选择刚创建的虚拟环境

```
E:\Vir_Env\ironfort3\Scripts\python.exe
创建app法1：直接勾选创建app：fort
创建app法2：Tools--> Run manage.py task--> startapp fort
```

### 3.2 创建MySQL数据库

```
库名：ironfort3
字符集：utf8
排列规则：utf8_general_ci
```

### 3.3 修改Django配置文件settings.py

```
# Database
# https://docs.djangoproject.com/en/2.1/ref/settings/#databases

# DATABASES = {
#     'default': {
#         'ENGINE': 'django.db.backends.sqlite3',
#         'NAME': os.path.join(BASE_DIR, 'db.sqlite3'),
#     }
# }
import pymysql         # 一定要添加这两行！通过pip install pymysql！
pymysql.install_as_MySQLdb()

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': 'ironfort3',
        'HOST': '127.0.0.1',
        'USER': 'root',
        'PASSWORD': '',
        'PORT': '3306',
    }
}

#以下注释部分为原文件，修改如下
# LANGUAGE_CODE = 'en-us'
LANGUAGE_CODE = 'zh-hans'
# TIME_ZONE = 'UTC'
TIME_ZONE = 'Asia/Shanghai'
USE_I18N = True
USE_L10N = True
# USE_TZ = True
USE_TZ = False

STATIC_URL = '/static/'
```

### 3.4 生成数据表

```
Tools--> Run manage.py task--> makemigrations-->  migrate
```

### 3.5 创建models并生成数据表

```
1、编辑fort/models.py
2、Tools--> Run manage.py task--> makemigrations-->  migrate
```

### 3.6 安装xadmin

参考：https://blog.csdn.net/iloveyin/article/details/44647579

Django自带后台页面太丑了，所以使用xadmin，Django2.0的xadmin无法直接pip安装。

```
安装详情：https://github.com/sshwsfc/xadmin/tree/django2
pip install https://codeload.github.com/sshwsfc/xadmin/zip/django2
```

修改settings.py配置文件，增加xadmin

```
INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'fort.apps.FortConfig',
    'xadmin',
    'crispy_forms',
    'import_export',
]
```

修改urls.py

```
from django.contrib import admin
from django.urls import path
import xadmin

urlpatterns = [
    path('xadmin/', xadmin.site.urls),
]
```

修改admin.py-->adminx.py，复制adminx.py全部代码

并且在apps.py新增

```
class FortConfig(AppConfig):
    name = 'fort'
    verbose_name = "主机信息"
```

生成xadmin数据表，并创建超级管理员

```
1、Tools--> Run manage.py task--> makemigrations xadmin-->  migrate xadmin
2、Tools--> Run manage.py task--> createsuperuser
```

启动项目，登录后台并导入基础数据

```
远程主机：支持导入csv格式文件，不存在任何格式问题，可批量导入主机
远程主机用户：远程目标服务器的ssh登录用户，例如root
用户绑定主机：远程ssh用户与远程主机绑定
堡垒机用户：可登录该WEB堡垒机的用户
堡垒机用户组：可分为运维组，可关联登录多台服务器权限
堡垒机用户行为日志：通过远程ssh登录服务器获得
Jenkins列表：通过后面引入的jenkinsapi获得
```

### 3.7 复制整个项目

```
1、添加url
2、添加view
3、在app：fort中添加一个templates\fort
4、添加jenkins111.py #一定不要命名成jenkins.py
5、添加server.py
6、添加start_ironfort.py
7、添加static
```

启动项目http://127.0.0.1:8000/pull_job 拉取jenkins列表，看到ok

测试登录，项目完成，部署到linux上，方法基本相同，仅一些命令linux与windows不同。





参考链接

http://www.liujiangblog.com/blog/26/

http://www.liujiangblog.com/