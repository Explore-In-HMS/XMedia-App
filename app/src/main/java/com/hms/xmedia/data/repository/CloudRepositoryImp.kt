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
package com.hms.xmedia.data.repository

import com.hms.xmedia.data.model.Error
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.Resource
import com.hms.xmedia.data.model.Status
import com.hms.xmedia.data.remote.CloudDBDataSource
import com.hms.xmedia.data.remote.CloudStorageDataSource
import com.hms.xmedia.utils.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class CloudRepositoryImp @Inject constructor(
    private val cloudDBDataSource: CloudDBDataSource,
    private val cloudStorageDataSource: CloudStorageDataSource
) : CloudRepository {

    override suspend fun uploadMediaFile(mediaFile: MediaFile): Flow<Resource<Boolean>> {
        return flow {
            try {
                emit(Resource.progress())
                val response = cloudStorageDataSource.uploadFileToStorage(mediaFile)
                when (response.status) {
                    Status.SUCCESSFUL -> {
                        val request = cloudDBDataSource.saveMediaFile(mediaFile, response.data!!)
                        when (request.status) {
                            Status.SUCCESSFUL -> {
                                emit(Resource.success(true))
                            }
                            Status.ERROR -> {
                                emit(
                                    Resource.error(
                                        Error(
                                            null,
                                            response.error?.errorMessage ?: "Error"
                                        )
                                    )
                                )
                            }
                            Status.LOADING -> {
                            }
                        }
                    }
                    Status.ERROR -> {
                        emit(Resource.error(Error(null, response.error?.errorMessage ?: "Error")))
                    }
                    Status.LOADING -> {
                    }
                }
            } catch (exception: Exception) {
                emit(Resource.error(Error(null, exception.message ?: "Error")))
            }

        }.flowOn(Dispatchers.IO)
    }

    override suspend fun deleteMediaFile(mediaFile: MediaFile): Flow<Resource<Boolean>> {
        return flow {
            emit(Resource.progress())
            if (mediaFile.isLocalFile) {
                emit(Resource.error(Error(null, Constant.ERROR_NOT_CLOUD_FILE)))
            } else {
                val deleteFromCloudStorageResponse =
                    cloudStorageDataSource.deleteUserFile(mediaFile)
                when (deleteFromCloudStorageResponse.status) {
                    Status.SUCCESSFUL -> {
                        val deleteFromCloudDB = cloudDBDataSource.deleteMediaFile(mediaFile)
                        emit(deleteFromCloudDB)
                    }
                    Status.ERROR -> {
                        emit(
                            Resource.error(
                                Error(
                                    null,
                                    deleteFromCloudStorageResponse.error?.errorMessage
                                        ?: "Error"
                                )
                            )
                        )
                    }
                    Status.LOADING -> {
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    override suspend fun getAllUserCloudFiles(): Flow<Resource<List<MediaFile>>> {
        return cloudDBDataSource.getUserAllMediaFiles()
    }

}