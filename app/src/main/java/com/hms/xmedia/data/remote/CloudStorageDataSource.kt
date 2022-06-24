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

import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement
import com.hms.xmedia.data.model.Error
import com.hms.xmedia.data.model.FileUploadStorageModel
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.Resource
import com.hms.xmedia.utils.Constant
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class CloudStorageDataSource @Inject constructor(
    private val storageManagement: AGCStorageManagement,
    private val agConnectAuth: AGConnectAuth
) {

    companion object {
        const val TAG = "CloudStorageDataSource"
    }

    suspend fun uploadFileToStorage(mediaFile: MediaFile): Resource<FileUploadStorageModel> {
        return suspendCoroutine { cont ->
            val userId = agConnectAuth.currentUser.uid
            val fileExtension = mediaFile.fileExtension
            val newFileName = "XMedia_${System.currentTimeMillis()}.$fileExtension"
            val reference = storageManagement.getStorageReference("$userId/$newFileName")
            val uploadTask = reference.putFile(File(mediaFile.path))

            uploadTask.addOnSuccessListener { uploadResult ->
                uploadResult.metadata.storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val fileUploadResponseModel = FileUploadStorageModel(
                        cloudFileName = uploadResult.metadata.name,
                        cloudFileURI = uri.toString(),
                        fileSize = uploadResult.metadata.size.toString(),
                        fileCreatedTime = uploadResult.metadata.cTime
                    )
                    cont.resume(Resource.success(fileUploadResponseModel))
                }
                    .addOnFailureListener { exception ->
                        cont.resume(
                            Resource.error(
                                Error(
                                    null,
                                    exception.message
                                        ?: Constant.ERROR_GETTING_DOWNLOAD_URL
                                )
                            )
                        )
                    }
            }
                .addOnFailureListener { exception ->
                    cont.resume(
                        Resource.error(
                            Error(
                                null,
                                exception.message ?: Constant.ERROR_UPLOAD_FILE
                            )
                        )
                    )
                }
        }
    }

    suspend fun deleteUserFile(mediaFile: MediaFile): Resource<Boolean> {
        return suspendCoroutine { cont ->
            val reference = storageManagement.getStorageReference(mediaFile.cloudFileFullName)
            val deleteTask = reference.delete()
            deleteTask.addOnSuccessListener {
                cont.resume(Resource.success(true))
            }.addOnFailureListener {
                cont.resume(Resource.error(Error(null, Constant.ERROR_DELETE_FILE)))
            }.addOnCanceledListener {
                cont.resume(Resource.error(Error(null, Constant.ERROR_CANCEL_DELETE_FILE)))
            }
        }
    }

}