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
package com.hms.xmedia.ui.imageedit.dialogs

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hms.xmedia.databinding.DialogImageImportBinding

class ImageImportDialog(
    context: Context,
    private val imageImportDialogClickListener: ImageImportDialogClickListener
) : Dialog(context) {

    private lateinit var binding: DialogImageImportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DialogImageImportBinding.inflate(inflater)
        setContentView(binding.root)

        window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width =
                (Resources.getSystem().displayMetrics.widthPixels * WIDTH_PERCENTAGE).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }

        binding.frameCamera.setOnClickListener {
            imageImportDialogClickListener.onCameraClicked()
            dismiss()
        }

        binding.frameGallery.setOnClickListener {
            imageImportDialogClickListener.onGalleryClicked()
            dismiss()
        }

    }

    companion object {
        private const val WIDTH_PERCENTAGE = 0.90
    }
}

interface ImageImportDialogClickListener {
    fun onCameraClicked()
    fun onGalleryClicked()
}