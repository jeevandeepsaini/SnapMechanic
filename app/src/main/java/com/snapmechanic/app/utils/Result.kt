package com.snapmechanic.app.utils

/**
 * Result — a simple sealed class to represent three states of any operation:
 *   Success → operation worked, here is the data
 *   Error   → operation failed, here is the error message
 *   Loading → operation is in progress
 *
 * Why use this? Instead of juggling multiple booleans (isLoading, isError, hasData),
 * every API call returns one of these three clear states.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
