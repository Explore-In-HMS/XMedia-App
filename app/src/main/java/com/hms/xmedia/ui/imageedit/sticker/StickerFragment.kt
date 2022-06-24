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

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.databinding.FragmentStickerBinding
import com.hms.xmedia.ui.imageedit.OnItemClickListener
import com.hms.xmedia.utils.Constant
import com.hms.xmedia.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StickerFragment : BaseFragment<StickerFragmentViewModel, FragmentStickerBinding>(),
    OnItemClickListener {

    private val viewModel: StickerFragmentViewModel by viewModels()

    override fun getFragmentViewModel(): StickerFragmentViewModel = viewModel

    private lateinit var adapter: StickerFragmentAdapter
    private val fileList: ArrayList<String> = ArrayList()
    var rootPath = ""
    private val args: StickerFragmentArgs by navArgs()

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStickerBinding {
        return FragmentStickerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for (num in 0..15) {
            fileList.add(rootPath + "sticker1/" + "sticker_" + (num + 1) + "_editable.png")
        }

        fragmentViewBinding.imgPhotoSticker.setImageBitmap(args.inputImage)

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = StickerFragmentAdapter(fileList, this)
        fragmentViewBinding.rvListSticker.adapter = adapter
        fragmentViewBinding.rvListSticker.setHasFixedSize(true)
        fragmentViewBinding.rvListSticker.layoutManager = layoutManager

        fragmentViewBinding.btnClear.setOnClickListener {
            fragmentViewBinding.stickerContainer.removeAllSticker()
        }

        fragmentViewBinding.btnDone.setOnClickListener {
            val stickerView = fragmentViewBinding.stickerLayout
            stickerView.isDrawingCacheEnabled = true
            stickerView.buildDrawingCache()
            val bm = stickerView.drawingCache
            val bitmap = Bitmap.createScaledBitmap(bm, stickerView.width, stickerView.height, true)

            goPreviousFragmentWithBackStack(bitmap)
        }
    }

    companion object {
        private const val TAG = "ImageEditFragment"
    }

    private fun goPreviousFragmentWithBackStack(resultImage: Bitmap) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            Constant.KEY_AVAILABILITY_CALENDAR_SHOULD_REFRESH.toString(),
            resultImage
        )
        findNavController().popBackStack()
    }

    override fun onItemClicked(position: Int) {
        val mStickerLayout = fragmentViewBinding.stickerContainer
        try {
            rootPath = activity?.baseContext?.filesDir?.path + "/vgmap/"
            FileUtils.copyAssetsFileToDirs(requireContext(), "vgmap", rootPath)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message)
        }
        val resultCode = mStickerLayout.addSticker(
            rootPath + "sticker1",
            "sticker_" + (position + 1) + "_editable.png"
        )
        Log.d(TAG, resultCode.toString())
    }
}