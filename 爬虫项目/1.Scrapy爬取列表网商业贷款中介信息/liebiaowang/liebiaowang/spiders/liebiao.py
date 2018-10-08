# -*- coding: utf-8 -*-
import scrapy
import requests
from scrapy.selector import Selector
from lxml import etree
from ..items import LiebiaowangItem

class LiebiaoSpider(scrapy.Spider):
    name = 'liebiao'
    allowed_domains = ['shanghai.liebiao.com']
    start_urls = []
    page = []
    for i in range(1, 2):
        # start_urls.append('http://shanghai.liebiao.com/jinrongdaikuan-gongsi/index' + str(i) + '.html')
        start_urls.append('http://shanghai.liebiao.com/jinrongdaikuan/index' + str(i) + '.html')

    def parse(self, response):
        item = LiebiaowangItem()
        title_list = response.xpath("//div[@class='post-title-wrap']/h2/a/text()").extract()
        # print(title_list)
        number_list = response.xpath("//div[@class='contact-info']/p[@class='contact-phone']/text()").extract()
        # print(number_list)
        #
        for i, j in zip(title_list, number_list):
            item['title'] = i
            item['number'] = j
            yield item

