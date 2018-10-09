# Scrapy爬取链家二手房信息并使用Django输出到前端

## 项目要求

```
链接二手房爬取https://sh.lianjia.com/ershoufang/p2
1、包含各个区 
2、价格：200-300万   
3、50-70平和70-90平   

爬取字段：
1、房屋id：housedelId
2、房屋总价：totalprice
3、房屋标题：title
4、房屋户型：houseType
5、建筑面积：constructionArea
6、房屋单价：unitprice
7、挂牌时间：listingTime
8、房屋朝向：toward
9、所在楼层：floor
10、建筑类型：buildingtype
11、是否电梯：hasElevator
12、装修类型：decorationType
13、产权年限：yearOfRight
14、建设年代：buildyear
15、房屋用途：useful   #注意这里不要定义成use，在插入数据库的时候会被当成特殊字符，而报错
16、上次交易：lastSell
17、30天带看：last30DayLead
18、关注人数：attention

取出的数据存入MySQL数据库，并使用Django新建项目，结合bootstrap在前端展示
```

## 新建爬虫项目

```
1、cd E:\PyCharm_Project\
2、mkvirtualenv lianjiaspider3
3、pip install -i https://pypi.douban.com/simple/ scrapy
4、scrapy startproject lianjiaspider3
5、cd lianjiaspider3 ;scrapy genspider lianjia sh.lianjia.com
#这里有个坑，链家url分网页版和移动手机版，测试时经常访问会自动跳转到m.lianjia.com造成url报错，或者数据结构不同，抓不到数据
6、python -m pip install pypiwin32
7、pip install requests pymysql selector
8、python -m pip install --upgrade pip==9.0.3
```

使用pycharm打开，配置file--> setting--> languages&frameworks--> sql dialects

## 编写项目

编辑items.py（项目里找lianjiaspider3\lianjiaspider3\items.py）

编辑settings.py，把原注释项取消注释，否则不会加载pipelines.py，添加user_agent

```
ITEM_PIPELINES = {
   'lianjiaspider.pipelines.LianjiaspiderPipeline': 300,
}
ROBOTSTXT_OBEY = False  #有时ROBOTSTXT_OBEY会报错，直接关闭
USER_AGENT = 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36'  #scrapy爬网站返回403错误的解决方法
```

本文由于爬取链家的多个区，配置USER_AGENT_LIST，在setttings.py同级目录下，自定义rotate_useragent.py(middleware文件)

```
import random
from scrapy.contrib.downloadermiddleware.useragent import UserAgentMiddleware
from lianjiaspider.settings import USER_AGENT_LIST 
#项目名.settting.py.引用USER_AGENT_LIST
from scrapy import log

class RandomUserAgentMiddleware(object):  #自定义middleware类
    def process_request(self, request, spider):
        ua  = random.choice(USER_AGENT_LIST)
        if ua:
            request.headers.setdefault('User-Agent', ua)
```

修改setting.py，注释USER_AGENT

```
DOWNLOADER_MIDDLEWARES = {
    'scrapy.contrib.downloadermiddleware.useragent.UserAgentMiddleware' : None,
    'lianjiaspider.rotate_useragent.RandomUserAgentMiddleware' :400
}  #yourproject_name(项目名).middlewares_name(文件名).middleware_name(类)

USER_AGENT_LIST = [
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1",
    "Mozilla/5.0 (X11; CrOS i686 2268.111.0) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11",
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6",
    "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1090.0 Safari/536.6",
    "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/19.77.34.5 Safari/537.1",
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.9 Safari/536.5",
    "Mozilla/5.0 (Windows NT 6.0) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.36 Safari/536.5",
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1063.0 Safari/536.3",
    "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1063.0 Safari/536.3",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_0) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1063.0 Safari/536.3",
    "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1062.0 Safari/536.3",
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1062.0 Safari/536.3",
    "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.1 Safari/536.3",
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.1 Safari/536.3",
    "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.1 Safari/536.3",
    "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.0 Safari/536.3",
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.24 (KHTML, like Gecko) Chrome/19.0.1055.1 Safari/535.24",
    "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/535.24 (KHTML, like Gecko) Chrome/19.0.1055.1 Safari/535.24"
]
```

编辑pipelines.py，将爬出的数据存入数据库

