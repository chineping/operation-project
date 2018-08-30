# 自定义配置GRAFANA仪表盘

网上关于ZABBIX数据源的Grafana仪表盘配置比较多，但是基于Prometheus数据源的自定义配置比较少，并且模板也比较少。

主要参考：https://www.cnblogs.com/netonline/p/8289411.html

## 一、导入模板

```
导入dashboard
从grafana官网下载相关dashboaed到本地，如：https://grafana.com/dashboards/22
Grafana首页-->左上角图标-->Dashboard-->import
```

主要导入405或22，适用于Prometheus数据源。

## 二、自定义模板

把鼠标点在每个图片的标题栏Title--> 左键--> Edit 

###  2.1 编辑General --> 编辑Title --> Repeat选择Disabled

Repeat：原模板每个图片的格式

需要自定义饼状图需要安装插件

https://grafana.com/plugins/grafana-piechart-panel/installation

```
grafana-cli plugins install grafana-piechart-panel
systemctl restart grafana-server.service
```

![](https://i.imgur.com/r4ndIPA.jpg)

### 2.2 编辑Metrics --> 选择数据源为prometheus --> 建立查询语句

数据格式及来源node：http://10.90.4.5:9100/metrics，需要对数据进行处理，最便捷的方法是从其他的模本内copy过来相应的query语句![](https://i.imgur.com/ytsdJ6N.jpg)

### 2.3 编辑Options--> 修改Position：Right side

| 左侧General参数含义        | Legend              |
| -------------------- | ------------------- |
| Type：pie 饼状图         | Position：注释位置       |
| Unit：可以在右侧标注添加时间     | Show Percentage：百分比 |
| Divider width：外框线条粗细 |                     |

![](https://i.imgur.com/IoxZcME.jpg)

所有编辑后一定要点保存！

### 2.4 拖拽图片右下角来改变大小

等到鼠标变成双箭头

### 2.5 移动图片Title移动图片位置

等鼠标变成花箭头

### 2.6 对自定义模板设置

右上角的setting按钮可以更改模板名等

参考文档

github：https://github.com/yangcvo/Grafana

安装插件：https://kiswo.com/article/1021

模板：https://grafana.com/dashboards/1860