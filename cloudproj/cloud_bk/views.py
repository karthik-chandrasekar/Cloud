from django.http import Http404
from django.shortcuts import render_to_response, get_object_or_404
from django.http import HttpResponseRedirect, HttpResponse
from django.core.urlresolvers import reverse
from django.template import RequestContext
from cloud.models import cloudapp
from wsgiref.util import FileWrapper

def index(request):
    features_list = cloudapp.objects.all()
    return render_to_response('cloud/index.html', {'all_features_list': features_list}, context_instance=RequestContext(request)) 


def results(request):
    features_list = cloudapp.objects.all()
    requested_id = request.POST['cloud']
    for feature in features_list:
        if feature.feature_id == requested_id:
            result = feature
    return render_to_response('cloud/results.html', {'final_feature': result}, context_instance=RequestContext(request)) 


def videos(request):
    #Return a sample vid
    file = FileWrapper(open('/Users/karthikchandrasekar/Desktop/Studies/Cloud/mytemplates/admin/my_file.mp3', 'rb'))
    response = HttpResponse(file, content_type='video/mp3')
    response['Content-Disposition'] = 'attachment; filename=my_file.mp3'
    return response

def images(request):
    file = FileWrapper(open('/Users/karthikchandrasekar/Desktop/Studies/Cloud/mytemplates/admin/my_file.JPG', 'rb'))
    response = HttpResponse(file, mimetype='image/jpg') 
    response['Content-Disposition'] = 'attachment; filename=my_file.JPG'
    return response
