package com.example.androidtraining.service.logger

import android.os.Bundle

class AppActivityLogger(): ActivityLogger {

    private val loggers: List<ActivityLogger> = listOf(
        LocalDebugAppActivityLogger()
    )

    override fun httpRequestEvent(method: String, url: String) {
        loggers.forEach { it.httpRequestEvent(method, url) }
    }

    override fun httpSuccessEvent(method: String, url: String) {
        loggers.forEach { it.httpSuccessEvent(method, url) }
    }

    override fun httpFailEvent(method: String, url: String, code: Int, reqHeaders: String?, resHeaders: String?, resBody: String?) {
        loggers.forEach { it.httpFailEvent(method, url, code, reqHeaders, resHeaders, resBody) }
    }

    override fun errorOccurred(error: Throwable) {
        loggers.forEach { it.errorOccurred(error) }
    }

}