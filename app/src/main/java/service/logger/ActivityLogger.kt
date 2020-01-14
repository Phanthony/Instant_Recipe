package com.example.androidtraining.service.logger

/**
 * Log the many activities/events that can happen within the app.
 */
interface ActivityLogger {
    fun httpRequestEvent(method: String, url: String)
    fun httpSuccessEvent(method: String, url: String)
    fun httpFailEvent(method: String, url: String, code: Int, reqHeaders: String?, resHeaders: String?, resBody: String?)
    fun errorOccurred(error: Throwable)
}