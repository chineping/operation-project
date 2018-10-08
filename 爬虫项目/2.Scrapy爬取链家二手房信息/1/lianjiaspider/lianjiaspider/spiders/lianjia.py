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
        for i in range(1,3):
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