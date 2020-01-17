package com.phanthony.instantrecipe.service

import android.content.Context
import com.example.androidtraining.service.error.*
import com.example.androidtraining.service.logger.ActivityLogger
import com.phanthony.instantrecipe.R
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result as RxResult
import kotlin.Result as KtResult
import java.io.IOException

class ResponseProcessor(
    private val context: Context,
    private val logger: ActivityLogger,
    private val jsonAdapter: JsonAdapter
) {

    fun <RESPONSE> process(
        result: RxResult<RESPONSE>,
        extraErrorHandling: ((code: Int, response: Response<RESPONSE>, errorBody: String, jsonAdapter: JsonAdapter) -> Throwable?)? = null
    ): ProcessedResult<RESPONSE> {
        when (result.error()) {
            is NoInternetConnectionException, is UnauthorizedException -> result.error()
            is IOException -> NetworkConnectionIssueException(context.getString(R.string.error_network_connection_issue))
            null -> {
                null
            }
            else -> {
                // According to Retrofit's result.error() Javadoc, if the error is not an instance of IOException, it's a programming error and should be looked at. Throw it so we can see it and fix it.
                logger.errorOccurred(result.error()!!)
                unhandledHttpResult(result)
            }
        }?.let { resultError ->
            return ProcessedResult(resultError, resultError.message!!, null)
        }

        val errorBody = result.response()?.errorBody()?.string()
        val statusCode = result.response()!!.code()
        when (statusCode) {
            in 500..600 -> ServerErrorException(context.getString(R.string.error_500_600_response_code))
            /**
             * Do not list FieldsError here. If a 422 happens, it might be a problem with the app and not the user. Therefore, if a 422 could happen, have the API call handle it individually instead of handle it globally here.
             */
            in 400 until 500 -> {
                extraErrorHandling?.invoke(statusCode, result.response()!!, errorBody!!, jsonAdapter)
            }
            else -> null
        }?.let { networkResponseError ->
            return ProcessedResult(networkResponseError, networkResponseError.message!!, null)
        }

        val response = result.response()!!
        return if (!response.isSuccessful) {
            val error = unhandledHttpResult(result)
            ProcessedResult(error, error.message!!, null)
        } else {
            // Finally. The successful response!
            return ProcessedResult(null,response.message(),response.body())
        }
    }

    data class ProcessedResult<T>(
        val error: Throwable?,
        val message: String?,
        val body: T?
    ) {

        fun isFailure(): Boolean = error != null

        fun isSuccessful(): Boolean = body != null

    }

    // During development you should be handling client side all of the ways that a HTTP API call could fail. However, you may forget some. So to handle that, we need to alert ourselves to fix this issue and to return back to our users a human readable message saying that an error we cannot handle it and we are going to fix it. So this code here logs the error and then returns back a Throwable that we can show the message to the user.
    fun <RESPONSE> unhandledHttpResult(result: RxResult<RESPONSE>): UnhandledHttpResultException {
        logger.errorOccurred(
            UnhandledHttpResultException(
                "Fatal HTTP network call. ${result.response()?.toString()
                    ?: "(no HTTP response found)."}"
            )
        )
        return UnhandledHttpResultException(context.getString(R.string.unhandled_http_result_message))
    }

}