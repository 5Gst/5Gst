import logging
from balancer import settings

logger = logging.getLogger(__name__)


class FiveGstLoggerMiddleware:
    # noinspection PyMethodMayBeStatic
    # This method's purpose is to get client ip even if nginx or apache reverse proxy will be configured in future
    # It's only special usage is here. No need to call it from anywhere else.
    def __uncover_sender_ip(self, request):
        forwarded = request.META.get('HTTP_X_FORWARDED_FOR')
        if forwarded:
            return forwarded
        return request.META.get('REMOTE_ADDR')

    # noinspection PyMethodMayBeStatic
    # Logging method choice varies on DEBUG value in settings.
    # So it should always be called with class instance where DEBUG status is checked due initialization.
    def __nonsensitive_log(self, request):
        nonsensitive_data = {'METHOD': request.META.get('REQUEST_METHOD'),
                             'FROM_PATH': request.META.get('PATH_INFO'),
                             'PORT': request.META.get('SERVER_PORT'),
                             'HTTPS': request.is_secure(),
                             'SENDER_ADDR': self.__uncover_sender_ip(request),
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
