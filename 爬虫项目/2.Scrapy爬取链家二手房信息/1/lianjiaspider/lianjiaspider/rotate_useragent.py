# -*- coding: utf-8 -*-

import random
from scrapy.contrib.downloadermiddleware.useragent import UserAgentMiddleware
from lianjiaspider.settings import USER_AGENT_LIST
from scrapy import log

class RandomUserAgentMiddleware(object):
    def process_request(self, request, spider):
        ua  = random.choice(USER_AGENT_LIST)
        if ua:
            request.headers.setdefault('User-Agent', ua)



