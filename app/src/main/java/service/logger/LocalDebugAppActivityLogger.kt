package com.example.androidtraining.service.logger


import android.util.Log
import com.phanthony.instantrecipe.BuildConfig
import extensions.getStacktrace

class LocalDebugAppActivityLogger: DevelopmentAppActivityLogger() {

    companion object {
        private const val TAG = "GitHubApp_DEV"
    }

    override fun logEvent(tag: String, message: String) {
        logMessage("$tag: $message")
    }

    override fun logError(error: Throwable) {
        logMessage("ERROR: ${error.message}\n\n${error.getStacktrace()}")
    }

    private fun logMessage(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

}