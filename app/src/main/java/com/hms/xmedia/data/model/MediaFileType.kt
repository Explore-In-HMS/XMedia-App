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

enum class MediaFileType {
    AUDIO_FILE,
    VIDEO_FILE,
    IMAGE_FILE;

    companion object {
        fun getMediaFileFromExtension(extension: String): MediaFileType? {
            return when (extension) {
                "mp3", "wav", "flac" -> AUDIO_FILE
                "mp4" -> VIDEO_FILE
                "jpg" -> IMAGE_FILE
                else -> null
            }
        }
    }
}