package com.example.podcastapp.utils

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data object Loading : Resource<Nothing>
    data object Error : Resource<Nothing>
    data object Idle : Resource<Nothing>
}