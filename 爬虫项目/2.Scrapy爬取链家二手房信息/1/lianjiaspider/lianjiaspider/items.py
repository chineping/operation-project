# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# https://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class LianjiaspiderItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    title = scrapy.Field()  # 用于存储房子标题
    resion = scrapy.Field()  # 用于存储小区名称
    position = scrapy.Field()  # 用户存储房子楼层信息
    unitprice = scrapy.Field()  # 用于存储房子的单价
    totalprice = scrapy.Field()  # 用于存储房子的总价

