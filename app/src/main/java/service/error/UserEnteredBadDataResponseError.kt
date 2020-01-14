package com.example.androidtraining.service.error


class UserEnteredBadDataResponseError(override val message: String, val error_type: String): Throwable(message)