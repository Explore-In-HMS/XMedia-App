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
package com.hms.xmedia.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.MediaFileType
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    private const val TAG = "Utils"

    fun millisecondToString(long: Long): String {
        val second = (long / 1000) % 60
        val min = (long / 1000) / 60
        return if (second < 10) {
            "$min:0$second"
        } else {
            "$min:$second"
        }
    }

    fun shareFileLocal(context: Context, mediaFile: MediaFile) {
        if (mediaFile.isLocalFile) {
            if (mediaFile.mediaFileType == MediaFileType.IMAGE_FILE) {
                val shareImageIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(mediaFile.downloadUri))
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(Intent.createChooser(shareImageIntent, "Share Image"))
            }
            if (mediaFile.mediaFileType == MediaFileType.VIDEO_FILE) {
                val shareVideoIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "video/*"
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(mediaFile.downloadUri))
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(Intent.createChooser(shareVideoIntent, "Share Video"))
            }

            if (mediaFile.mediaFileType == MediaFileType.AUDIO_FILE) {
                val shareAudioIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(mediaFile.downloadUri))
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(Intent.createChooser(shareAudioIntent, "Share Audio"))
            }
        }
    }

    fun getDateFromTimeInMillis(dateAsMilliSec: Long): String {
        if (dateAsMilliSec == 0L) return ""
        val date = Date(dateAsMilliSec)
        val language = "en"
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale(language))
        return dateFormat.format(date)
    }
}