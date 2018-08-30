from xadmin import views
import xadmin
from .models import Host,RemoteUser,RemoteUserBindHost,UserProfile,Group,AccessLog,JenkinsList

from import_export import resources
from import_export.admin import ImportExportModelAdmin
from import_export import resources

class HostResource(resources.ModelResource):

        class Meta:
            model = Host
            import_id_fields = ('id',)
            fields = ('id', 'host_name', 'ip', 'port', 'release', 'cpu', 'memory', 'memo')

class BaseSetting(object):
    enable_themes = True
    use_bootswatch = True

class GlobalSettings(object):
    site_title = "旗计资源管理平台"
    site_footer = "旗计CMDB"
    menu_style = "accordion"

class HostAdmin(object):
    ordering = ['id', ]
    list_display = ['id','host_name','ip','port','release','cpu','memory','memo']
    search_fields = ['id','host_name','ip','port','release','cpu','memory','memo']
    list_filter = ['id','host_name','ip','port','release','cpu','memory','memo']
    # import_export_args = {'import_resource_class': HostResource, 'export_resource_class': HostResource}
    import_export_args = {'import_resource_class': HostResource,}

class RemoteUserAdmin(object):
    list_display = ['remote_user_name','password']
    search_fields = ['remote_user_name','password']
    list_filter = ['remote_user_name','password']

class RemoteUserBindHostAdmin(object):
    list_display = ['remote_user','host','enabled']
    search_fields = ['remote_user','host','enabled']
    list_filter = ['remote_user','host','enabled']

class UserProfileAdmin(object):
    list_display = ['user','user_type','remote_user_bind_hosts','groups','enabled']
    search_fields = ['user','user_type','remote_user_bind_hosts','groups','enabled']
    list_filter = ['user','user_type','remote_user_bind_hosts','groups','enabled']

class GroupAdmin(object):
    list_display = ['group_name','remote_user_bind_hosts']
    search_fields = ['group_name','remote_user_bind_hosts']
    list_filter = ['group_name','remote_user_bind_hosts']

class AccessLogAdmin(object):
    list_display = ['user','log_type','content','c_time']
    search_fields = ['user','log_type','content']
    list_filter = ['user','log_type','content','c_time']

class JenkinsListAdmin(object):
    list_display = ['job_name','build_url']
    search_fields = ['job_name','build_url']
    list_filter = ['job_name','build_url']

xadmin.site.register(views.BaseAdminView, BaseSetting)
xadmin.site.register(views.CommAdminView, GlobalSettings)

xadmin.site.register(Host,HostAdmin)
xadmin.site.register(RemoteUser,RemoteUserAdmin)
xadmin.site.register(RemoteUserBindHost,RemoteUserBindHostAdmin)
xadmin.site.register(UserProfile,UserProfileAdmin)
xadmin.site.register(Group,GroupAdmin)
xadmin.site.register(AccessLog,AccessLogAdmin)
xadmin.site.register(JenkinsList,JenkinsListAdmin)
