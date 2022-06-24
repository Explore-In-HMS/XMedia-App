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
package com.hms.xmedia.ui.projects

import androidx.lifecycle.viewModelScope
import com.hms.xmedia.base.BaseViewModel
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.Resource
import com.hms.xmedia.data.repository.CloudRepository
import com.hms.xmedia.data.repository.MediaStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository,
    private val cloudRepository: CloudRepository
) : BaseViewModel() {

    companion object {
        const val TAG = "ProjectsViewModel"
    }

    var isStoragePermissionGranted = false

    val isLocalProjectSelected: MutableStateFlow<Boolean> = MutableStateFlow(true)

    val cloudMediaFileList: MutableStateFlow<Resource<List<MediaFile>>> =
        MutableStateFlow(Resource.progress())

    val localMediaFileList: MutableStateFlow<Resource<List<MediaFile>>> =
        MutableStateFlow(Resource.progress())


    fun getAllLocalMediaFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            mediaStoreRepository.getAllMediaFiles().collect { resource ->
                localMediaFileList.emit(resource)
            }
        }
    }

    fun getAllCloudMediaFile() {
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.getAllUserCloudFiles().collect { resource ->
                cloudMediaFileList.emit(resource)
            }
        }
    }

    suspend fun deleteUserFile(mediaFile: MediaFile): Flow<Resource<Boolean>> {
        return cloudRepository.deleteMediaFile(mediaFile)
    }

    fun changeStoragePermissionGrantedStatus(value: Boolean) {
        isStoragePermissionGranted = value
    }

}