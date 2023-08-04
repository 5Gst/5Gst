import logging

from rest_framework.serializers import ValidationError
from rest_framework.utils.serializer_helpers import ReturnDict

from services.models import IperfStatistics
from services.serializers import IperfStatisticsSerializer

logger = logging.getLogger(__name__)


class IperfStatsAPI:
    def create(self, json_data: dict[str: list[dict[str: float], ...]]) -> None:
        json_data['start_timestamp'] = json_data['results'][0]['timestamp']
        try:
            serializer_for_writing = IperfStatisticsSerializer(data=json_data)
            serializer_for_writing.is_valid(raise_exception=True)
            serializer_for_writing.save()
            logger.info('Saved user iperf results in database.')
        except ValidationError as exc:
            logger.error(f"Error {exc.status_code} happened: {exc.detail}", exc_info=exc)

    def read(self, id: int) -> ReturnDict:
        instance = IperfStatistics.objects.get(id=id)
        serializer_for_reading = IperfStatisticsSerializer(instance=instance)
        logger.info('Last record from iperf statistics has been fetched')
        return serializer_for_reading.data
