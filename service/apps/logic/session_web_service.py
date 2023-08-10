import logging

from drf_yasg import openapi
from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from django.core.exceptions import BadRequest

from apps import serializers
from apps.logic import iperf_wrapper
from apps.logic.watchdog import WatchdogService
from apps.logic.watchdog_service import balancer_communication_watchdog_service, iperf_stop_watchdog_service
from service import settings

logger = logging.getLogger(__name__)


class _SessionStopWatchdogService(WatchdogService):
    def __init__(self,
                 _session_service: 'SessionWebService',
                 interval_seconds: float = settings.SESSION_MAX_IDLE_TIME_SECONDS):
        super(_SessionStopWatchdogService, self).__init__(interval_seconds=interval_seconds, timer_mode=True)
        self._session_service = _session_service

    def _on_watchdog_timeout(self):
        self._session_service.stop_session(is_called_by_timeout=True)


class SessionWebService:
    def __init__(self):
        self._is_in_session = False
        self._stop_watchdog_service = _SessionStopWatchdogService(self)

    start_session_swagger_auto_schema = swagger_auto_schema(
        operation_id='start_session',
    )

    def start_session(self) -> Response:
        balancer_communication_watchdog_service.stop()
        self._stop_watchdog_service.start()
        self._is_in_session = True
        return Response("Session started", status=status.HTTP_200_OK)

    stop_session_swagger_auto_schema = swagger_auto_schema(
        operation_id='stop_session',
    )

    def stop_session(self, is_called_by_timeout=False) -> Response:
        if not self._is_in_session:
            return Response("Not in session", status=status.HTTP_200_OK)

        iperf_wrapper.iperf.wipe_probes_container()
        self.stop_iperf()
        if not is_called_by_timeout:
            self._stop_watchdog_service.stop()
        balancer_communication_watchdog_service.start()
        self._is_in_session = False
        return Response("Session stopped", status=status.HTTP_200_OK)

    start_iperf_swagger_auto_schema = swagger_auto_schema(
        operation_id='start_iperf',
        request_body=serializers.IperfArgsSerializer,
        responses={
            200: 'Iperf started',
            500: 'Could not start iperf',
        }
    )

    def start_iperf(self, iperf_args: str) -> Response:
        if not self._is_in_session:
            return Response("Not in session", status=status.HTTP_400_BAD_REQUEST)

        self._stop_watchdog_service.reset_timer()

        self.stop_iperf()
        iperf_wrapper.iperf.iperf_parameters = iperf_args
        if iperf_wrapper.iperf.start(port_iperf=settings.IPERF_PORT):
            iperf_stop_watchdog_service.start()
            logger.info(f"iPerf parameters received from client: {iperf_wrapper.iperf.iperf_parameters}")
            return Response(data=f"iPerf parameters received from client: {iperf_wrapper.iperf.iperf_parameters}",
                            status=status.HTTP_200_OK)
        else:
            logger.error("Failed to start iperf")
            return Response("Failed to start iperf", status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    stop_iperf_swagger_auto_schema = swagger_auto_schema(
        operation_id='stop_iperf',
    )

    def stop_iperf(self) -> Response:
        if not self._is_in_session:
            return Response("Not in session", status=status.HTTP_400_BAD_REQUEST)

        status_code = iperf_wrapper.iperf.stop()
        if status_code != 0:
            logger.error(f"Iperf was stopped with status code {status_code}")

        self._stop_watchdog_service.reset_timer()
        iperf_stop_watchdog_service.stop()
        return Response("Iperf was successfully stopped", status=status.HTTP_200_OK)

    get_iperf_speed_results_swagger_auto_schema = swagger_auto_schema(
        operation_description='Returns iperf speed probes',
        operation_id='get_iperf_speed_probes',
        manual_parameters=[
            openapi.Parameter(
                'from_probe',
                openapi.IN_QUERY,
                type=openapi.TYPE_INTEGER,
            ),
        ],
        responses={
            200: openapi.Response('Speed results are successfully returned', serializers.IperfMeasurementSerializer),

        },
    )

    def get_iperf_speed_probes(self, start_index: int):
        if iperf_wrapper.iperf.iperf_active_parsed_speed_container is None:
            raise BadRequest("Requested Iperf speed probes on closed / non-existing session")

        model = iperf_wrapper.iperf.iperf_active_parsed_speed_container.get_from_probe(start_index)
        serialized_model = serializers.IperfMeasurementSerializer(model)

        return Response(data=serialized_model.data, status=status.HTTP_200_OK)


session_web_service = SessionWebService()
