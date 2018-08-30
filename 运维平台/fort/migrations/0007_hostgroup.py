# Generated by Django 2.1 on 2018-08-13 16:59

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('fort', '0006_auto_20180813_1634'),
    ]

    operations = [
        migrations.CreateModel(
            name='HostGroup',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('host_groupname', models.CharField(max_length=100, unique=True, verbose_name='远程主机组')),
                ('memo', models.TextField(blank=True, null=True, verbose_name='备注')),
                ('remote_host_bind_hosts', models.ManyToManyField(blank=True, to='fort.Host', verbose_name='组内关联的远程主机')),
            ],
            options={
                'verbose_name': '远程主机组',
                'verbose_name_plural': '远程主机组',
            },
        ),
    ]
