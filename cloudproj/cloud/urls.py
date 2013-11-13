from django.conf.urls import patterns, url

urlpatterns = patterns('cloud.views',
    url(r'^index/$', 'index'),
    url(r'^results/$', 'results'),
    url(r'^videos/$', 'videos'),
    url(r'^images/$', 'images'),
    url(r'^my_images/$', 'myimages'),
    url(r'^my_videos/$', 'myvideos'),
    url(r'^my_videos/(\d{1})/$', 'myvideos'),
)
