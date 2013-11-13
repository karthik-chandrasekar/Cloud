from django.conf.urls import patterns, url

urlpatterns = patterns('cloud.views',
    url(r'^index/$', 'index'),
    url(r'^results/$', 'results'),
    url(r'^videos/$', 'videos'),
    url(r'^images/$', 'images'),
)
