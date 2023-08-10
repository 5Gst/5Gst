import logging

from rest_framework.exceptions import APIException
from rest_framework.views import exception_handler
from rest_framework.response import Response

logger = logging.getLogger(__name__)


def handle_exception(exc, context):
    if isinstance(exc, APIException):
        if 400 <= exc.status_code <= 499:
            logger.debug(f"Error {exc.status_code} happened", exc_info=exc)
        elif 500 <= exc.status_code <= 599:
            logger.error(f"Error {exc.status_code} happened", exc_info=exc)
        return exception_handler(exc, context)
    else:
        logger.error("Server error", exc_info=exc)
        return Response("Server error", status=500)
