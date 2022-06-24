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
package com.hms.xmedia.ui.imageedit.sticker

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hms.xmedia.R
import com.hms.xmedia.databinding.ItemStickerImageBinding
import com.hms.xmedia.ui.imageedit.OnItemClickListener

class StickerFragmentAdapter(
    private val stickerPaths: List<String>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<StickerFragmentAdapter.MainViewHolder>() {

    class MainViewHolder(val binding: ItemStickerImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            ItemStickerImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val path = stickerPaths[position]
        val pictureName = stickerPaths[position].substringAfterLast("/")
        Glide.with(holder.binding.root.context)
            .load(Uri.parse("file:///android_asset/vgmap/sticker1/res/$pictureName"))
            .error(R.drawable.sticker_background_img)
            .placeholder(R.drawable.sticker_background_img)
            .into(holder.binding.ivSticker)
        holder.binding.ivSticker.setOnClickListener {
            onItemClickListener.onItemClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return stickerPaths.size
    }
}