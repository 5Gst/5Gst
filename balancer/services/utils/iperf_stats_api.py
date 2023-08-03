import logging

from services.models import IperfStatistics
from services.serializers import IperfStatisticsSerializer

logger = logging.getLogger(__name__)


class IperfStatsAPI:
    def create(self, data: tuple) -> None:
        json_data = {'results': []}
        for speed, timestamp in data:
            json_data['results'].append({'speed': speed, 'timestamp': timestamp})
        serializer_for_writing = IperfStatisticsSerializer(data=json_data)
        serializer_for_writing.is_valid()
        serializer_for_writing.save()
        logger.info('Saved user iperf results in database.')

    def read(self):
        instance = IperfStatistics.objects.latest('id')
        serializer_for_reading = IperfStatisticsSerializer(instance=instance)
        logger.info('Last record from iperf statistics has been fetched')
        return serializer_for_reading.data

    def delete(self, delete_all_records=False) -> None:
        if not delete_all_records:
            IperfStatistics.objects.latest('id').delete()
            logger.info('Last record from iperf statistics has been deleted.')
        else:
            IperfStatistics.objects.all().delete()
            logger.info('All records from iperf statistics have been deleted.')