```
import pymysql
pymysql.install_as_MySQLdb()

class Lianjiaspider3Pipeline(object):
    def open_spider(self, spider):
        self.con = pymysql.connect('localhost', 'root', '', 'lianjia3')
        self.cursor = self.con.cursor()

    def process_item(self, item, spider):
        insert_sql = "INSERT INTO lianjia3(housedelId, totalPrice, title, houseType, constructionArea, unitPrice, listingTime, toward, floor, buildingtype, hasElevator, decorationType, buildyear, useful, yearOfRight, lastSell, last30DayLead, attention) VALUES ('{}', '{}', '{}', '{}','{}', '{}', '{}', '{}','{}', '{}', '{}', '{}','{}', '{}', '{}', '{}','{}', '{}')".format(item['housedelId'], item['totalPrice'], item['title'], item['houseType'], item['constructionArea'], item['unitPrice'], item['listingTime'], item['toward'], item['floor'], item['buildingtype'], item['hasElevator'], item['decorationType'], item['buildyear'], item['useful'], item['yearOfRight'], item['lastSell'], item['last30DayLead'], item['attention'])
        # insert_sql = "INSERT INTO lianjia3(housedelId, totalPrice) VALUES ('{}', '{}')".format(item['housedelId'], item['totalPrice'])
        self.cursor.execute(insert_sql)  # 执行sql语句
        self.con.commit()  # 提交到数据库，insert和updata语句必须执行这句
        return item

    def spider_close(self, spider):
        self.con.close()
```

编写一个启动文件run.py

```
from scrapy import cmdline
cmdline.execute("scrapy crawl lianjia3".split())
```

编写spiders下lianjia3.py

```
# -*- coding: utf-8 -*-
import scrapy
from lianjiaspider3.items import Lianjiaspider3Item
from scrapy.selector import Selector
from scrapy.http import Request

class Lianjia3Spider(scrapy.Spider):
    name = 'lianjia3'
    allowed_domains = ['sh.lianjia.com']
    start_urls = []
    resions = {
        # 'pudongxinqu': '浦东新区',
        'minhang': '闵行'
        # 'baoshan': '宝山',
        # 'xuhui': '徐汇',
        # 'putuo': '普陀',
        # 'yangpu': '杨浦',
        # 'changning': '长宁',
        # 'songjiang': '松江',
        # 'jiading': '嘉定',
        # 'huangpu': '黄埔',
        # 'jingan': '静安',
        # 'zhabei': '闸北',
        # 'hongkou': '虹口',
        # 'qingpu': '青浦',
        # 'fengxian': '奉贤',
        # 'jinshan': '金山',
        # 'chongming': '崇明',
        # 'shanghaizhoubian': '上海周边'
    } 	#此处注释原因为抓取时间过长
    for region in list(resions.keys()):
        for i in range(1,2):
            start_urls.append('https://sh.lianjia.com/ershoufang/' + region + '/pg' + str(i) + "a2a3p2/")

#此处是在页面上循环抓hrefs的具体url
    def parse(self, response):
        li_item = response.xpath('//li[@class="clear LOGCLICKDATA"]')
        for li in li_item:
            hrefs = li.xpath('//a[@class="noresultRecommend img "]/@href').extract()
            #print(hrefs)
            for href in hrefs:
                #进入详情，继续抓, 进入子页面函数parse_item
                yield scrapy.Request(url=href, callback=self.parse_item, dont_filter=True)

    def parse_item(self, response):
        item = Lianjiaspider3Item()
        housedelId_list = response.xpath('//a/@data-lj_action_housedel_id').extract()
        totalPrice_list = response.xpath('//div[@class="price "]/span[@class="total"]/text()').extract()
        title_list = response.xpath('//div[@class="sellDetailHeader"]/div[@class="title-wrapper"]/div[@class="content"]/div[@class="title"]/h1/text()').extract()
        houseType_list = response.xpath('//div[@class="introContent"]/div[@class="base"]/div[@class="content"]/ul/li[1]/text()').extract()
        constructionArea_list = response.xpath('//div[@class="introContent"]/div[@class="base"]/div[@class="content"]/ul/li[3]/text()').extract()
        unitPrice_list = response.xpath('//div[@class="unitPrice"]/span[@class="unitPriceValue"]/text()').extract()
        listingTime_list = response.xpath('//div[@class="transaction"]/div[@class="content"]/ul/li[1]/span[2]/text()').extract()
        toward_list = response.xpath('//div[@class="introContent"]/div[@class="base"]/div[@class="content"]/ul/li[7]/text()').extract()
        floor_list = response.xpath('//div[@class="introContent"]/div[@class="base"]/div[@class="content"]/ul/li[2]/text()').extract()
        buildingtype_list = response.xpath('//div[@class="introContent"]/div[@class="base"]/div[@class="content"]/ul/li[6]/text()').extract()
        hasElevator_list = response.xpath('//div[@class="introContent"]/div[@class="base"]/div[@class="content"]/ul/li[11]/text()').extract()
        decorationType_list = response.xpath('//div[@class="introContent"]/div[@class="base"]/div[@class="content"]/ul/li[9]/text()').extract()
        yearOfRight_list = response.xpath('//div[@class="introContent"]/div[@class="base"]/div[@class="content"]/ul/li[12]/text()').extract()
        #['高楼层/共6层', '平层/精装', '1994年建/板楼']
        # buildyear_list = response.xpath('//div[@class="subInfo"]/text()').extract()[2]
        buildyear_list = response.xpath('//div[contains(text(),"年建")]/text()').extract()
        # print(buildyear_list) #此处难点，后来找到contains方法简直太简便了
        useful_list = response.xpath('//div[@class="transaction"]/div[@class="content"]/ul/li[4]/span[2]/text()').extract()
        lastSell_list = response.xpath('//div[@class="transaction"]/div[@class="content"]/ul/li[3]/span[2]/text()').extract()
        last30DayLead_list = response.xpath('//div[@class="panel"]/div[@class="totalCount"]/span/text()').extract()
        attention_list = response.xpath('//span[@id="favCount"]/text()').extract()

        for a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r in zip(housedelId_list, totalPrice_list, title_list, houseType_list, constructionArea_list, unitPrice_list, listingTime_list, toward_list, floor_list, buildingtype_list, hasElevator_list, decorationType_list, yearOfRight_list, buildyear_list, useful_list, lastSell_list, last30DayLead_list, attention_list):
            item['housedelId'] = a
            item['totalPrice'] = b
            item['title'] = c
            item['houseType'] = d
            item['constructionArea'] = e
            item['unitPrice'] = f
            item['listingTime'] = g
            item['toward'] = h
            item['floor'] = i
            item['buildingtype'] = j
            item['hasElevator'] = k
            item['decorationType'] = l
            item['yearOfRight'] = m
            item['buildyear'] = n
            item['useful'] = o
            item['lastSell'] = p
            item['last30DayLead'] = q
            item['attention'] = r
            yield item
```

