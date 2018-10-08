# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# https://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class Lianjiaspider3Item(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    housedelId = scrapy.Field()        #id
    title = scrapy.Field()             #标题
    totalPrice = scrapy.Field()        #总售价
    houseType = scrapy.Field()         #户型，几室几厅
    constructionArea = scrapy.Field()  #建筑面积
    unitPrice = scrapy.Field()         #单价
    listingTime = scrapy.Field()       #挂牌时间
    toward = scrapy.Field()            #朝向
    floor = scrapy.Field()             #所在楼层
    buildingtype = scrapy.Field()      #建筑类型
    hasElevator = scrapy.Field()       #是否电梯
    decorationType = scrapy.Field()    #装修类型
    buildyear = scrapy.Field()         #建设年代
    useful = scrapy.Field()            #用途
    lastSell = scrapy.Field()          #上次交易
    last30DayLead = scrapy.Field()     #近30天带看次数
    attention = scrapy.Field()         #关注人数
    yearOfRight = scrapy.Field()       #产权年限
