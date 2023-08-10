from rest_framework import serializers


class IperfArgsSerializer(serializers.Serializer):
    iperf_args = serializers.CharField(allow_blank=True, allow_null=True)


class IperfSpeedProbeSerializer(serializers.Serializer):
    bits_per_second = serializers.IntegerField(allow_null=True)


class IperfDownloadMeasurementSerializer(serializers.Serializer):
    probes = IperfSpeedProbeSerializer(many=True)
