# -*- coding: utf-8 -*-
import scrapy
from lianjiaspider3.items import Lianjiaspider3Item
from urllib.parse import urljoin
from scrapy.http import Request

class Lianjia3Spider(scrapy.Spider):
    name = '1'
    allowed_domains = ['sh.lianjia.com']

    def parse(self, response):
        start_urls = []
        for i in range(1, 2):
            start_urls = start_urls.append('https://sh.lianjia.com/ershoufang/pg' + str(i) + 'a2a3p2')
            houseLst = response.xpath("//li[@class='pictext']")
            for house in houseLst:
                url = urljoin("https://sh.lianjia.com", house.xpath("a/@href").extract_first())  # 房屋详情url
                yield Request(url, callback=self.parse_house_info)
            yield Request(start_urls, callback=self.parse)

    def parse_house_info(self, response):
        item = Lianjiaspider3Item()
        houseUrl = response.url
        housedelId_list = houseUrl.xpath('//a/@data-lj_action_housedel_id').extract()
        print(housedelId_list)
        totalPrice_list = houseUrl.xpath('//div[@class="price "]/span[@class="total"]/text()').extract()
        print(totalPrice_list)

        for i, j in zip(housedelId_list, totalPrice_list):
            item['housedelId'] = i
            item['totalPrice'] = j
            yield item