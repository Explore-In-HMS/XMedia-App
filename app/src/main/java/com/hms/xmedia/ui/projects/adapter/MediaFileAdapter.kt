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
package com.hms.xmedia.ui.projects.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hms.xmedia.R
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.MediaFileType
import com.hms.xmedia.databinding.ItemMediaBinding
import java.io.File

class MediaFileAdapter(
    private val projectClickListener: ProjectClickListener
) :
    ListAdapter<MediaFile, MediaFileAdapter.MediaFileViewHolder>(MediaFileDiffCallback) {

    class MediaFileViewHolder(
        private val binding: ItemMediaBinding,
        private val projectClickListener: ProjectClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mediaFile: MediaFile) {
            binding.tvCardName.text = binding.root.context.getString(
                R.string.name_file_and_extension,
                mediaFile.title,
                mediaFile.fileExtension
            )
            binding.LayoutCard.setOnClickListener {
                projectClickListener.onProjectItemClicked(mediaFile)
            }

            binding.buttonMore.setOnClickListener {
                projectClickListener.onProjectMoreClicked(mediaFile)
            }

            when (mediaFile.mediaFileType) {
                MediaFileType.AUDIO_FILE -> {
                    Glide.with(binding.root.context)
                        .load(R.drawable.ic_bg_audio)
                        .into(binding.ivCardBackground)
                }
                MediaFileType.VIDEO_FILE -> {
                    Glide.with(binding.root.context)
                        .load(R.drawable.ic_video_with_space)
                        .into(binding.ivCardBackground)
                }
                MediaFileType.IMAGE_FILE -> {
                    if (mediaFile.isLocalFile) {
                        mediaFile.path.let { filePath ->
                            Glide.with(binding.root.context)
                                .load(File(filePath))
                                .into(binding.ivCardBackground)
                        }
                    } else {
                        Glide.with(binding.root.context)
                            .load(mediaFile.downloadUri)
                            .into(binding.ivCardBackground)
                    }

                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaFileViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaFileViewHolder(binding, projectClickListener)
    }

    override fun onBindViewHolder(holder: MediaFileViewHolder, position: Int) {
        val mediaFile = getItem(position)
        holder.bind(mediaFile)
    }

    object MediaFileDiffCallback : DiffUtil.ItemCallback<MediaFile>() {

        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean =
            oldItem.Id == newItem.Id
    }

}

interface ProjectClickListener {
    fun onProjectItemClicked(mediaFile: MediaFile)
    fun onProjectMoreClicked(mediaFile: MediaFile)
}

