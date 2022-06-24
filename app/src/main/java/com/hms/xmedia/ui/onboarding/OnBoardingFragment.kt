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
package com.hms.xmedia.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.hms.xmedia.R
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.data.model.OnBoardingDataModel
import com.hms.xmedia.databinding.FragmentOnboardingBinding
import com.hms.xmedia.ui.onboarding.adapter.OnBoardingSliderAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingFragment : BaseFragment<OnBoardingViewModel, FragmentOnboardingBinding>() {

    private val viewModel: OnBoardingViewModel by viewModels()

    override fun getFragmentViewModel(): OnBoardingViewModel = viewModel

    private val onBoardingSliderAdapter by lazy {
        OnBoardingSliderAdapter(
            listOf(
                OnBoardingDataModel(
                    getString(R.string.onBoarding_title_edit_photo),
                    getString(R.string.onBoarding_desc_edit_photo),
                    "lottie/animation_photo_edit.json"
                ),
                OnBoardingDataModel(
                    getString(R.string.onBoarding_title_edit_video_audio),
                    getString(R.string.onBoarding_desc_edit_video_audio),
                    "lottie/animation_video_edit.json"
                ),
                OnBoardingDataModel(
                    getString(R.string.onBoarding_title_save_cloud),
                    getString(R.string.onBoarding_desc_save_cloud),
                    "lottie/animation_cloud.json"
                )
            )
        )
    }


    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnboardingBinding {
        return FragmentOnboardingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentViewBinding.viewPagerSlider.adapter = onBoardingSliderAdapter
        fragmentViewBinding.indicator.setViewPager(fragmentViewBinding.viewPagerSlider)
        fragmentViewBinding.viewPagerSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == onBoardingSliderAdapter.itemCount - 1) {
                    val animation = AnimationUtils.loadAnimation(
                        requireActivity(),
                        R.anim.slider_finish_animation
                    )
                    fragmentViewBinding.buttonNext.animation = animation
                    fragmentViewBinding.buttonNext.text =
                        getString(R.string.onBoarding_button_finish)
                    fragmentViewBinding.buttonNext.setOnClickListener {
                        viewModel.saveOnBoardingShowedStatus(true)
                        findNavController()
                            .navigate(R.id.action_onboardingFragment_to_loginFragment)
                    }
                } else {
                    fragmentViewBinding.buttonNext.text = getString(R.string.onBoarding_button_next)
                    fragmentViewBinding.buttonNext.setOnClickListener {
                        fragmentViewBinding.viewPagerSlider.currentItem.let {
                            fragmentViewBinding.viewPagerSlider.setCurrentItem(it + 1, false)
                        }
                    }
                }
            }
        })


    }
}