from django.db import models

# Create your models here.

class cloudApp(models.Model):
    feature_name = models.CharField(max_length=200)
    feature_id  = models.CharField(max_length=200)
    feature_response  = models.CharField(max_length=200)

class middlewareInfo(models.Model):
    middleware_name = models.CharField(max_length=200)
    middleware_id = models.CharField(max_length=200)
    middleware_addr = models.IPAddressField(max_length=200)
    middleware_location = models.FloatField(max_length=200)

class clientInfo(models.Model):
    client_name = models.CharField(max_length=200)
    client_id = models.CharField(max_length=200)
    client_addr = models.IPAddressField(max_length=200)
    client_location = models.FloatField(max_length=200)
    client_file_id = models.CharField(max_length=200)
    client_battery_info = models.CharField(max_length=200)
