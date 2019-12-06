from django.shortcuts import render
from django.http import  HttpResponse  # Quick tests of the new pages

# Create your views here.

def index(request):
    return render(request,
                  'georunner/main/main.html',
                  {})

def about(request):
    return render(request,
                  'georunner/about/about.html',
                  {})

def map(request):
    return render(request,
                  'georunner/map_page/map_page.html',
                  {})

