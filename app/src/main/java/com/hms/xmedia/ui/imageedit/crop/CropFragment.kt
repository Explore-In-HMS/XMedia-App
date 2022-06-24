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
package com.hms.xmedia.ui.imageedit.crop

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.huawei.hms.image.vision.crop.CropLayoutView
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.databinding.FragmentCropImageBinding
import com.hms.xmedia.utils.Constant
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CropFragment : BaseFragment<CropFragmentViewModel, FragmentCropImageBinding>() {

    private val viewModel: CropFragmentViewModel by viewModels()

    override fun getFragmentViewModel(): CropFragmentViewModel = viewModel

    private var inputBitMap: Bitmap? = null
    private val args: CropFragmentArgs by navArgs()

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCropImageBinding {
        return FragmentCropImageBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cropLayoutView: CropLayoutView = fragmentViewBinding.cropImageView

        //Input image coming from camera or gallery
        fragmentViewBinding.imgPhoto.visibility = View.INVISIBLE
        inputBitMap = args.inputImage
        cropLayoutView.setImageBitmap(inputBitMap)
        cropLayoutView.visibility = View.VISIBLE

        fragmentViewBinding.btnFlipHorizontally.setOnClickListener {
            fragmentViewBinding.imgPhoto.visibility = View.INVISIBLE
            cropLayoutView.setImageBitmap(inputBitMap)
            cropLayoutView.visibility = View.VISIBLE
            cropLayoutView.flipImageHorizontally()
        }

        fragmentViewBinding.btnFlipVertically.setOnClickListener {
            fragmentViewBinding.imgPhoto.visibility = View.INVISIBLE
            cropLayoutView.setImageBitmap(inputBitMap)
            cropLayoutView.visibility = View.VISIBLE
            cropLayoutView.flipImageVertically()
        }

        fragmentViewBinding.btnRotate.setOnClickListener {
            fragmentViewBinding.imgPhoto.visibility = View.INVISIBLE
            cropLayoutView.setImageBitmap(inputBitMap)
            cropLayoutView.visibility = View.VISIBLE
            cropLayoutView.rotateClockwise()
        }

        fragmentViewBinding.btnDone.setOnClickListener {
            val croppedImage = cropLayoutView.croppedImage
            fragmentViewBinding.imgPhoto.setImageBitmap(croppedImage)
            cropLayoutView.visibility = View.INVISIBLE
            fragmentViewBinding.imgPhoto.visibility = View.VISIBLE
            goPreviousFragmentWithBackStack(croppedImage)
        }

        fragmentViewBinding.rbGroup.setOnCheckedChangeListener { radioGroup, _ ->
            val radioButton = radioGroup.checkedRadioButtonId
            if (radioButton == fragmentViewBinding.rbCircular.id) {
                cropLayoutView.cropShape = CropLayoutView.CropShape.OVAL
            } else {
                cropLayoutView.cropShape = CropLayoutView.CropShape.RECTANGLE
            }
        }
    }

    private fun goPreviousFragmentWithBackStack(resultImage: Bitmap) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            Constant.KEY_AVAILABILITY_CALENDAR_SHOULD_REFRESH.toString(),
            resultImage
        )
        findNavController().popBackStack()
    }
}