## 调试过程

get请求输出网页内容，便于抓取getpage1-3

```
from urllib.parse import urljoin
from scrapy.http import Request
import requests
from lxml import etree
import csv

title_list = []
title_list.append(['标题',])
num_list = list(range(1,2))
# num_list = (2,)
for num in num_list:
    # page_url = 'https://m.lianjia.com/sh/ershoufang/pudong/pg' + str(num) + 'a2a3p2'
    page_url = 'https://sh.lianjia.com/ershoufang/107100328528.html'
    # page_url = 'https://sh.lianjia.com/ershoufang/pg3a2a3p2'
    page = requests.get(page_url).text
    # print(page)
    text = etree.HTML(page)

    # ['高楼层/共6层', '平层/精装', '1994年建/板楼']
    # buildyear = text.xpath('//html/body/div[@class="overview"]/div[@class="content"]/div[@class="houseInfo"]/div[@class="area"]/div[@class="subInfo"]/text()')
    # buildyear = text.xpath('//a[contains(text(),"年建")]')
    buildyear = text.xpath('//div[contains(text(),"年建")]/text()')
    # buildyear = text.xpath('//div[@class="subInfo"]/text()')
    print(buildyear)
```

使用pycharm的debug模式，打断点，可输出每一步运行状态，下方显示page内容，右键copy value到一个文本编辑器中，找到title、resion等字段。

在运行爬虫的过程中，会显示有些页面连接超时，继续等待即可，爬取的区域和页数越多，爬取时间较长。

## Django把爬取来的数据显示到页面

### 新建Django项目

