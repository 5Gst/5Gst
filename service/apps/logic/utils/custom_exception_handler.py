import logging
import traceback

from rest_framework.views import exception_handler

logger = logging.getLogger(__name__)


def custom_exception_handler(exc, context):
    response = exception_handler(exc, context)
    response.data = {
        'status_code': response.status_code,
        'status_text': response.status_text,
        'default_detail': exc.default_detail,
    }

    logger.exception(traceback.format_exc())
    return response
