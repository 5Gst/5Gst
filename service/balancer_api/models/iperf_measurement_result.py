# coding: utf-8

"""
    Balancer API

    Speedtest load balancer  # noqa: E501

    OpenAPI spec version: 0.1.0
    Contact: dev@5gst.ru
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


import pprint
import re  # noqa: F401

import six

from balancer_api.configuration import Configuration


class IperfMeasurementResult(object):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    """

    """
    Attributes:
      swagger_types (dict): The key is attribute name
                            and the value is attribute type.
      attribute_map (dict): The key is attribute name
                            and the value is json key in definition.
    """
    swagger_types = {
        'start_timestamp': 'datetime',
        'probes': 'list[IperfProbeResults]'
    }

    attribute_map = {
        'start_timestamp': 'start_timestamp',
        'probes': 'probes'
    }

    def __init__(self, start_timestamp=None, probes=None, _configuration=None):  # noqa: E501
        """IperfMeasurementResult - a model defined in Swagger"""  # noqa: E501
        if _configuration is None:
            _configuration = Configuration()
        self._configuration = _configuration

        self._start_timestamp = None
        self._probes = None
        self.discriminator = None

        self.start_timestamp = start_timestamp
        self.probes = probes

    @property
    def start_timestamp(self):
        """Gets the start_timestamp of this IperfMeasurementResult.  # noqa: E501


        :return: The start_timestamp of this IperfMeasurementResult.  # noqa: E501
        :rtype: datetime
        """
        return self._start_timestamp

    @start_timestamp.setter
    def start_timestamp(self, start_timestamp):
        """Sets the start_timestamp of this IperfMeasurementResult.


        :param start_timestamp: The start_timestamp of this IperfMeasurementResult.  # noqa: E501
        :type: datetime
        """
        if self._configuration.client_side_validation and start_timestamp is None:
            raise ValueError("Invalid value for `start_timestamp`, must not be `None`")  # noqa: E501

        self._start_timestamp = start_timestamp

    @property
    def probes(self):
        """Gets the probes of this IperfMeasurementResult.  # noqa: E501


        :return: The probes of this IperfMeasurementResult.  # noqa: E501
        :rtype: list[IperfProbeResults]
        """
        return self._probes

    @probes.setter
    def probes(self, probes):
        """Sets the probes of this IperfMeasurementResult.


        :param probes: The probes of this IperfMeasurementResult.  # noqa: E501
        :type: list[IperfProbeResults]
        """
        if self._configuration.client_side_validation and probes is None:
            raise ValueError("Invalid value for `probes`, must not be `None`")  # noqa: E501

        self._probes = probes

    def to_dict(self):
        """Returns the model properties as a dict"""
        result = {}

        for attr, _ in six.iteritems(self.swagger_types):
            value = getattr(self, attr)
            if isinstance(value, list):
                result[attr] = list(map(
                    lambda x: x.to_dict() if hasattr(x, "to_dict") else x,
                    value
                ))
            elif hasattr(value, "to_dict"):
                result[attr] = value.to_dict()
            elif isinstance(value, dict):
                result[attr] = dict(map(
                    lambda item: (item[0], item[1].to_dict())
                    if hasattr(item[1], "to_dict") else item,
                    value.items()
                ))
            else:
                result[attr] = value
        if issubclass(IperfMeasurementResult, dict):
            for key, value in self.items():
                result[key] = value

        return result

    def to_str(self):
        """Returns the string representation of the model"""
        return pprint.pformat(self.to_dict())

    def __repr__(self):
        """For `print` and `pprint`"""
        return self.to_str()

    def __eq__(self, other):
        """Returns true if both objects are equal"""
        if not isinstance(other, IperfMeasurementResult):
            return False

        return self.to_dict() == other.to_dict()

    def __ne__(self, other):
        """Returns true if both objects are not equal"""
        if not isinstance(other, IperfMeasurementResult):
            return True

        return self.to_dict() != other.to_dict()