```
1、pip install virtualenv virtualenvwrapper-win
2、mkvirtualenv lianjia3_django
3、pip install -i https://pypi.douban.com/simple django pymysql
#插件也可以使用django内File-->Setting-->Project-->Interpreter进行安装
4、使用pycharm选择刚创建的虚拟环境（E:\Vir_Env\lianjia3_django\Scripts\python.exe）新建django项目，直接勾选创建app：lianjia3
5、修改Django配置文件settings.py
```

#### settings.py主要配置，详情见代码

```
import pymysql         # 一定要添加这两行！通过pip install pymysql！
pymysql.install_as_MySQLdb()

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': 'lianjia3',
        'HOST': '127.0.0.1',
        'USER': 'root',
        'PASSWORD': '',
        'PORT': '3306',
    }
}

#以下注释部分为原文件，修改如下
LANGUAGE_CODE = 'zh-hans'
TIME_ZONE = 'Asia/Shanghai'
USE_I18N = True
USE_L10N = True
USE_TZ = False

STATIC_URL = '/static/'
STATICFILES_DIRS = [
   os.path.join(BASE_DIR, 'static'),
]
```

#### 编辑lianjia3_django\lianjia3\models.py

6、数据库选择爬虫使用的数据库即可lianjia3，Tools--> Run manage.py task--> makemigrations-->  migrate，django项目会在原数据库内自动新建默认表及module自定义表，自定义数据表内无数据，把爬虫数据导过去（导的过程中，django会自动生成primary key的id列，需要在原爬虫数据表中新增id列，这样才能数据对应上）

```
from django.db import models

# Create your models here.
class lianjia3(models.Model):
    housedelId = models.IntegerField(verbose_name="房屋id")
    totalPrice = models.CharField(max_length=255, verbose_name="房屋总价")
    title = models.CharField(max_length=255, verbose_name="房屋标题")
    houseType = models.CharField(max_length=255, verbose_name="房屋户型")
    constructionArea = models.CharField(max_length=255, verbose_name="建筑面积")
    unitPrice = models.IntegerField(verbose_name="房屋单价")
    listingTime = models.DateTimeField(verbose_name="挂牌时间")
    toward = models.CharField(max_length=255, verbose_name="房屋朝向")
    floor = models.CharField(max_length=255, verbose_name="所在楼层")
    buildingtype = models.CharField(max_length=255, verbose_name="建筑类型")
    hasElevator = models.CharField(max_length=255, verbose_name="是否电梯")
    decorationType = models.CharField(max_length=255, verbose_name="装修类型")
    yearOfRight = models.CharField(max_length=255, verbose_name="产权年限")
    buildyear = models.CharField(max_length=255, verbose_name="建设年代")
    useful = models.CharField(max_length=255, verbose_name="房屋用途")
    lastSell = models.CharField(max_length=255, verbose_name="上次交易")
    last30DayLead = models.CharField(max_length=255, verbose_name="30天带看")
    attention = models.CharField(max_length=255, verbose_name="关注人数")

    class Meta:
        verbose_name = '二手房信息'
        verbose_name_plural = verbose_name

    def __str__(self):
        return self.title

```

#### 编辑lianjia3_django\lianjia3\views.py

```
from django.shortcuts import render
from lianjia3 import forms

from lianjia3.models import lianjia3
from django.shortcuts import HttpResponseRedirect,Http404,HttpResponse,render_to_response
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger
#此处加入分页

# Create your views here.
def table(request):
    # table_form = forms.SignupForm()
    limit = 5  # 每页显示的记录数
    lists = lianjia3.objects.all()
    paginator = Paginator(lists, limit)  # 实例化一个分页对象

    page = request.GET.get('page')  # 获取页码
    try:
        lists = paginator.page(page)  # 获取某页对应的记录
    except PageNotAnInteger:  # 如果页码不是个整数
        lists = paginator.page(1)  # 取第一页的记录
    except EmptyPage:  # 如果页码太大，没有相应的记录
        lists = paginator.page(paginator.num_pages)  # 取最后一页的记录

    return render_to_response('table.html', {'lists': lists})
    # return render_to_response("table.html", locals())
```

#### 编辑lianjia3_django\urls.py

```
from django.contrib import admin
from django.urls import path
from lianjia3 import views

urlpatterns = [
    path('admin/', admin.site.urls),
    path('table/',views.table, name='table'),
]
```

#### 编辑lianjia3_django\templates\table.html

