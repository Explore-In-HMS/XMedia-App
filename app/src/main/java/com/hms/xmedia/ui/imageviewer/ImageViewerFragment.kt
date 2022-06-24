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
package com.hms.xmedia.ui.imageviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.hms.xmedia.R
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.custom.CustomDialog
import com.hms.xmedia.custom.DialogType
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.Status
import com.hms.xmedia.databinding.FragmentImageViewerBinding
import com.hms.xmedia.utils.Constant
import com.hms.xmedia.utils.PermissionUtil
import com.hms.xmedia.utils.PermissionUtil.launchMultiplePermission
import com.hms.xmedia.utils.PermissionUtil.registerPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ImageViewerFragment : BaseFragment<ImageViewerViewModel, FragmentImageViewerBinding>() {

    private val viewModel: ImageViewerViewModel by viewModels()

    override fun getFragmentViewModel(): ImageViewerViewModel = viewModel

    val args: ImageViewerFragmentArgs by navArgs()

    companion object {
        const val TAG = "ImageViewerFragment"
    }

    private val storagePermissionForUpload = registerPermission {
        onStoragePermissionForUploadResult(it)
    }


    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentImageViewerBinding {
        return FragmentImageViewerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val mediaFile = args.argMediaFile

        if (mediaFile.isLocalFile) {
            fragmentViewBinding.imageViewUpload.visibility = View.VISIBLE
        }

        if (mediaFile.isLocalFile) {
            mediaFile.path.let {
                Glide.with(requireContext())
                    .load(it)
                    .into(fragmentViewBinding.imageViewPhoto)
            }
        } else {
            Glide.with(requireContext())
                .load(mediaFile.downloadUri)
                .into(fragmentViewBinding.imageViewPhoto)
        }



        fragmentViewBinding.tvTitle.text =
            getString(R.string.name_file_and_extension, mediaFile.title, mediaFile.fileExtension)


        fragmentViewBinding.imageViewBackButton.setOnClickListener {
            goPreviousFragmentWithBackStack(viewModel.isImageUploaded)
        }

        fragmentViewBinding.imageViewUpload.setOnClickListener {
            storagePermissionForUpload.launchMultiplePermission(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goPreviousFragmentWithBackStack(viewModel.isImageUploaded)
        }
    }

    private fun onStoragePermissionForUploadResult(state: PermissionUtil.PermissionState) {
        when (state) {
            PermissionUtil.PermissionState.Denied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_upload),
                    Toast.LENGTH_SHORT
                ).show()
            }
            PermissionUtil.PermissionState.Granted -> {
                val mediaFile = args.argMediaFile
                uploadImageToCloud(mediaFile)
            }
            PermissionUtil.PermissionState.PermanentlyDenied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_upload),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun goPreviousFragmentWithBackStack(isShouldRefreshCloudProject: Boolean) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            Constant.KEY_SHOULD_REFRESH_PROJECT,
            isShouldRefreshCloudProject
        )
        findNavController().popBackStack()
    }


    private fun uploadImageToCloud(mediaFile: MediaFile) {
        lifecycleScope.launch {
            viewModel.uploadMediaFile(mediaFile).collect { resource ->
                when (resource.status) {
                    Status.SUCCESSFUL -> {
                        withContext(Dispatchers.Main) {
                            fragmentViewBinding.progressBar.visibility = View.INVISIBLE
                            viewModel.isImageUploaded = true
                            val customDialog = CustomDialog(
                                requireContext(),
                                DialogType.SUCCESS,
                                "Upload Image",
                                "Image successfully uploaded."
                            )
                            customDialog.show()
                        }
                    }
                    Status.ERROR -> {
                        withContext(Dispatchers.Main) {
                            fragmentViewBinding.progressBar.visibility = View.INVISIBLE
                            val customDialog = CustomDialog(
                                requireContext(),
                                DialogType.ERROR,
                                "Upload Image",
                                resource.error?.errorMessage ?: "Error"
                            )
                            customDialog.show()
                        }
                    }
                    Status.LOADING -> {
                        withContext(Dispatchers.Main) {
                            fragmentViewBinding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

}