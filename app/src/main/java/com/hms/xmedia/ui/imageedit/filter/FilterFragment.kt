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

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.hms.image.vision.ImageVision
import com.huawei.hms.image.vision.ImageVisionImpl
import com.hms.xmedia.BuildConfig
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.databinding.FragmentFilterBinding
import com.hms.xmedia.ui.imageedit.OnItemClickListener
import com.hms.xmedia.utils.Constant
import com.hms.xmedia.utils.ImageFilterStrings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

@AndroidEntryPoint
class FilterFragment : BaseFragment<FilterFragmentViewModel, FragmentFilterBinding>(),
    OnItemClickListener {

    private val viewModel: FilterFragmentViewModel by viewModels()

    override fun getFragmentViewModel(): FilterFragmentViewModel = viewModel

    private lateinit var adapter: FilterFragmentAdapter
    private lateinit var imageVisionAPI: ImageVisionImpl
    var authJson: JSONObject? = null
    private var filteredImageList: ArrayList<Bitmap> = ArrayList()
    var initCode = -1
    private var inputImg: Bitmap? = null
    private val args: FilterFragmentArgs by navArgs()

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFilterBinding {
        return FragmentFilterBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputImg = args.inputImage
        fragmentViewBinding.imgFilteredPhoto.setImageBitmap(inputImg)
        fragmentViewBinding.imgFilteredPhoto.visibility = View.VISIBLE

        initAuthJson()
        initImageVisionAPI(requireContext())

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = FilterFragmentAdapter(filteredImageList, this)
        fragmentViewBinding.rvListFilteredImages.adapter = adapter
        fragmentViewBinding.rvListFilteredImages.setHasFixedSize(true)
        fragmentViewBinding.rvListFilteredImages.layoutManager = layoutManager

        fragmentViewBinding.progressBar.visibility = View.VISIBLE
        startFilterRecyclerView(1.0)

        fragmentViewBinding.btnDone.setOnClickListener {
            goPreviousFragmentWithBackStack(viewModel.imageWithFilter)
        }
    }

    private fun initAuthJson() {
        try {
            authJson = JSONObject().apply {
                put("projectId", BuildConfig.PROJECT_ID)
                put("appId", BuildConfig.APP_ID)
                put("authApiKey", BuildConfig.API_KEY)
                put("clientSecret", BuildConfig.CLIENT_SECRET)
                put("clientId", BuildConfig.CLIENT_ID)
            }
        } catch (e: JSONException) {
            Log.i("Error with authJson", e.toString())
        }
    }

    private fun goPreviousFragmentWithBackStack(resultImage: Bitmap?) {
        resultImage?.let {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                Constant.KEY_AVAILABILITY_CALENDAR_SHOULD_REFRESH.toString(),
                resultImage
            )
        }
        findNavController().popBackStack()
    }

    private fun initImageVisionAPI(context: Context?) {
        imageVisionAPI = ImageVision.getInstance(context)
        imageVisionAPI.setVisionCallBack(object : ImageVision.VisionCallBack {
            override fun onSuccess(successCode: Int) {
                initCode = imageVisionAPI.init(context, authJson)
                Log.d(TAG, "onSuccess: init ImageVisionAPI :$initCode")
            }

            override fun onFailure(errorCode: Int) {
                Log.d(TAG, "onFailure: $errorCode")
            }
        })
    }

    private fun startFilterRecyclerView(intensity: Double) {
        lifecycleScope.launch {
            val jsonObject = JSONObject()
            val taskJson = JSONObject()
            for (filterNumber in 0..24) {
                try {
                    taskJson.put(ImageFilterStrings.intensity, intensity)
                    taskJson.put(ImageFilterStrings.filterType, filterNumber)
                    taskJson.put(ImageFilterStrings.compressRate, 1)
                    jsonObject.put(ImageFilterStrings.requestId, 1)
                    jsonObject.put(ImageFilterStrings.taskJson, taskJson)
                    jsonObject.put(ImageFilterStrings.authJson, authJson)
                } catch (e: JSONException) {
                    Log.e(TAG, "JSONException: ${e.message}")
                }

                val visionResult = withContext(Dispatchers.IO) {
                    imageVisionAPI.getColorFilter(
                        jsonObject,
                        inputImg
                    )
                }
                val image = visionResult.image
                if (image == null) {
                    Log.e(TAG, "Error when filtering")
                }
                filteredImageList.add(image)
            }
            imageVisionAPI.stop()
            adapter.updateList(filteredImageList)
            fragmentViewBinding.progressBar.visibility = View.INVISIBLE
        }
    }

    companion object {
        private const val TAG = "FilterFragment"
    }

    override fun onItemClicked(position: Int) {
        fragmentViewBinding.tvFilterName.text = Constant.filters[position]
        fragmentViewBinding.imgFilteredPhoto.setImageBitmap(filteredImageList[position])
        viewModel.imageWithFilter = filteredImageList[position]
    }
}