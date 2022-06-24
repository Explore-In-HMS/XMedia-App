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
package com.hms.xmedia.data.remote

import android.util.Log
import com.hms.xmedia.data.model.MediaFileOnline
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.hmf.tasks.Task
import com.hms.xmedia.data.model.*
import com.hms.xmedia.utils.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class CloudDBDataSource @Inject constructor(
    private var cloudDB: AGConnectCloudDB,
    private var zoneConfig: CloudDBZoneConfig,
    private val agConnectAuth: AGConnectAuth
) {

    companion object {
        const val TAG = "CloudDBDataSource"
    }

    private lateinit var cloudDBZone: CloudDBZone

    private suspend fun checkInitCloudDB(): Boolean {
        return suspendCoroutine { cont ->
            if (this::cloudDBZone.isInitialized) {
                cont.resume(true)
            } else {
                cloudDB.openCloudDBZone2(zoneConfig, true).addOnSuccessListener {
                    this.cloudDBZone = it
                    cont.resume(true)
                }.addOnFailureListener {
                    Log.e(
                        TAG, "${Constant.ERROR_INITIALIZED_CLOUD_DB} - ${it.message}"
                    )
                    cont.resume(false)
                }
            }
        }
    }

    suspend fun saveMediaFile(
        mediaFile: MediaFile,
        fileUploadStorageModel: FileUploadStorageModel
    ): Resource<Boolean> {
        if (!checkInitCloudDB()) {
            return Resource.error(Error(null, errorMessage = Constant.ERROR_INITIALIZED_CLOUD_DB))
        }
        return suspendCoroutine { continuation ->
            val currentUserId = agConnectAuth.currentUser.uid
            val mediaFileOnline = MediaFileOnline()
            mediaFileOnline.apply {
                mediaId = mediaFile.Id
                mediaType = mediaFile.mediaFileType.name
                mediaURI = fileUploadStorageModel.cloudFileURI
                userId = currentUserId
                title = mediaFile.title
                fileExtension = mediaFile.fileExtension
                fileNameWithoutExtension = mediaFile.fileNameWithoutExtension
                fileAddedDate = mediaFile.fileAddedDate
                fileSize = mediaFile.fileSize
                cloudFileFullName = fileUploadStorageModel.cloudFileName
                fileAddedDate = mediaFile.fileAddedDate
                duration = mediaFile.duration
                artist = mediaFile.artist
            }

            val upsertTask: Task<Int> = cloudDBZone.executeUpsert(mediaFileOnline)
            upsertTask.addOnSuccessListener {
                continuation.resume(Resource.success(true))
            }.addOnFailureListener { exception ->
                continuation.resume(
                    Resource.error(
                        Error(
                            null,
                            errorMessage = "${Constant.ERROR_CLOUD_DB_SAVE} - ${exception.message}"
                        )
                    )
                )
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getUserAllMediaFiles(): Flow<Resource<List<MediaFile>>> =
        withContext(Dispatchers.IO) {
            callbackFlow {
                val currentUserId = agConnectAuth.currentUser.uid
                val mediaFileOnlineList = mutableListOf<MediaFileOnline>()
                trySend(Resource.progress())

                if (!checkInitCloudDB()) {
                    trySend(
                        Resource.error(
                            Error(
                                null,
                                errorMessage = Constant.ERROR_INITIALIZED_CLOUD_DB
                            )
                        )
                    )
                    return@callbackFlow
                }

                val query: CloudDBZoneQuery<MediaFileOnline> =
                    CloudDBZoneQuery.where(MediaFileOnline::class.java)
                        .equalTo("userId", currentUserId)
                val queryTask = cloudDBZone.executeQuery(
                    query,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
                )

                try {
                    queryTask.addOnSuccessListener { snapshot ->
                        if (snapshot.snapshotObjects != null) {

                            if (snapshot.snapshotObjects.size() == 0) {
                                trySend(
                                    Resource.success(emptyList())
                                )
                                return@addOnSuccessListener
                            }
                            while (snapshot.snapshotObjects.hasNext()) {
                                val mediaFileOnline: MediaFileOnline =
                                    snapshot.snapshotObjects.next()
                                mediaFileOnlineList.add(mediaFileOnline)
                            }
                            val mediaFileList = convertMediaFileList(mediaFileOnlineList)
                            trySend(Resource.success(mediaFileList))
                        }
                    }
                        .addOnFailureListener { exception ->
                            trySend(
                                Resource.error(
                                    Error(
                                        null,
                                        errorMessage = "${Constant.ERROR_CLOUD_DB_GETTING_DATA} - ${exception.message}"
                                    )
                                )
                            )
                        }
                } catch (exception: Exception) {
                    trySend(
                        Resource.error(
                            Error(
                                null,
                                errorMessage = "${Constant.ERROR_CLOUD_DB_GETTING_DATA} - ${exception.message}"
                            )
                        )
                    )
                }
                awaitClose {
                    queryTask.addOnSuccessListener(null)
                    queryTask.addOnFailureListener(null)
                }
            }
        }


    suspend fun deleteMediaFile(mediaFile: MediaFile): Resource<Boolean> {
        if (!checkInitCloudDB()) {
            return Resource.error(
                Error(
                    null,
                    errorMessage = Constant.ERROR_INITIALIZED_CLOUD_DB
                )
            )
        }

        return suspendCoroutine { continuation ->

            val currentUserId = agConnectAuth.currentUser.uid
            val query: CloudDBZoneQuery<MediaFileOnline> =
                CloudDBZoneQuery.where(MediaFileOnline::class.java).equalTo("userId", currentUserId)
                    .and()
                    .equalTo("mediaURI", mediaFile.downloadUri!!)
            val queryTask = cloudDBZone.executeQuery(
                query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask.addOnSuccessListener { snapshot ->
                val mediaFileOnlineList = mutableListOf<MediaFileOnline>()
                while (snapshot.snapshotObjects.hasNext()) {
                    val mediaFileOnline: MediaFileOnline = snapshot.snapshotObjects.next()
                    mediaFileOnlineList.add(mediaFileOnline)
                }
                val deleteTask = cloudDBZone.executeDelete(mediaFileOnlineList)
                deleteTask.addOnSuccessListener {
                    continuation.resume(Resource.success(true))
                }
                deleteTask.addOnFailureListener { exception ->
                    continuation.resume(
                        Resource.error(
                            Error(
                                null,
                                errorMessage = "${Constant.ERROR_CLOUD_DB_DELETE_DATA} : ${exception.message}"
                            )
                        )
                    )
                }
            }
                .addOnFailureListener { exception ->
                    continuation.resume(
                        Resource.error(
                            Error(
                                null,
                                errorMessage = "${Constant.ERROR_CLOUD_DB_DELETE_DATA} : ${exception.message}"
                            )
                        )
                    )
                }
        }
    }

    private fun convertMediaFileList(mediaFileOnlineList: List<MediaFileOnline>): List<MediaFile> {
        val tempMediaFileList = mutableListOf<MediaFile>()
        mediaFileOnlineList.forEach {
            val mediaFile = MediaFile(
                Id = it.mediaId,
                isLocalFile = false,
                mediaFileType = MediaFileType.valueOf(it.mediaType),
                path = it.mediaURI,
                title = it.title,
                fileExtension = it.fileExtension,
                fileNameWithoutExtension = it.fileNameWithoutExtension,
                fileAddedDate = it.fileAddedDate,
                fileSize = it.fileSize,
                duration = it.duration,
                artist = it.artist,
                cloudFileFullName = it.cloudFileFullName,
                downloadUri = it.mediaURI
            )
            tempMediaFileList.add(mediaFile)
        }
        return tempMediaFileList
    }

}