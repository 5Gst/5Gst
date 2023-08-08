from rest_framework import serializers


class IperfArgsSerializer(serializers.Serializer):
    iperf_args = serializers.CharField(allow_blank=True, allow_null=True)


class IperfSpeedProbesSerializer(serializers.Serializer):
    probes = serializers.ListField(child=serializers.IntegerField())

