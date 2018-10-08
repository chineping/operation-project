# -*- coding:utf-8 -*-
__author__ = 'zhangxiying'
__date__ = '2018/9/29 13:13'

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

