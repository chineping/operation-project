# !/usr/bin/env python
# -*-coding:utf-8-*-
import os

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "IronFort2.settings")

'''
Django 版本大于等于1.7的时候，需要加上下面两句
import django
django.setup()
否则会抛出错误 django.core.exceptions.AppRegistryNotReady: Models aren't loaded yet.
'''

import django

if django.VERSION >= (1, 7):  # 自动判断版本
    django.setup()

import jenkins
jenkins_server_url = 'http://10.90.4.5:8080'
user_id = 'zhangxy'
api_token = '6233195386ec0d9cdbde367459232097'


def get_job_info():
    server = jenkins.Jenkins(jenkins_server_url, username=user_id, password=api_token)
    my_jobs = server.get_all_jobs()
    # print(my_jobs)
    data = [
            {'job_name': _['name'], 'build_url': _['url']}
            for _ in my_jobs
            ]
    return data


f = open('fort\jenkins_info.txt','w')
f.write(str(get_job_info()))
f.close()


def update_job_info():
    from fort.models import JenkinsList
    info = get_job_info()
    Jnames = [_['job_name'] for _ in info]
    for i in info:
        JenkinsList.objects.get_or_create(job_name=i['job_name'],build_url=i['build_url'])
    jenkinsnames = [_['job_name'] for _ in JenkinsList.objects.all().values('job_name')]
    diff_names = list(set(jenkinsnames).difference(set(Jnames)))
    JenkinsList.objects.filter(job_name__in=diff_names).delete()














