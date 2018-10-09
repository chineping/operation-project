from django.shortcuts import render
from lianjia3 import forms

from lianjia3.models import lianjia3
from django.shortcuts import HttpResponseRedirect,Http404,HttpResponse,render_to_response
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger

# Create your views here.
def table(request):
    # table_form = forms.SignupForm()
    limit = 5  # 每页显示的记录数
    lists = lianjia3.objects.all()
    paginator = Paginator(lists, limit)  # 实例化一个分页对象

    page = request.GET.get('page')  # 获取页码
    try:
        lists = paginator.page(page)  # 获取某页对应的记录
    except PageNotAnInteger:  # 如果页码不是个整数
        lists = paginator.page(1)  # 取第一页的记录
    except EmptyPage:  # 如果页码太大，没有相应的记录
        lists = paginator.page(paginator.num_pages)  # 取最后一页的记录

    return render_to_response('table.html', {'lists': lists})
    # return render(request,'table.html',{'form':table_form})
    # return render_to_response("table.html", locals())

