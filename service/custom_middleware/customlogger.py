import logging
from service import settings
from django.utils.deprecation import MiddlewareMixin

logger = logging.getLogger(__name__)


class LoggerMiddleware(MiddlewareMixin):

    # noinspection PyMethodMayBeStatic
    def __nonsensitive_log(self, request):
        nonsensitive_data = {'METHOD': request.META.get('REQUEST_METHOD'),
                             'PATH': request.META.get('PATH_INFO'),
                             'BALANCER': request.body,
                             'SERVICE_PORT': request.META.get('SERVER_PORT'),
                             'HTTPS': request.is_secure(),
                             'CONTENT_TYPE': request.META.get('CONTENT_TYPE'),
                             }
        logger.info(nonsensitive_data)

    def __sensitive_log(self, request):
        self.__nonsensitive_log(request)
        sensitive_data = {'POST_METHOD_DATA': request.POST,
                          'GET_METHOD_DATA': request.GET,
                          'FILES': request.FILES,
                          'MORE_DATA': 'some sensitive data here', }
        logger.debug(sensitive_data)

    def __init__(self, get_response):
        super().__init__(get_response)
        self.fuse = settings.DEBUG
        if self.fuse:
            self.log = self.__sensitive_log
        else:
            self.log = self.__nonsensitive_log

    def process_request(self, request):
        self.log(request)
