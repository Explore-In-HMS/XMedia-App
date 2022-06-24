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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaFile(
    val Id: String,
    var isLocalFile:Boolean = false,
    val mediaFileType: MediaFileType,
    val path: String,
    val title: String,
    val fileExtension: String,
    val fileNameWithoutExtension: String,
    val fileAddedDate: String,
    val fileSize: String,
    val duration: String?,
    val artist: String?,
    val cloudFileFullName: String?,
    val downloadUri: String?
) : Parcelable