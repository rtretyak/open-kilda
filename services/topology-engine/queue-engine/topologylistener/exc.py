# Copyright 2017 Telstra Open Source
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

import sys


class Error(Exception):
    pass


class DBInvalidResponse(Error):
    pass


class DBEmptyResponse(DBInvalidResponse):
    def __str__(self):
        return 'There is no record fetched from DB cursor'


class DBMultipleResponse(DBInvalidResponse):
    def __str__(self):
        return 'DB cursor contain too many result records'


class DBRecordNotFound(Error):
    @property
    def query(self):
        return self.args[0]

    @property
    def params(self):
        return self.args[1]

    def __init__(self, query, params):
        super(DBRecordNotFound, self).__init__(query, params)

    def __str__(self):
        return 'DB record not found'


class InvalidDecodeError(Error):
    @property
    def details(self):
        return self.args[0]

    @property
    def raw_request(self):
        return self.args[1]

    def __init__(self, details, raw_request):
        super().__init__(details, raw_request)

    def __str__(self):
        return 'Can\'t decode input: {}'.format(self.details)


class NoHandlerError(Error):
    def __str__(self):
        return 'There is no handler for this request'


class RecoverableError(Error):
    @property
    def cause(self):
        return self.args[0]

    def __init__(self, cause=None):
        super(RecoverableError, self).__init__(cause)

    def __str__(self):
        msg = 'Recoverable error (operation can be successful in next attempt)'
        if self.cause:
            msg += ': {}'.format(self.cause)
        return msg


class UnrecoverableError(Error):
    """
    Ugly workaround over retry mechanism. Drop it when all relevant code will
    use RecoverableError
    """

    @property
    def exc_info(self):
        return self.args[0]

    def __init__(self):
        super(UnrecoverableError, self).__init__(sys.exc_info())
