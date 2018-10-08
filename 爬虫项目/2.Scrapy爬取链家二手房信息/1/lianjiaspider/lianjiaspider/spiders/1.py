# import requests
# from lxml import etree
# import csv
#
# title_list = []
# title_list.append(['标题',])
# num_list = list(range(1,2))
# #num_list = (2,)
# for num in num_list:
#     page_url = 'https://m.lianjia.com/sh/ershoufang/pudong/pg' + str(num) + 'a2a3p2'
#     page = requests.get(page_url).text
#     print(page)
#     # titles = etree.HTML(page)
#     # print(titles)