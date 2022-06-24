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
package com.hms.xmedia.ui.home

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.huawei.hms.audioeditor.ui.api.AudioEditorLaunchOption
import com.huawei.hms.audioeditor.ui.api.HAEUIManager
import com.huawei.hms.videoeditor.ui.api.MediaApplication
import com.huawei.hms.videoeditor.ui.api.MediaExportCallBack
import com.huawei.hms.videoeditor.ui.api.MediaInfo
import com.huawei.hms.videoeditor.ui.api.VideoEditorLaunchOption
import com.hms.xmedia.R
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.databinding.FragmentHomeBinding
import com.hms.xmedia.utils.Constant
import com.hms.xmedia.utils.PermissionUtil
import com.hms.xmedia.utils.PermissionUtil.launchMultiplePermission
import com.hms.xmedia.utils.PermissionUtil.registerPermission
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    override fun getFragmentViewModel(): HomeViewModel = viewModel

    companion object {
        const val TAG = "HomeFragment"
    }

    private val storagePermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val storagePermissionForVideoEdit = registerPermission {
        onStoragePermissionForVideoEditResult(it)
    }


    private val storagePermissionForAudioEdit = registerPermission {
        onStoragePermissionForAudioEditResult(it)
    }

    private val storagePermissionForImageEdit = registerPermission {
        onStoragePermissionForImageEditResult(it)
    }


    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    override fun setupListeners() {
        super.setupListeners()

        fragmentViewBinding.cardViewSoundEdit.setOnClickListener {
            storagePermissionForAudioEdit.launchMultiplePermission(storagePermissions)
        }

        fragmentViewBinding.cardViewVideoEdit.setOnClickListener {
            storagePermissionForVideoEdit.launchMultiplePermission(storagePermissions)
        }

        fragmentViewBinding.cardViewImageEdit.setOnClickListener {
            storagePermissionForImageEdit.launchMultiplePermission(storagePermissions)
        }

    }

    private fun onStoragePermissionForVideoEditResult(permissionState: PermissionUtil.PermissionState) {
        when (permissionState) {
            PermissionUtil.PermissionState.Denied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_video_editing),
                    Toast.LENGTH_SHORT
                ).show()
            }
            PermissionUtil.PermissionState.Granted -> {
                startVideoEditor()
            }
            PermissionUtil.PermissionState.PermanentlyDenied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_video_editing),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun onStoragePermissionForAudioEditResult(permissionState: PermissionUtil.PermissionState) {
        when (permissionState) {
            PermissionUtil.PermissionState.Denied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_audio_editing),
                    Toast.LENGTH_SHORT
                ).show()
            }
            PermissionUtil.PermissionState.Granted -> {
                startAudioEditor()
            }
            PermissionUtil.PermissionState.PermanentlyDenied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_audio_editing),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun onStoragePermissionForImageEditResult(permissionState: PermissionUtil.PermissionState) {
        when (permissionState) {
            PermissionUtil.PermissionState.Denied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_image_editing),
                    Toast.LENGTH_SHORT
                ).show()
            }
            PermissionUtil.PermissionState.Granted -> {
                startImageEditor()
            }
            PermissionUtil.PermissionState.PermanentlyDenied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_image_editing),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startVideoEditor() {
        val option = VideoEditorLaunchOption.Builder()
            .setStartMode(MediaApplication.START_MODE_IMPORT_FROM_MEDIA)
            .build()
        MediaApplication.getInstance().apply {
            launchEditorActivity(requireContext(), option)
            setOnMediaExportCallBack(videoMediaExportCallback)
        }
    }

    private fun startAudioEditor() {
        val folderName = Constant.FOLDER_NAME_OF_THE_SAVED_AUDIO_FILES
        val exportPath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                .toString() + File.separator + folderName + File.separator
        )
        if (!exportPath.exists()) exportPath.mkdir()
        val audioEditorLaunchOption = AudioEditorLaunchOption.Builder()
        audioEditorLaunchOption.setExportPath(exportPath.path)
        HAEUIManager.getInstance()
            .launchEditorActivity(requireContext(), audioEditorLaunchOption.build())
    }

    private fun startImageEditor() {
        findNavController().navigate(R.id.action_homeFragment_to_imageFragment)
    }

    private val videoMediaExportCallback: MediaExportCallBack = object : MediaExportCallBack {
        override fun onMediaExportSuccess(mediaInfo: MediaInfo) {
            Log.d(TAG, "Export success path:${mediaInfo.mediaPath}")
        }

        override fun onMediaExportFailed(errorCode: Int) {
            Log.d(TAG, "Export failed error code:$errorCode")
        }
    }

}