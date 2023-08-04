import logging
from rest_framework.request import Request
from rest_framework.renderers import JSONRenderer
from rest_framework.views import APIView
from django.http import HttpResponse, HttpResponseBadRequest
from drf_yasg.utils import swagger_auto_schema
from apps.logic.session_web_service import session_web_service, SessionWebService
from apps.logic import iperf_wrapper
from apps import serializers

logger = logging.getLogger(__name__)


class StartSessionView(APIView):
    @SessionWebService.start_session_swagger_auto_schema
    def post(self, request: Request):
        return session_web_service.start_session()


class StopSessionView(APIView):
    @SessionWebService.stop_session_swagger_auto_schema
    def post(self, request: Request):
        return session_web_service.stop_session()


class StartIperfView(APIView):
    @SessionWebService.start_iperf_swagger_auto_schema
    def post(self, request: Request):
        serializer = serializers.IperfArgsSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        iperf_args = serializer.validated_data['iperf_args']
        iperf_args = iperf_args if iperf_args is not None else ''
        return session_web_service.start_iperf(iperf_args)


class StopIperfView(APIView):
    @SessionWebService.stop_iperf_swagger_auto_schema
    def post(self, request: Request):
        return session_web_service.stop_iperf()


class IperfSpeedResultsAPIView(APIView):
    @swagger_auto_schema(
        operation_description='Client requests server for real speed results',
        operation_id='send_results',
        request_body=serializers.IperfSpeedResultsSerializer,
        responses={
            200: 'results sent',
            400: 'results do no exist',
            500: 'unexpected server error',

        },
        security=[],
    )
    def get(self, request: Request):
        start_index = request.GET.get('fromFrame')
        if iperf_wrapper.iperf_active_parsed_speed_container is None or start_index is None:
            return HttpResponseBadRequest()
        model = iperf_wrapper.iperf_active_parsed_speed_container.get_from_probe(int(start_index))
        serialized_model = serializers.IperfSpeedResultsSerializer(model)
        response = JSONRenderer().render(serialized_model.data)

        return HttpResponse(response)
