package com.example.podcastapp.util

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data object Loading : Resource<Nothing>
    data class Error(val e: Throwable? = null) : Resource<Nothing>
    data object Idle : Resource<Nothing>
}