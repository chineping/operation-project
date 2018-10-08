# -*- coding: utf-8 -*-

# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://doc.scrapy.org/en/latest/topics/item-pipeline.html
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
