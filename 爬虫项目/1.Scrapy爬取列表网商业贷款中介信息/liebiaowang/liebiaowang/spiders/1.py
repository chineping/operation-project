# #!/usr/bin/env python
# # -*- coding: utf-8 -*-
import requests
from lxml import etree
import csv
#
#
# # phone_list = []
# # phone_list.append(['电话',])
num_list = list(range(1,2))
#num_list = (2,)
for num in num_list:
    page_url = 'http://shanghai.liebiao.com/jinrongdaikuan/index'   + str(num) + '.html'
    page = requests.get(page_url).text
    print(page)
#     phones = etree.HTML(page)
#     #print(phones)
#     #phone = phones.xpath('//button[@class="btn-contact"]/@data-phone')
#     phone = phones.xpath('//div[@class="contact-info"]/p[@class="contact-phone"]/text()')
#     #print(len(phone))
#     for i in range(len(phone)):
# #        print(i)
#         phone_list.append([phone[i]])
#
#
# with open('phone.csv', 'w', newline='') as file:
#     writer = csv.writer(file)
#     writer.writerows(phone_list)