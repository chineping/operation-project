# Generated by Django 2.1 on 2018-08-13 15:56

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('fort', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='group',
            name='group_name',
            field=models.CharField(max_length=64, unique=True, verbose_name='堡垒机用户组名'),
        ),
    ]
