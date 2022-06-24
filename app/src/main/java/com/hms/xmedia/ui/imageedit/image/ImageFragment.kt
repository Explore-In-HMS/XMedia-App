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
package com.hms.xmedia.ui.imageedit.image

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.hms.xmedia.R
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.databinding.FragmentImageBinding
import com.hms.xmedia.ui.imageedit.dialogs.ImageImportDialog
import com.hms.xmedia.ui.imageedit.dialogs.ImageImportDialogClickListener
import com.hms.xmedia.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@AndroidEntryPoint
class ImageFragment : BaseFragment<ImageFragmentViewModel, FragmentImageBinding>() {

    private val viewModel: ImageFragmentViewModel by viewModels()

    override fun getFragmentViewModel(): ImageFragmentViewModel = viewModel

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentImageBinding {
        return FragmentImageBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentViewBinding.includeEmptyImage.root.visibility = View.VISIBLE

        viewModel.selectedBitmap.observe(viewLifecycleOwner) {
            if (it != null) {
                fragmentViewBinding.ivUploadedPicture.setImageBitmap(it)
                fragmentViewBinding.includeEmptyImage.root.visibility = View.INVISIBLE
            }
        }


        startListenBackStackEntry()
        fragmentViewBinding.btnAddImage.setOnClickListener {
            val imageImportDialog = ImageImportDialog(
                requireContext(),
                object : ImageImportDialogClickListener {
                    override fun onCameraClicked() {
                        checkCameraPermission()
                    }

                    override fun onGalleryClicked() {
                        pickGallery()
                    }
                })
            imageImportDialog.show()
        }

        fragmentViewBinding.btnSave.setOnClickListener {
            if (viewModel.selectedBitmap.value != null) {
                saveMediaToStorage(viewModel.selectedBitmap.value!!)
                Toast.makeText(activity, "Image Saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Add an image first!", Toast.LENGTH_SHORT).show()
            }
        }

        fragmentViewBinding.btnAddFilter.setOnClickListener {

            if (viewModel.selectedBitmap.value != null) {
                val action = ImageFragmentDirections.actionImageFragmentToFilterFragment(
                    viewModel.selectedBitmap.value!!
                )
                Navigation.findNavController(fragmentViewBinding.root).navigate(action)
            } else {
                Toast.makeText(activity, "Upload an Image to continue", Toast.LENGTH_SHORT).show()
            }
        }

        fragmentViewBinding.btnAddSticker.setOnClickListener {
            if (viewModel.selectedBitmap.value != null) {
                val action = ImageFragmentDirections.actionImageFragmentToStickerFragment(
                    viewModel.selectedBitmap.value!!
                )
                Navigation.findNavController(fragmentViewBinding.root).navigate(action)
            } else {
                Toast.makeText(activity, "Upload an Image to continue", Toast.LENGTH_SHORT).show()
            }
        }

        fragmentViewBinding.btnCrop.setOnClickListener {
            if (viewModel.selectedBitmap.value != null) {
                val action =
                    ImageFragmentDirections.actionImageFragmentToCropFragment(viewModel.selectedBitmap.value!!)
                Navigation.findNavController(fragmentViewBinding.root).navigate(action)
            } else {
                Toast.makeText(activity, "Upload an Image to continue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startListenBackStackEntry() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Bitmap>(
            Constant.KEY_AVAILABILITY_CALENDAR_SHOULD_REFRESH.toString()
        )
            ?.observe(
                viewLifecycleOwner
            ) { resultImage ->
                val image: Bitmap = resultImage
                Glide.with(fragmentViewBinding.root.context)
                    .load(image)
                    .error(R.drawable.ic_broken_image)
                    .into(fragmentViewBinding.ivUploadedPicture)
                viewModel.setSelectedBitmap(image)
            }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                Constant.PERMISSION_REQUEST_CODE_CAMERA
            )
        } else {
            takePhoto()
        }
    }

    private fun takePhoto() {
        val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (activity?.packageManager?.let { it1 -> callCameraIntent.resolveActivity(it1) } != null) {
            startActivityForResult(callCameraIntent, Constant.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "IMG${System.currentTimeMillis()}.jpg"
        val folderName = Constant.FOLDER_NAME_OF_THE_SAVED_IMAGE_FILES

        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context?.contentResolver?.also { resolver ->

                val contentValues = ContentValues().apply {

                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + folderName
                    )
                }

                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + File.separator + folderName
            )
            if (!imagesDir.exists()) imagesDir.mkdir()
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constant.PERMISSION_REQUEST_CODE_CAMERA -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    takePhoto()
                }
            }
        }
    }

    private fun pickGallery() {
        val getPhotoIntent = Intent(Intent.ACTION_GET_CONTENT)
        val mimeTypes = arrayOf("image/jpg", "image/png", "image/jpeg")
        getPhotoIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        getPhotoIntent.type = "image/*"
        getPhotoIntent.addCategory(Intent.CATEGORY_OPENABLE)
        this.startActivityForResult(getPhotoIntent, Constant.REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent != null) {
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    Constant.REQUEST_PICK_IMAGE -> try {
                        val uri: Uri? = intent.data

                        Glide.with(fragmentViewBinding.root.context)
                            .load(uri)
                            .error(R.drawable.ic_broken_image)
                            .placeholder(R.drawable.ic_broken_image)
                            .into(fragmentViewBinding.ivUploadedPicture)

                        fragmentViewBinding.includeEmptyImage.root.visibility = View.INVISIBLE

                        if (uri != null) {
                            viewModel.setSelectedBitmap(
                                MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    uri
                                )
                            )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            if (resultCode == Activity.RESULT_OK && requestCode == Constant.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                val bitmaps = (intent.extras?.get("data") as Bitmap)
                viewModel.setSelectedBitmap(bitmaps)
                fragmentViewBinding.ivUploadedPicture.setImageBitmap(bitmaps)
            }
        }
    }
}