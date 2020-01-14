package com.example.androidtraining.service.logger


abstract class DevelopmentAppActivityLogger: ActivityLogger {

    override fun httpRequestEvent(method: String, url: String) {
        logEvent("HttpEvent", "Request method: $method, url: $url")
    }

    override fun httpSuccessEvent(method: String, url: String) {
        logEvent("HttpEvent", "Response Success! method: $method, url: $url")
    }

    override fun httpFailEvent(method: String, url: String, code: Int, reqHeaders: String?, resHeaders: String?, resBody: String?) {
        val message = "Response Failed! method: $method, url: $url, code: $code, req headers: ${reqHeaders ?: "(none)"}, res headers: ${resHeaders ?: "(none)"}, res body: ${resBody ?: "(none)"}"

        // Log event and error so we capture it well.
        logEvent("HttpEvent", message)
        logError(HttpFailError(message))
    }

    override fun errorOccurred(error: Throwable) {
        logError(error)
    }

    class HttpFailError(message: String): Throwable(message)

    abstract fun logEvent(tag: String, message: String)

    abstract fun logError(error: Throwable)

}