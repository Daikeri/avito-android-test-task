package com.example.util

// универсальный state
sealed class ResultState<out T> {
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val exceptionMsg: String) : ResultState<Nothing>()
}