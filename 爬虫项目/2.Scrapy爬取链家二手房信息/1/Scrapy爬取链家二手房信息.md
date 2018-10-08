# Scrapy爬取链家二手房信息

### 链家爬虫1，仅包含浦东新区，未做到包含上海各个区，抓取字段较少

## 项目要求

```
链接二手房爬取https://sh.lianjia.com/ershoufang/p2
1、包含各个区 
2、价格：200-300万   
3、50-70平和70-90平   

爬取字段：
1、标题：title
2、小区名：resion
3、位置：position
4、单价：unitprice
5、标价金额：totalprice
```

## 新建项目

```
1、cd E:\PyCharm_Project\
2、mkvirtualenv lianjiaspider
3、pip install -i https://pypi.douban.com/simple/ scrapy
4、scrapy startproject lianjiaspider
5、cd lianjiaspider ;scrapy genspider lianjia sh.lianjia.com
6、python -m pip install pypiwin32
7、pip install requests pymysql selector
8、python -m pip install --upgrade pip==9.0.3
```

使用pycharm打开，配置file--> setting--> languages&frameworks--> sql dialects

## 编写项目

编辑items.py

```
import scrapy
class LianjiaspiderItem(scrapy.Item):
    title = scrapy.Field()      # 用于存储房子标题
    resion = scrapy.Field()     # 用于存储小区名称
    position = scrapy.Field()   # 用户存储房子楼层信息
    unitprice = scrapy.Field()  # 用于存储房子的单价
    totalprice = scrapy.Field() # 用于存储房子的总价
```

编辑settings.py，把原注释项取消注释，否则不会加载pipelines.py，添加user_agent

```
ITEM_PIPELINES = {
   'lianjiaspider.pipelines.LianjiaspiderPipeline': 300,
}

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

class LianjiaspiderPipeline(object):
    def open_spider(self, spider):
        self.con = pymysql.connect('localhost', 'root', '', 'lianjia')
        self.cursor = self.con.cursor()

    def process_item(self, item, spider):
        insert_sql = "INSERT INTO lianjia(title, resion, unitprice, totalprice) VALUES ('{}', '{}', '{}', '{}')".format(item['title'], item['resion'], item['unitprice'], item['totalprice'])
        self.cursor.execute(insert_sql)  # 执行sql语句
        self.con.commit()  # 提交到数据库，insert和updata语句必须执行这句
        return item

    def spider_close(self, spider):
        self.con.close()

```

编写一个启动文件run.py

```
from scrapy import cmdline
cmdline.execute("scrapy crawl lianjia".split())
```

编写spiders下lianjia.py

```
# -*- coding: utf-8 -*-
import scrapy
from lianjiaspider.items import LianjiaspiderItem
from scrapy.selector import Selector

class LianjiaSpider(scrapy.Spider):
    name = 'lianjia'
    allowed_domains = ['sh.lianjia.com']
    start_urls = []
    # 指定爬虫所需的上海各个区域名称
    # citys = ['pudongxinqu', 'minhang', 'baoshan', 'xuhui', 'putuo', 'yangpu', 'changning', 'songjiang',
    #          'jiading', 'huangpu', 'jingan', 'zhabei', 'hongkou', 'qingpu', 'fengxian', 'jinshan', 'chongming',
    #          'shanghaizhoubian']
    for j in ('jiading', 'huangpu', 'jingan', 'zhabei', 'hongkou'):
        for i in range(1,3):  #可更改爬取页数，此处设置仅爬取第1、2页
            start_urls.append('https://m.lianjia.com/sh/ershoufang/' + str(j) + '/pg/' + str(i) + 'a2a3p2')
            print(start_urls)

    def parse(self, response):
        selector = Selector(response)
        infos = selector.xpath('//div[@class="item_list"]')
        for info in infos:
            item = LianjiaspiderItem()
            title_list = info.xpath('div[@class="item_main"]/text()').extract()
            # print(title_list)
            resion_list = info.xpath('div[@class="item_other text_cut"]/text()').extract()
            # print(resion_list)
            unitprice_list = info.xpath('div[3]/span[@class="unit_price"]/text()').extract()
            # print(unitprice_list)
            totalprice_list = info.xpath('div[3]/span[@class="price_total"]/em/text()').extract()
            # print(totalprice_list)
            # position_list = response.xpath('//li[@data-el="ershoufang"]/div[1]/div[3]/div/text()').extract()
            # print(position_list)

            for i,j,k,l in zip(title_list, resion_list, unitprice_list, totalprice_list):
                    item['title'] = i
                    item['resion'] = j
                    item['unitprice'] = k
                    item['totalprice'] = l
                    yield item
```

## 调试过程

get请求输出网页内容，便于抓取

```
import requests
from lxml import etree
import csv

title_list = []
title_list.append(['标题',])
num_list = list(range(1,2))
#num_list = (2,)
for num in num_list:
    page_url = 'https://m.lianjia.com/sh/ershoufang/pudong/pg' + str(num) + 'a2a3p2'
    page = requests.get(page_url).text
    print(page)
    # titles = etree.HTML(page)
    # print(titles)
```

使用pycharm的debug模式，打断点，可输出每一步运行状态，下方显示page内容，右键copy value到一个文本编辑器中，找到title、resion等字段。

在运行爬虫的过程中，会显示有些页面连接超时，继续等待即可，爬取的区域和页数越多，爬取时间较长。

本文还未实现resion的爬取，需要爬取下一页，并且对字段进行拼接，再存入到数据库内，后续更新。

## 参考文档

##### Scrapy 增加随机请求头user_agent：https://www.cnblogs.com/dahu-daqing/p/7580719.html

##### Scrapy爬取数据存入MySQL数据库：https://www.jianshu.com/p/ad7ba01b0e77

##### Scrapy爬虫——xpath与css选择器详解：https://www.jianshu.com/p/489c5d21cdc7

##### Scrapy 抓取数据提取对象小技巧：https://www.jianshu.com/p/ed9290b3cfc7