import logging

from django.core.exceptions import BadRequest
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi
from rest_framework import status
from rest_framework.serializers import ValidationError
from rest_framework.response import Response

from services.models import IperfMeasurementResult
from services.serializers import IperfMeasurementResultSerializer

logger = logging.getLogger(__name__)


class IperfMeasurementHistoryAPI:
    create_swagger_auto_schema = swagger_auto_schema(
        operation_description='Save measurement results',
        operation_id='create',
        request_body=IperfMeasurementResultSerializer,
        responses={
            201: openapi.Response('Saved user measurement results in database'),
            400: openapi.Response('Could not save measurement results in database'),
        }
    )

    def create(self, data) -> Response:
        try:
            serializer_for_writing = IperfMeasurementResultSerializer(data=data)
            serializer_for_writing.is_valid(raise_exception=True)
            serializer_for_writing.save()
            logger.info('Saved measurement results in database')
            return Response(status=status.HTTP_201_CREATED)
        except ValidationError as exc:
            logger.error(f"Error {exc.status_code} happened: {exc.detail}", exc_info=exc)
            raise BadRequest('Could not save measurement results in database')

    read_swagger_auto_schema = swagger_auto_schema(
        operation_description='Return measurement results',
        operation_id='read',
        request_body=IperfMeasurementResultSerializer,
        manual_parameters=[
            openapi.Parameter(
                'measurement_id',
                openapi.IN_QUERY,
                type=openapi.TYPE_INTEGER,
            ),
        ],
        responses={
            200: openapi.Response('Measurement result has been fetched',
                                  IperfMeasurementResultSerializer),
            400: openapi.Response('Invalid measurement id'),
        }
    )

    def read(self, measurement_id: str) -> Response:
        err = False

        if measurement_id is None:
            err = True
        elif isinstance(measurement_id, str):
            if not measurement_id.isdecimal():
                err = True
            else:
                measurement_id = int(measurement_id)
                if measurement_id < 0 or measurement_id > IperfMeasurementResult.objects.latest('id').id:
                    err = True
        elif isinstance(measurement_id, int):
            if measurement_id < 0 or measurement_id > IperfMeasurementResult.objects.latest('id').id:
                err = True
        else:
            err = True

        if err:
            logger.error('Invalid measurement id')
            raise BadRequest('Invalid measurement id')

        measurement_data = IperfMeasurementResult.objects.get(id=measurement_id)
        serializer_for_reading = IperfMeasurementResultSerializer(instance=measurement_data)
        logger.info('Measurement result has been fetched')
        return Response(serializer_for_reading.data, status=status.HTTP_200_OK)


user_measurement_history = IperfMeasurementHistoryAPI()
