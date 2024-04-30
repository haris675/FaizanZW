package com.app.faizanzw.utils


sealed class AppState<T> {
    class Loading<T>(val progress: String = "") : AppState<T>()
    data class Success<T>(val model: T, val code: Int = 0) : AppState<T>()
    data class Error<T>(val error: String = "", val code: Int = 0) : AppState<T>()
}