# Scrapy爬取列表网商业贷款中介信息

## 背景

目前市场欺诈及逾期案件多发，为识别申请的中介代办或多头负债风险，需要从网络渠道获取中介电话并建立中介电话数据库。需求详见贷款中介电话数据爬取，由于该文档的需求提出时间的比较早，各种页面已经变更、反爬虫或者需要登录。后续将继续研究，后续再更新。

## 新建项目

```
1、cd E:\PyCharm_Project\
2、mkvirtualenv liebiaowangspider
3、pip install -i https://pypi.douban.com/simple/ scrapy
4、scrapy startproject liebiaowang
5、cd SpiderTest ;scrapy genspider liebiao shanghai.liebiao.com
6、python -m pip install pypiwin32
7、pip install requests pymysql
8、python -m pip install --upgrade pip==9.0.3
```

使用pycharm打开，配置file--> setting--> languages&frameworks--> sql dialects

## 编写项目

编辑items.py

```
import scrapy
class LiebiaowangItem(scrapy.Item):
    title = scrapy.Field()
    number = scrapy.Field()
```

编辑settings.py，把原注释项取消注释，否则不会加载pipelines.py，添加user_agent

```
ITEM_PIPELINES = {
   'liebiaowang.pipelines.LiebiaowangPipeline': 300,
}

USER_AGENT = 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36'  #scrapy爬网站返回403错误的解决方法
```

编辑pipelines.py，将爬出的数据存入数据库

```
import pymysql
pymysql.install_as_MySQLdb()

class LiebiaowangPipeline(object):
    def open_spider(self, spider):
        self.con = pymysql.connect('localhost', 'root', '', 'liebiaowang')  #在本地建立一个dbname：liebiaowang
        self.cursor = self.con.cursor()

    def process_item(self, item, spider):
        insert_sql = "INSERT INTO liebiaowang(title, number) VALUES ('{}', '{}')".format(item['title'], item['number'])  #在liebiaowang这一数据库内新建tbname：liebiaowang，建立2个字段title和number
        self.cursor.execute(insert_sql)  # 执行sql语句
        self.con.commit()  # 提交到数据库，insert和updata语句必须执行这句
        return item

    def spider_close(self, spider):
        self.con.close()
```

编写一个启动文件run.py

```
from scrapy import cmdline
cmdline.execute("scrapy crawl liebiao".split())
```

编写spiders下liebiao.py

```
# -*- coding: utf-8 -*-
import scrapy
import requests
from lxml import etree
from ..items import LiebiaowangItem

class LiebiaoSpider(scrapy.Spider):
    name = 'liebiao'
    allowed_domains = ['shanghai.liebiao.com']
    start_urls = []
    for i in range(1, 2):      #爬取第一、二页  start_urls.append('http://shanghai.liebiao.com/jinrongdaikuan/index' + str(i) + '.html')  #查找规律，index1.html、index2.html..

    def parse(self, response):
        item = LiebiaowangItem()
        title_list = response.xpath("//div[@class='post-title-wrap']/h2/a/text()").extract()
        # print(title_list)
        number_list = response.xpath("//div[@class='contact-info']/p[@class='contact-phone']/text()").extract()
        # print(number_list)
        
        for i, j in zip(title_list, number_list):
            item['title'] = i
            item['number'] = j
            yield item
```

## 调试过程

get请求输出网页内容，便于抓取

```
import requests
from lxml import etree
import csv

num_list = list(range(1,2))
#num_list = (2,)
for num in num_list:
    page_url = 'http://shanghai.liebiao.com/jinrongdaikuan/index'   + str(num) + '.html'
    page = requests.get(page_url).text
    print(page)
```

使用pycharm的debug模式，打断点，可输出每一步运行状态，下方显示page内容，右键copy value到一个文本编辑器中，找到title、number字段

## 参考文档

##### Scrapy爬取数据存入MySQL数据库：https://www.jianshu.com/p/ad7ba01b0e77

##### Scrapy爬虫——xpath与css选择器详解：https://www.jianshu.com/p/489c5d21cdc7

##### Scrapy 抓取数据提取对象小技巧：https://www.jianshu.com/p/ed9290b3cfc7