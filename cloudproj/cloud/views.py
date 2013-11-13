from django.http import Http404
from django.shortcuts import render_to_response, get_object_or_404, redirect
from django.http import HttpResponseRedirect, HttpResponse
from django.core.urlresolvers import reverse
from django.template import RequestContext
from cloud.models import cloudApp, middlewareInfo, clientInfo
from wsgiref.util import FileWrapper
import os

LOCATION_THRESHOLD = 50
BATTERY_THRESHOLD = 70
vidname_to_file_dict = {"1":"dev.mp4", "3":"buf.mp4", "6":"sus.mp4", "8":"mcd.mp4", "9":"thai.mp4"}
vid_dir = "/Users/karthikchandrasekar/Desktop/Studies/Cloud/mytemplates/admin/"

def index(request):
    features_list = cloudApp.objects.all()
    return render_to_response('cloud/index.html', {'all_features_list': features_list}, context_instance=RequestContext(request)) 


def results(request):
    features_list = cloudApp.objects.all()
    requested_id = request.POST['cloud']
    for feature in features_list:
        if feature.feature_id == requested_id:
            result = feature
    #middleware_addr = select_middleware(requset)
    middleware_addr = ''
    middleware_addr = 'http://192.168.0.16:8000/cloud/index/'
    peer_addr = ''

    #1-Transfer through middleware
    if middleware_addr:
        return redirect(middleware_addr)

    #2-Transfer through peer
    if  peer_addr:
        peer_addr = select_peer(request)
        return redirect(peer_addr)

    #3-Transfer through centralized server
    else:  
        return render_to_response('cloud/results.html', {'final_feature': result}, context_instance=RequestContext(request)) 

def select_middleware(request):
    
    # MIDDLE WARE SELECTION ALGORITHM
  
    min_loc_diff = 0 
    middleware_list = middlewareInfo.objects.all()
    for middleware in middleware_list:
        loc_diff = locationdiff(middleware, request) 
        if loc_diff < min_loc_diff:
            closest_middleware = middleware
    return closest_middleware.middleware_addr


def select_peer(request):

    #PEER SELECTION ALGORITHM
    
    neighbour_set = neighbour_selection(request)
    nodes_with_files_set = file_detection(request)
    peer_candidates_set = neighbour_set.intersection(nodes_with_files_set)
    return peer_candidates_set[:1].client_addr


def neighbour_selection(request):
    
    #NEIGHBOUR SELECTION ALGORITHM

    neighbour_set = set()
    peer_set = clientInfo.objects.all()
    for peer in peer_set:
        loc_diff = locationdiff(peer, request)
        if loc_diff < LOCATION_THRESHOLD:
            neighbour_set.add(peer)
    return neighbour_set


def file_detection(request):

    #FILE DETECTION ALGORITHM

    nodes_with_file_set = set()
    client_file_id = request.POST['file_id']
    peer_set = clientInfo.objects.all()
    for peer in peer_set:
        if peer.client_file_id == client_file_id:
            nodes_with_file_set.add(peer)
    selected_nodes_set = resource_checking(nodes_with_file_set)
    return selected_nodes_set

def resource_checking(nodes_with_file_set):

    #RESOURCE CHECKING
    selected_nodes_set = set()
    for client in nodes_with_file_set:
        if client.client_battery_info > BATTERY_THRESHOLD:
            selected_nodes_set.add(client)
    return selected_nodes_set


def locationdiff(middleware, request):
    return 0

def videos(request):
    #Return a sample vid
    file = FileWrapper(open('/Users/karthikchandrasekar/Desktop/Studies/Cloud/mytemplates/admin/my_file.mp3', 'rb'))
    response = HttpResponse(file, content_type='video/mp3')
    response['Content-Disposition'] = 'attachment; filename=my_file.mp3'
    return response

def myaudios(request):
    video_data = open("/Users/karthikchandrasekar/Desktop/Studies/Cloud/mytemplates/admin/my_file.mp3", "rb").read()
    return HttpResponse(video_data, mimetype="audio/mpeg3")


def myvideos(request):
    video_data = open("/Users/karthikchandrasekar/Desktop/Studies/Cloud/mytemplates/admin/my_file.mp4", "rb").read()
    return HttpResponse(video_data, mimetype="video/mp4")

def myvideos(request, vid_num):
    file_name = vidname_to_file_dict.get(vid_num)
    if not file_name:
        file_name = "thai.mp4"
    video_data = open((os.path.join(vid_dir, file_name)), "rb").read()
    return HttpResponse(video_data, mimetype="video/mp4")

def myimages(request):
    image_data = open("/Users/karthikchandrasekar/Desktop/Studies/Cloud/mytemplates/admin/my_file.JPG", "rb").read()
    return HttpResponse(image_data, mimetype="image/png")

def images(request):
    file = FileWrapper(open('/Users/karthikchandrasekar/Desktop/Studies/Cloud/mytemplates/admin/my_file.JPG', 'rb'))
    response = HttpResponse(file, mimetype='image/jpg') 
    response['Content-Disposition'] = 'attachment; filename=my_file.JPG'
    return response
