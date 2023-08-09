import logging

from django.http import HttpResponseBadRequest
from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.serializers import ValidationError
from rest_framework.response import Response

from services.models import IperfStatistics
from services.serializers import IperfStatisticsSerializer

logger = logging.getLogger(__name__)


class IperfMeasurementHistoryAPI:
    create_swagger_auto_schema = swagger_auto_schema(
        operation_id='create',
        responses={
            201: 'Saved user iperf results in database.',
            400: 'Could not save user iperf results in database.'
        }
    )

    def create(self, data: dict[str: list[dict[str: float], ...]]) -> Response:
        try:
            serializer_for_writing = IperfStatisticsSerializer(data=data)
            serializer_for_writing.is_valid(raise_exception=True)
            serializer_for_writing.save()
            logger.info('Saved user iperf results in database.')
            return Response(status=status.HTTP_201_CREATED)
        except ValidationError as exc:
            logger.error(f"Error {exc.status_code} happened: {exc.detail}", exc_info=exc)
            raise HttpResponseBadRequest

    read_swagger_auto_schema = swagger_auto_schema(
        operation_id='read',
        responses={
            200: 'Last record from iperf statistics has been fetched.',
            400: 'User ID not received to get data.'
        }
    )

    def read(self, id: int) -> Response:
        if id is None:
            logger.error('User ID not received to get data.')
            raise HttpResponseBadRequest
        instance = IperfStatistics.objects.get(id=id)
        serializer_for_reading = IperfStatisticsSerializer(instance=instance)
        logger.info('Last record from iperf statistics has been fetched.')
        return Response(serializer_for_reading.data, status=status.HTTP_200_OK)


user_measurement_history = IperfMeasurementHistoryAPI()