```
{% load static %}
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>二手房信息</title>
    <link rel="stylesheet" href="{% static 'css/bootstrap.min.css' %}">
   <script src="{% static 'jquery-3.3.1/jquery-3.3.1.min.js' %}"></script>
   <script src="{% static 'js/bootstrap.min.js' %}"></script>
</head>
<body>
<div class="container-fluid">
      <ol class="breadcrumb my-4">
        <li class="breadcrumb-item active">二手房信息</li>
      </ol>
      <table class="table">
        <thead class="thead-inverse">
          <tr>
            <th>id</th>
            <th>totalPrice</th>
            <th>title</th>
            <th>houseType</th>
            <th>constructionArea</th>
            <th>unitPrice</th>
            <th>listingTime</th>
            <th>toward</th>
            <th>floor</th>
            <th>buildingtype</th>
            <th>hasElevator</th>
            <th>decorationType</th>
{#            <th>yearOfRight</th>#}
{#            <th>buildyear</th>#}
{#            <th>useful</th>#}
{#            <th>lastSell</th>#}
{#            <th>last30DayLead</th>#}
{#            <th>attention</th>#}
          </tr>
        </thead>
        <tbody>
          {% for item in lists %}
                <tr>
                    <td>{{ forloop.counter }}</td>
                    <td>{{ item.totalPrice }}</td>
                    <td>{{ item.title }}</td>
                    <td>{{ item.houseType }}</td>
                    <td>{{ item.constructionArea }}</td>
                    <td>{{ item.unitPrice }}</td>
                    <td>{{ item.listingTime }}</td>
                    <td>{{ item.toward }}</td>
                    <td>{{ item.floor }}</td>
                    <td>{{ item.buildingtype }}</td>
                    <td>{{ item.hasElevator }}</td>
                    <td>{{ item.decorationType }}</td>
{#                    <td>{{ item.yearOfRight }}</td>#}
{#                    <td>{{ item.buildyear }}</td>#}
{#                    <td>{{ item.useful }}</td>#}
{#                    <td>{{ item.lastSell }}</td>#}
{#                    <td>{{ item.last30DayLead }}</td>#}
{#                    <td>{{ item.attention }}</td>#}
                </tr>
          {% endfor %}
        </tbody>
      </table>
{#<p>#} #第一种分页方式
  {# topics.paginator.page_range 这个函数返回包含一个所有页码数的 range 对象 #}
  {# 即 range(1, topics.paginator.num_pages + 1) #}
{#  {% for page_number in lists.paginator.page_range %}#}
{#    {% ifequal page_number  topics.number %}#}
{#      {{ page_number }}#}
{#    {% else %}#}
{#      <a href="?page={{ page_number }}">{{ page_number }}</a>#}
{#    {% endifequal %}#}
{#  {% endfor %}#}
{#</p>#}
<p> #第二种分页方式
{% if lists.has_previous %}
  <a href="?page={{ lists.previous_page_number }}">Previous</a>
  {% endif %}
  {# topics.paginator.number_pages 返回总页数 #}
  Page {{ lists.number }} of {{ lists.paginator.num_pages }}.
{% if lists.has_next %}
  <a href="?page={{ lists.next_page_number }}">Next</a>
{% endif %}
</p>
    </div>
</body>
</html>
```

#### 下载bootstrap静态资源到static目录

```
E:\链家_爬虫2\lianjia3_django\static\css
E:\链家_爬虫2\lianjia3_django\static\js
E:\链家_爬虫2\lianjia3_django\static\jquery-3.3.1
#html内引用，格式
```

注意：爬虫项目和Django项目中多余的文件为调试及测试过程，可以忽略

## 参考文档

##### Scrapy 增加随机请求头user_agent：https://www.cnblogs.com/dahu-daqing/p/7580719.html

##### Scrapy爬取数据存入MySQL数据库：https://www.jianshu.com/p/ad7ba01b0e77

##### Scrapy爬虫——xpath与css选择器详解：https://www.jianshu.com/p/489c5d21cdc7

##### Scrapy 抓取数据提取对象小技巧：https://www.jianshu.com/p/ed9290b3cfc7

##### Django读取Mysql数据并显示在前端：https://blog.csdn.net/GitzLiu/article/details/54627517

##### 使用 Paginator 实现分页功能：https://mozillazg.com/2013/01/django-pagination-by-use-paginator.html

