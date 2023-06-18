from django.urls import path, include

urlpatterns = [
    path('5gst/iperf_load_balancer/0.1.0/', include('balancer.urls_v_0_1_0')),
]
