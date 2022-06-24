/*
 * Copyright 2022. Explore in HMS. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hms.xmedia.data.model

data class Resource<T>(
    var status: Status,
    var data: T? = null,
    var error: Error? = null
) {

    val isStatusSuccess: Boolean = status == Status.SUCCESSFUL
    val isStatusLoading: Boolean = status == Status.LOADING
    val isStatusError: Boolean = status == Status.ERROR

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(status = Status.SUCCESSFUL, data = data)
        }

        fun <T> progress(): Resource<T> {
            return Resource(status = Status.LOADING)
        }

        fun <T> error(error: Error?): Resource<T> {
            return Resource(status = Status.ERROR, error = error)
        }
    }
}

enum class Status { SUCCESSFUL, ERROR, LOADING }

data class Error(
    val errorCode: Int? = null,
    val errorMessage: String = ""
)