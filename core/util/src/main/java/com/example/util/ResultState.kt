package com.example.util


sealed class ResultState<out T, out E> {
    data class Success<out T>(val data: T) : ResultState<T, Nothing>()
    data class Error<out E>(val error: E) : ResultState<Nothing, E>()
}