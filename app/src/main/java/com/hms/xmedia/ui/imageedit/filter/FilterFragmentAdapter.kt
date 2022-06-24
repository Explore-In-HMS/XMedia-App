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
package com.hms.xmedia.ui.imageedit.filter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hms.xmedia.R
import com.hms.xmedia.databinding.ItemFilterImageBinding
import com.hms.xmedia.ui.imageedit.OnItemClickListener

class FilterFragmentAdapter(
    private var filteredImages: List<Bitmap>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<FilterFragmentAdapter.MainViewHolder>() {

    class MainViewHolder(val binding: ItemFilterImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            ItemFilterImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val filteredImage = filteredImages[position]

        Glide.with(holder.binding.root.context)
            .load(filteredImage)
            .error(R.drawable.ic_broken_image)
            .placeholder(R.drawable.ic_broken_image)
            .into(holder.binding.ivFilteredImage)

        holder.binding.ivFilteredImage.setOnClickListener {
            onItemClickListener.onItemClicked(position)
        }
    }

    fun updateList(newFilteredImages: List<Bitmap>) {
        filteredImages = newFilteredImages
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return filteredImages.size
    }
}