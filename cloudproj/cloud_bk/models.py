from django.db import models

# Create your models here.

class cloudapp(models.Model):
    feature_name = models.CharField(max_length=200)
    feature_id  = models.CharField(max_length=200)
    feature_response  = models.CharField(max_length=200)
