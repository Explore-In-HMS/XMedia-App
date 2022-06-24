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
package com.hms.xmedia.ui.projects

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.RecoverableSecurityException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hms.xmedia.R
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.MediaFileType
import com.hms.xmedia.data.model.ProjectSelectedItemType
import com.hms.xmedia.data.model.Status
import com.hms.xmedia.databinding.FragmentProjectsBinding
import com.hms.xmedia.ui.projects.adapter.MediaFileAdapter
import com.hms.xmedia.ui.projects.adapter.ProjectClickListener
import com.hms.xmedia.ui.projects.dialogs.ProjectBottomSheetDialog
import com.hms.xmedia.ui.projects.dialogs.ProjectSelectionDialog
import com.hms.xmedia.ui.projects.dialogs.ProjectSelectionDialogClickListener
import com.hms.xmedia.utils.Constant
import com.hms.xmedia.utils.PermissionUtil
import com.hms.xmedia.utils.PermissionUtil.launchMultiplePermission
import com.hms.xmedia.utils.PermissionUtil.registerPermission
import com.hms.xmedia.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ProjectsFragment : BaseFragment<ProjectsViewModel, FragmentProjectsBinding>() {

    private val viewModel: ProjectsViewModel by viewModels()

    override fun getFragmentViewModel(): ProjectsViewModel = viewModel

    companion object {
        const val TAG = "ProjectsFragment"
    }

    private val storagePermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val storagePermissionForProject = registerPermission {
        onStoragePermissionForProjectResult(it)
    }

    private lateinit var localMediaFileAdapter: MediaFileAdapter
    private lateinit var cloudMediaFileAdapter: MediaFileAdapter

    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProjectsBinding {
        return FragmentProjectsBinding.inflate(inflater, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isStoragePermissionGranted) {
            storagePermissionForProject.launchMultiplePermission(storagePermissions)
        }

        localMediaFileAdapter = MediaFileAdapter(object : ProjectClickListener {
            override fun onProjectItemClicked(mediaFile: MediaFile) {
                when (mediaFile.mediaFileType) {
                    MediaFileType.AUDIO_FILE -> {
                        val action =
                            ProjectsFragmentDirections.actionProjectsFragmentToAudioPlayerFragment(
                                mediaFile
                            )
                        findNavController().navigate(action)
                    }
                    MediaFileType.VIDEO_FILE -> {
                        val action =
                            ProjectsFragmentDirections.actionProjectsFragmentToVideoPlayerFragment(
                                mediaFile
                            )
                        findNavController().navigate(action)
                    }
                    MediaFileType.IMAGE_FILE -> {
                        val action =
                            ProjectsFragmentDirections.actionProjectsFragmentToImageScreenFragment(
                                mediaFile
                            )
                        findNavController().navigate(action)
                    }
                }
            }

            override fun onProjectMoreClicked(mediaFile: MediaFile) {
                ProjectBottomSheetDialog { projectSelectedItemType ->
                    when (projectSelectedItemType) {
                        ProjectSelectedItemType.ITEM_SHARE -> {
                            Utils.shareFileLocal(requireContext(), mediaFile)
                        }
                        ProjectSelectedItemType.ITEM_DELETE -> {
                            lifecycleScope.launch {
                                deleteLocalFile(mediaFile)
                            }
                        }
                    }
                }.show(parentFragmentManager, "ProjectBottomSheetDialog")
            }
        })

        cloudMediaFileAdapter = MediaFileAdapter(object : ProjectClickListener {
            override fun onProjectItemClicked(mediaFile: MediaFile) {
                when (mediaFile.mediaFileType) {
                    MediaFileType.AUDIO_FILE -> {
                        val action =
                            ProjectsFragmentDirections.actionProjectsFragmentToAudioPlayerFragment(
                                mediaFile
                            )
                        findNavController().navigate(action)
                    }
                    MediaFileType.VIDEO_FILE -> {
                        val action =
                            ProjectsFragmentDirections.actionProjectsFragmentToVideoPlayerFragment(
                                mediaFile
                            )
                        findNavController().navigate(action)
                    }
                    MediaFileType.IMAGE_FILE -> {
                        val action =
                            ProjectsFragmentDirections.actionProjectsFragmentToImageScreenFragment(
                                mediaFile
                            )
                        findNavController().navigate(action)
                    }
                }
            }

            override fun onProjectMoreClicked(mediaFile: MediaFile) {
                ProjectBottomSheetDialog { projectSelectedItemType ->
                    when (projectSelectedItemType) {
                        ProjectSelectedItemType.ITEM_SHARE -> {
                            Toast.makeText(
                                requireContext(),
                                "You only can share the local files.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        ProjectSelectedItemType.ITEM_DELETE -> {
                            deleteCloudFile(mediaFile)
                        }
                    }
                }.show(parentFragmentManager, "ProjectBottomSheetDialog")
            }

        })


        fragmentViewBinding.recyclerViewMedia.adapter = localMediaFileAdapter
        fragmentViewBinding.recyclerViewCloudMedia.adapter = cloudMediaFileAdapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLocalProjectSelected.collect { isLocalProjectSelected ->
                        if (isLocalProjectSelected) {
                            fragmentViewBinding.frameLocalStorage.visibility = View.VISIBLE
                            fragmentViewBinding.frameCloudStorage.visibility = View.GONE
                        } else {
                            fragmentViewBinding.frameLocalStorage.visibility = View.GONE
                            fragmentViewBinding.frameCloudStorage.visibility = View.VISIBLE
                        }
                    }
                }

                launch {
                    viewModel.localMediaFileList.collect { resources ->
                        when (resources.status) {
                            Status.SUCCESSFUL -> {
                                fragmentViewBinding.includeErrorPage.root.visibility =
                                    View.INVISIBLE
                                if (resources.data.isNullOrEmpty()) {
                                    fragmentViewBinding.includeEmptyLocalStorage.root.visibility =
                                        View.VISIBLE
                                } else {
                                    fragmentViewBinding.includeEmptyLocalStorage.root.visibility =
                                        View.GONE
                                }
                                localMediaFileAdapter.submitList(resources.data)
                                fragmentViewBinding.progressBarLocal.visibility = View.GONE
                            }
                            Status.ERROR -> {
                                fragmentViewBinding.progressBarLocal.visibility = View.GONE
                                val errorMessage = resources.error?.errorMessage
                                fragmentViewBinding.includeErrorPage.tvErrorDesc.text = errorMessage
                                fragmentViewBinding.includeErrorPage.root.visibility = View.VISIBLE

                                fragmentViewBinding.includeErrorPage.btnError.setOnClickListener {
                                    viewModel.getAllCloudMediaFile()
                                    viewModel.getAllLocalMediaFiles()
                                }
                            }
                            Status.LOADING -> {
                                fragmentViewBinding.progressBarLocal.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                launch {
                    viewModel.cloudMediaFileList.collect { resources ->
                        when (resources.status) {
                            Status.SUCCESSFUL -> {
                                if (resources.data.isNullOrEmpty()) {
                                    fragmentViewBinding.includeEmptyCloudStorage.root.visibility =
                                        View.VISIBLE
                                } else {
                                    fragmentViewBinding.includeEmptyCloudStorage.root.visibility =
                                        View.GONE
                                }
                                cloudMediaFileAdapter.submitList(resources.data)
                                fragmentViewBinding.progressBarCloud.visibility = View.GONE
                            }
                            Status.ERROR -> {
                                fragmentViewBinding.progressBarCloud.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    getString(
                                        R.string.projects_an_error_occurred,
                                        "error when getting data from cloud"
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Status.LOADING -> {
                                fragmentViewBinding.progressBarCloud.visibility = View.VISIBLE
                            }
                        }
                    }
                }

            }
        }

        fragmentViewBinding.layoutToolbar.setOnClickListener {
            val projectSelectionDialog = ProjectSelectionDialog(
                requireContext(),
                object : ProjectSelectionDialogClickListener {
                    override fun onLocalStorageCLicked() {
                        fragmentViewBinding.tvTitle.text =
                            getString(R.string.projects_toolbar_on_local)
                        lifecycleScope.launch {
                            viewModel.isLocalProjectSelected.emit(true)
                        }
                    }

                    override fun onCloudStorageClicked() {
                        fragmentViewBinding.tvTitle.text =
                            getString(R.string.projects_toolbar_on_cloud)
                        lifecycleScope.launch {
                            viewModel.isLocalProjectSelected.emit(false)
                        }
                    }

                })
            projectSelectionDialog.show()
        }


        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == RESULT_OK) {
                    fragmentViewBinding.progressBarLocal.visibility = View.INVISIBLE
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.projects_an_error_occurred, ""),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.getAllLocalMediaFiles()
                } else {
                    fragmentViewBinding.progressBarLocal.visibility = View.INVISIBLE
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.projects_an_error_occurred, ""),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        startListenBackStackForCloudRefresh()
    }

    private fun onStoragePermissionForProjectResult(permissionState: PermissionUtil.PermissionState) {
        when (permissionState) {
            PermissionUtil.PermissionState.Denied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_projects),
                    Toast.LENGTH_SHORT
                ).show()
            }
            PermissionUtil.PermissionState.Granted -> {
                viewModel.getAllCloudMediaFile()
                viewModel.getAllLocalMediaFiles()
                viewModel.changeStoragePermissionGrantedStatus(true)
            }
            PermissionUtil.PermissionState.PermanentlyDenied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_projects),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startListenBackStackForCloudRefresh() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(Constant.KEY_SHOULD_REFRESH_PROJECT)
            ?.observe(
                viewLifecycleOwner
            ) { isShouldRefreshCloudProject ->
                if (isShouldRefreshCloudProject) {
                    viewModel.getAllCloudMediaFile()
                }
            }
    }


    suspend fun deleteLocalFile(mediaFile: MediaFile) {
        fragmentViewBinding.progressBarLocal.visibility = View.VISIBLE
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(mediaFile.downloadUri)
            try {
                requireContext().contentResolver.delete(uri, null, null)
                withContext(Dispatchers.Main) {
                    fragmentViewBinding.progressBarCloud.visibility = View.INVISIBLE
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.projects_deleted_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.getAllLocalMediaFiles()
                }
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(
                            requireContext().contentResolver,
                            listOf(uri)
                        ).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }
                intentSender?.let { sender ->
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }

    private fun deleteCloudFile(mediaFile: MediaFile) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteUserFile(mediaFile).collect { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        withContext(Dispatchers.Main) {
                            fragmentViewBinding.progressBarCloud.visibility = View.VISIBLE
                        }
                    }
                    Status.SUCCESSFUL -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.projects_deleted_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            fragmentViewBinding.progressBarCloud.visibility = View.INVISIBLE
                            viewModel.getAllCloudMediaFile()
                        }
                    }
                    Status.ERROR -> {
                        withContext(Dispatchers.Main) {
                            val errorMessage = resource.error?.errorMessage
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.projects_an_error_occurred, errorMessage ?: ""),
                                Toast.LENGTH_SHORT
                            ).show()
                            fragmentViewBinding.progressBarCloud.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
    }
}