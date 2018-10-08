# -*- coding: utf-8 -*-
import scrapy
from lianjiaspider3.items import Lianjiaspider3Item
from urllib.parse import urljoin
from scrapy.selector import Selector
from scrapy.http import Request
import requests
from lxml import etree

class Lianjia3Spider(scrapy.Spider):
    name = '2'
    allowed_domains = ['sh.lianjia.com']
    start_urls = ['https://sh.lianjia.com/ershoufang/pg1a2a3p2']
    print(start_urls)
    # for i in range(1, 2):
    #     start_urls.append('https://sh.lianjia.com/ershoufang/pg' + str(i) + 'a2a3p2')
        # print(start_urls)

    def parse(self, response):
        sel = Selector(response)
        houseUrl = sel.xpath('//li[@class="clear LOGCLICKDATA"]/a[@class="noresultRecommend img "]/@href').extract()
        # for house in houseLst:
        #     url = urljoin("https://sh.lianjia.com", house.xpath("a/@href").extract())  # 房屋详情url
        #     # print(url)
        #     yield Request(url, callback = self.parse_item)

        for a in houseUrl:
            item = Lianjiaspider3Item()
            # houseUrl = Selector(response)
            housedelId_list = a.xpath('//a/@data-lj_action_housedel_id').extract()
            print(housedelId_list)
            totalPrice_list = a.xpath('//div[@class="price "]/span[@class="total"]/text()').extract()
            print(totalPrice_list)

            for i, j in zip(housedelId_list, totalPrice_list):
                item['housedelId'] = i
                item['totalPrice'] = j
                yield item