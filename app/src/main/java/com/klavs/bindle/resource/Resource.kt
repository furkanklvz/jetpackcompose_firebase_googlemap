package com.klavs.bindle.resource

sealed class Resource<T>(val data: T? = null, val messageResource: Int? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(messageResource: Int, data: T? = null) : Resource<T>(data, messageResource)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Idle<T> : Resource<T>()
}