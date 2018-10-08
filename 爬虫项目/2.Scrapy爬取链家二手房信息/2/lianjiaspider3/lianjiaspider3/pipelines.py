# -*- coding: utf-8 -*-

# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://doc.scrapy.org/en/latest/topics/item-pipeline.html
import pymysql
pymysql.install_as_MySQLdb()

class Lianjiaspider3Pipeline(object):
    def open_spider(self, spider):
        self.con = pymysql.connect('localhost', 'root', '', 'lianjia3')
        self.cursor = self.con.cursor()

    def process_item(self, item, spider):
        insert_sql = "INSERT INTO lianjia3(housedelId, totalPrice, title, houseType, constructionArea, unitPrice, listingTime, toward, floor, buildingtype, hasElevator, decorationType, buildyear, useful, yearOfRight, lastSell, last30DayLead, attention) VALUES ('{}', '{}', '{}', '{}','{}', '{}', '{}', '{}','{}', '{}', '{}', '{}','{}', '{}', '{}', '{}','{}', '{}')".format(item['housedelId'], item['totalPrice'], item['title'], item['houseType'], item['constructionArea'], item['unitPrice'], item['listingTime'], item['toward'], item['floor'], item['buildingtype'], item['hasElevator'], item['decorationType'], item['buildyear'], item['useful'], item['yearOfRight'], item['lastSell'], item['last30DayLead'], item['attention'])
        # insert_sql = "INSERT INTO lianjia3(housedelId, totalPrice) VALUES ('{}', '{}')".format(item['housedelId'], item['totalPrice'])
        self.cursor.execute(insert_sql)  # 执行sql语句
        self.con.commit()  # 提交到数据库，insert和updata语句必须执行这句
        return item

    def spider_close(self, spider):
        self.con.close()