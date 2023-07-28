import logging
from service import settings

logger = logging.getLogger(__name__)


class FiveGstLoggerMiddleware:
    # noinspection PyMethodMayBeStatic
    # Logging method choice varies on DEBUG value in settings.
    # So it should always be called with class instance where DEBUG status is checked due initialization.
    def __nonsensitive_log(self, request):
        nonsensitive_data = {'METHOD': request.META.get('REQUEST_METHOD'),
                             'FROM_PATH': request.META.get('PATH_INFO'),
                             'PORT': request.META.get('SERVER_PORT'),
                             'HTTPS': request.is_secure(),
                             'CONTENT_TYPE': request.META.get('CONTENT_TYPE'),
                             }
        logger.info(f"*> Request data: {nonsensitive_data}")

    def __sensitive_log(self, request):
        self.__nonsensitive_log(request)
        sensitive_data = {'POST_METHOD_DATA': request.POST,
                          'GET_METHOD_DATA': request.GET,
                          'BODY': request.body,
                          'FILES': request.FILES,
                          }
        logger.debug(f"     Sensitive: {sensitive_data}")

    def process_request(self, request):
        self.log(request)

    def __init__(self, get_response):
        self.get_response = get_response
        if settings.DEBUG:
            self.log = self.__sensitive_log
        else:
            self.log = self.__nonsensitive_log

    def __call__(self, request):
        self.process_request(request)
        # Required for proper Django architecture work
        response = self.get_response(request)
        return response
