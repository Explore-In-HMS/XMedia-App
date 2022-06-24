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

import com.hms.xmedia.data.local.MediaStoreDataSource
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class MediaStoreRepositoryImp @Inject constructor(
    private val mediaStoreDataSource: MediaStoreDataSource
) : MediaStoreRepository {
    override suspend fun getAudioFiles(): Flow<List<MediaFile>> {
        return mediaStoreDataSource.getAudioFilesFromLocal()
    }

    override suspend fun getVideoFiles(): Flow<List<MediaFile>> {
        return mediaStoreDataSource.getVideoFilesFromLocal()
    }

    override suspend fun getImageFiles(): Flow<List<MediaFile>> {
        return mediaStoreDataSource.getImageFilesFromLocal()
    }

    override suspend fun getAllMediaFiles(): Flow<Resource<List<MediaFile>>> = flow {
        val audioFilesAsFlow = mediaStoreDataSource.getAudioFilesFromLocal()
        val videoFilesAsFlow = mediaStoreDataSource.getVideoFilesFromLocal()
        val imageFilesAsFlow = mediaStoreDataSource.getImageFilesFromLocal()
        combine(
            audioFilesAsFlow,
            videoFilesAsFlow,
            imageFilesAsFlow
        ) { audioFiles, videoFiles, imageFiles ->
            audioFiles + videoFiles + imageFiles
        }.collect {
            emit(Resource.success(it))
        }
    }.flowOn(Dispatchers.IO)
}