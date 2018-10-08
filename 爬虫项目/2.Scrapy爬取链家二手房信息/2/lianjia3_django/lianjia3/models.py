from django.db import models

# Create your models here.
class lianjia3(models.Model):
    housedelId = models.IntegerField(verbose_name="房屋id")
    totalPrice = models.CharField(max_length=255, verbose_name="房屋总价")
    title = models.CharField(max_length=255, verbose_name="房屋标题")
    houseType = models.CharField(max_length=255, verbose_name="房屋户型")
    constructionArea = models.CharField(max_length=255, verbose_name="建筑面积")
    unitPrice = models.IntegerField(verbose_name="房屋单价")
    listingTime = models.DateTimeField(verbose_name="挂牌时间")
    toward = models.CharField(max_length=255, verbose_name="房屋朝向")
    floor = models.CharField(max_length=255, verbose_name="所在楼层")
    buildingtype = models.CharField(max_length=255, verbose_name="建筑类型")
    hasElevator = models.CharField(max_length=255, verbose_name="是否电梯")
    decorationType = models.CharField(max_length=255, verbose_name="装修类型")
    yearOfRight = models.CharField(max_length=255, verbose_name="产权年限")
    buildyear = models.CharField(max_length=255, verbose_name="建设年代")
    useful = models.CharField(max_length=255, verbose_name="房屋用途")
    lastSell = models.CharField(max_length=255, verbose_name="上次交易")
    last30DayLead = models.CharField(max_length=255, verbose_name="30天带看")
    attention = models.CharField(max_length=255, verbose_name="关注人数")

    class Meta:
        verbose_name = '二手房信息'
        verbose_name_plural = verbose_name

    def __str__(self):
        return self.title
