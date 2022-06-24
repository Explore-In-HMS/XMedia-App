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
package com.hms.xmedia.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.MediaFileType
import com.hms.xmedia.utils.Constant
import com.hms.xmedia.utils.FileUtils
import com.hms.xmedia.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Singleton

@Singleton
class MediaStoreDataSource(val context: Context) {

    fun getAudioFilesFromLocal(): Flow<List<MediaFile>> = flow {

        val audioFileList: ArrayList<MediaFile> = ArrayList()
        val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Audio.Media.DATA} LIKE ?"
        }

        val folderName = Constant.FOLDER_NAME_OF_THE_SAVED_AUDIO_FILES

        val selectionArgs = arrayOf("%Music/$folderName%")

        val query = context.contentResolver.query(songUri, null, selection, selectionArgs, null)

        query?.use { cursor ->
            val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val songTitleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val songArtistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val songDurationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val createdDateIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val songSizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)


            while (cursor.moveToNext()) {
                val duration = Utils.millisecondToString(cursor.getInt(songDurationIndex).toLong())
                val contentId = cursor.getLong(idColumnIndex)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    contentId
                )
                val realPath = FileUtils.getRealPath(context, contentUri) ?: return@use
                val fileExtension =
                    FileUtils.getFileExtension(realPath)
                val fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(realPath)

                audioFileList += MediaFile(
                    Id = cursor.getInt(idColumnIndex).toString(),
                    isLocalFile = true,
                    mediaFileType = MediaFileType.AUDIO_FILE,
                    path = realPath,
                    title = cursor.getString(songTitleIndex),
                    fileExtension = fileExtension,
                    fileNameWithoutExtension = fileNameWithoutExtension,
                    fileAddedDate = (cursor.getLong(createdDateIndex) * 1000).toString(),
                    fileSize = cursor.getString(songSizeIndex),
                    artist = cursor.getString(songArtistIndex),
                    duration = duration,
                    cloudFileFullName = null,
                    downloadUri = contentUri.toString()
                )
            }
        }

        emit(audioFileList)

    }.flowOn(Dispatchers.IO)

    fun getVideoFilesFromLocal(): Flow<List<MediaFile>> = flow {

        val videoFileList: ArrayList<MediaFile> = ArrayList()
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Video.Media.DATA} LIKE ?"
        }

        val selectionArgs = arrayOf("%Pictures/VideoEditor%")

        val query =
            context.contentResolver.query(videoUri, null, selection, selectionArgs, null)

        query?.use { cursor ->
            val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val videoTitleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val videoArtistIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
            val videoDurationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val createdDateIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val videoSizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                val duration = Utils.millisecondToString(cursor.getLong(videoDurationIndex))
                val contentId = cursor.getLong(idColumnIndex)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentId
                )
                val realPath = FileUtils.getRealPath(context, contentUri) ?: return@use
                val fileExtension =
                    FileUtils.getFileExtension(realPath)
                val fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(realPath)

                videoFileList += MediaFile(
                    Id = cursor.getInt(idColumnIndex).toString(),
                    isLocalFile = true,
                    mediaFileType = MediaFileType.VIDEO_FILE,
                    path = realPath,
                    title = cursor.getString(videoTitleIndex),
                    fileExtension = fileExtension,
                    fileNameWithoutExtension = fileNameWithoutExtension,
                    fileAddedDate = (cursor.getLong(createdDateIndex) * 1000).toString(),
                    fileSize = cursor.getString(videoSizeIndex),
                    artist = cursor.getString(videoArtistIndex),
                    duration = duration,
                    cloudFileFullName = null,
                    downloadUri = contentUri.toString()
                )
            }
        }
        emit(videoFileList)
    }.flowOn(Dispatchers.IO)

    fun getImageFilesFromLocal(): Flow<List<MediaFile>> = flow {

        val imageFileList: ArrayList<MediaFile> = ArrayList()
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Images.Media.DATA} LIKE ?"
        }

        val folderName = Constant.FOLDER_NAME_OF_THE_SAVED_IMAGE_FILES

        val selectionArgs = arrayOf("%Pictures/$folderName%")

        val query =
            context.contentResolver.query(imageUri, null, selection, selectionArgs, null)

        query?.use { cursor ->
            val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val imageTitleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
            val createdDateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val imageSizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val contentId = cursor.getLong(idColumnIndex)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentId
                )
                val realPath = FileUtils.getRealPath(context, contentUri) ?: return@use
                val fileExtension =
                    FileUtils.getFileExtension(realPath)
                val fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(realPath)

                imageFileList += MediaFile(
                    Id = cursor.getInt(idColumnIndex).toString(),
                    isLocalFile = true,
                    mediaFileType = MediaFileType.IMAGE_FILE,
                    path = realPath,
                    title = cursor.getString(imageTitleIndex),
                    fileExtension = fileExtension,
                    fileNameWithoutExtension = fileNameWithoutExtension,
                    fileAddedDate = (cursor.getLong(createdDateIndex) * 1000).toString(),
                    fileSize = cursor.getString(imageSizeIndex),
                    artist = null,
                    duration = null,
                    cloudFileFullName = null,
                    downloadUri = contentUri.toString()
                )
            }
        }
        emit(imageFileList)
    }.flowOn(Dispatchers.IO)

}