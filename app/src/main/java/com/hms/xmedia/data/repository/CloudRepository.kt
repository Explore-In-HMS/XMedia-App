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

import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

interface CloudRepository {
    suspend fun uploadMediaFile(mediaFile: MediaFile): Flow<Resource<Boolean>>
    suspend fun deleteMediaFile(mediaFile: MediaFile): Flow<Resource<Boolean>>
    @ExperimentalCoroutinesApi
    suspend fun getAllUserCloudFiles(): Flow<Resource<List<MediaFile>>>
}