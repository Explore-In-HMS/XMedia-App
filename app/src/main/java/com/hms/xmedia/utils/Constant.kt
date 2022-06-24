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

object Constant {

    const val FOLDER_NAME_OF_THE_SAVED_AUDIO_FILES = "XMedia"
    const val FOLDER_NAME_OF_THE_SAVED_IMAGE_FILES = "XMedia"


    const val loginRequestCode = 1003

    const val PERMISSION_REQUEST_CODE_CAMERA = 1
    const val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1045
    const val REQUEST_PICK_IMAGE = 1001

    const val KEY_AVAILABILITY_CALENDAR_SHOULD_REFRESH = 1002
    const val KEY_SHOULD_REFRESH_PROJECT = "KEY_SHOULD_REFRESH_PROJECT"

    const val AUDIO_EDITOR_REQUEST_CODE = 1001

    val filters: ArrayList<String> = arrayListOf(
        "No filter",
        "Black-and-white",
        "Brown tone",
        "Lazy",
        "Freesia",
        "Fuji",
        "Peach pink",
        "Sea salt",
        "Mint",
        "Reed",
        "Vintage",
        "Marshmallow",
        "Moss",
        "Sunlight",
        "Time",
        "Haze blue",
        "Sunflower",
        "Hard",
        "Bronze yellow",
        "Monochromic tone",
        "Yellow-green tone",
        "Yellow tone",
        "Green tone",
        "Cyan tone",
        "Violet tone"
    )

    const val MUSIC_NOTIFY_CHANNEL_ID_PLAY = "1111"

    const val ERROR_DELETE_FILE = "An error occurred while deleting file."
    const val ERROR_CANCEL_DELETE_FILE = "Cancelled file deleting."
    const val ERROR_UPLOAD_FILE = "An error occurred while uploading file."
    const val ERROR_GETTING_DOWNLOAD_URL = "An error occurred while getting download URL."
    const val ERROR_INITIALIZED_CLOUD_DB = "Cloud DB is not initialized"
    const val ERROR_CLOUD_DB_SAVE = "An error occurred while saving file."
    const val ERROR_CLOUD_DB_USER_NOT_EXIST = "User is not exist"
    const val ERROR_CLOUD_DB_GETTING_DATA = "An error occurred while getting files"
    const val ERROR_CLOUD_DB_DELETE_DATA = "An error occurred while deleting file"
    const val ERROR_NOT_CLOUD_FILE = "It is not a Cloud Saved File"
}