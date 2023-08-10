from rest_framework.exceptions import APIException
from rest_framework import status


class ClientError(APIException):
    detail = None
    status_code = None

    def __init__(self, detail, code):
        super().__init__(detail, code)
        self.detail = detail
        self.status_code = code


class BadRequest(ClientError):
    def __init__(self, detail='Bad Request 400'):
        super().__init__(detail, status.HTTP_400_BAD_REQUEST)


class InternalServerError(ClientError):
    def __init__(self, detail='Internal Server Error 400'):
        super().__init__(detail, status.HTTP_500_INTERNAL_SERVER_ERROR)
