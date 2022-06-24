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
package com.hms.xmedia.ui.onboarding.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hms.xmedia.data.model.OnBoardingDataModel
import com.hms.xmedia.databinding.ItemOnboardingSliderBinding

class OnBoardingSliderAdapter(private val onBoardingDataList: List<OnBoardingDataModel>) :
    RecyclerView.Adapter<OnBoardingSliderAdapter.OnBoardingSliderViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnBoardingSliderViewHolder {
        return OnBoardingSliderViewHolder(
            ItemOnboardingSliderBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return onBoardingDataList.size
    }

    override fun onBindViewHolder(holder: OnBoardingSliderViewHolder, position: Int) {
        val introSlide = onBoardingDataList[position]

        holder.binding.textTitle.text = introSlide.title
        holder.binding.textDescription.text = introSlide.description
        holder.binding.animationViewSlider.imageAssetsFolder = "images"
        holder.binding.animationViewSlider.setAnimation(introSlide.icon)
    }

    class OnBoardingSliderViewHolder(val binding: ItemOnboardingSliderBinding) :
        RecyclerView.ViewHolder(binding.root)
}