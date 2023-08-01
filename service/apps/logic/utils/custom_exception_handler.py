# custom handler
import logging

from rest_framework.views import exception_handler

logger = logging.getLogger(__name__)


def custom_exception_handler(exc, context):
    # Call REST framework's default exception handler first,
    # to get the standard error response.

    handlers = {
        'BadRequest': _handler_badrequest_error,
    }

    response = exception_handler(exc, context)

    # Now add the HTTP status code to the response.
    if response is not None:
        response.data['status_code'] = response.status_code

    exception_class = exc.__class__.__name__

    if exception_class in handlers:
        return handlers[exception_class](exc, context, response)
    return response


def _handler_badrequest_error(exc, context, response):
    response.data = {
        'status_code': response.status_code,
        'status_text': response.status_text,
        'error': 'The session may be closed because the timer has expired.',
    }
