from django.shortcuts import render
from lianjia3 import forms

from lianjia3.models import lianjia3
from django.shortcuts import HttpResponseRedirect,Http404,HttpResponse,render_to_response

# Create your views here.
def table(request):
    # table_form = forms.SignupForm()
    list = lianjia3.objects.all()
    # housedelId_List = lianjia3.objects.values("housedelId")

    # return render(request,'table.html',{'form':table_form})
    return render_to_response("table.html", locals())

