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
package com.hms.xmedia.ui.videoplayer

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hms.xmedia.R
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.custom.CustomDialog
import com.hms.xmedia.custom.DialogType
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.Status
import com.hms.xmedia.databinding.FragmentVideoPlayerBinding
import com.hms.xmedia.utils.Constant
import com.hms.xmedia.utils.PermissionUtil
import com.hms.xmedia.utils.PermissionUtil.launchMultiplePermission
import com.hms.xmedia.utils.PermissionUtil.registerPermission
import com.hms.xmedia.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VideoPlayerFragment : BaseFragment<VideoPlayerViewModel, FragmentVideoPlayerBinding>() {

    private val viewModel: VideoPlayerViewModel by viewModels()

    override fun getFragmentViewModel(): VideoPlayerViewModel = viewModel

    companion object {
        const val TAG = "VideoPlayerFragment"
    }

    private var mediaController: MediaController? = null

    private val storagePermissionForUpload = registerPermission {
        onStoragePermissionForUploadResult(it)
    }

    private val args: VideoPlayerFragmentArgs by navArgs()
    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVideoPlayerBinding {
        return FragmentVideoPlayerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configVideoPlayer()

        fragmentViewBinding.imageViewBackButton.setOnClickListener {
            requireActivity().onBackPressed()
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
            goPreviousFragmentWithBackStack(viewModel.isVideoUploaded)
        }

    }

    private fun goPreviousFragmentWithBackStack(isShouldRefreshCloudProject: Boolean) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            Constant.KEY_SHOULD_REFRESH_PROJECT,
            isShouldRefreshCloudProject
        )
        findNavController().popBackStack()
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
                uploadFile(mediaFile)
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

    private fun uploadFile(mediaFile: MediaFile) {
        lifecycleScope.launch {
            viewModel.uploadFile(mediaFile).collect { resource ->
                when (resource.status) {
                    Status.SUCCESSFUL -> {
                        if (resource.data != true) return@collect
                        fragmentViewBinding.progressBar.visibility = View.INVISIBLE
                        val customDialog = CustomDialog(
                            requireContext(),
                            DialogType.SUCCESS,
                            "Upload Video",
                            "Video successfully uploaded."
                        )
                        customDialog.show()
                    }
                    Status.ERROR -> {
                        val customDialog = CustomDialog(
                            requireContext(),
                            DialogType.ERROR,
                            "Upload Video",
                            resource.error?.errorMessage ?: "Error"
                        )
                        customDialog.show()
                        fragmentViewBinding.progressBar.visibility = View.INVISIBLE
                    }
                    Status.LOADING -> {
                        fragmentViewBinding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun configVideoPlayer() {
        val mediaFile = args.argMediaFile
        val uri: Uri = Uri.parse(mediaFile.path)

        fragmentViewBinding.videoViewLocal.setVideoURI(uri)
        mediaController = MediaController(fragmentViewBinding.root.context)
        fragmentViewBinding.videoViewLocal.setMediaController(mediaController)
        mediaController!!.setAnchorView(fragmentViewBinding.videoViewLocal)

        val fullName = mediaFile.title + "." + mediaFile.fileExtension
        val date = Utils.getDateFromTimeInMillis(mediaFile.fileAddedDate.toLongOrNull() ?: 0L)
        fragmentViewBinding.tvVideoName.text = fullName
        fragmentViewBinding.tvVideoDate.text = date

        fragmentViewBinding.videoViewLocal.requestFocus()
        fragmentViewBinding.videoViewLocal.pause()
    }
}