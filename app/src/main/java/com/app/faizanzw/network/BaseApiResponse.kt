package com.app.faizanzw.network

import com.app.faizanzw.utils.AppState
import retrofit2.Response

abstract class BaseApiResponse {
    protected suspend fun <T> safeApiCall(
        //@ApplicationContext context: Context,
        apiCall: suspend () -> Response<T>
    ): AppState<T> {
        try {
            val response = apiCall()
            AppState.Loading<T>()
            if (response.isSuccessful) {
                response.body()?.let {
                    return AppState.Success(it, response.code())
                }
            }
            return if (response.code() == 403) {
                error(response.message(), response.code())
            } else if (response.code() == 401) {
                error(response.message(), response.code())
            } else {
                error(response.message(), response.code())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return error(e.message ?: e.toString(), 0)
        }
    }

    private fun <T> error(errorMessage: String, code: Int = 0): AppState<T> =
        AppState.Error("Api call failed $errorMessage", code)
}