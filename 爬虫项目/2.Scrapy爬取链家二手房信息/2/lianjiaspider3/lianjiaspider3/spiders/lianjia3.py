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
    }
    for region in list(resions.keys()):
        for i in range(1,2):
            start_urls.append('https://sh.lianjia.com/ershoufang/' + region + '/pg' + str(i) + "a2a3p2/")

    def parse(self, response):
        li_item = response.xpath('//li[@class="clear LOGCLICKDATA"]')
        for li in li_item:
            hrefs = li.xpath('//a[@class="noresultRecommend img "]/@href').extract()
            print(hrefs)
            for href in hrefs:
                #进入详情，继续抓
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
        # print(buildyear_list)
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