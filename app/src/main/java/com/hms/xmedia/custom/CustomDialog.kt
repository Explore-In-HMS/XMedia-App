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
package com.hms.xmedia.custom

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.hms.xmedia.R
import com.hms.xmedia.databinding.DialogCustomBinding

class CustomDialog(
    context: Context,
    var dialogType: DialogType,
    var dialogTitle: String,
    var dialogDesc: String
) : Dialog(context) {

    private lateinit var binding: DialogCustomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DialogCustomBinding.inflate(inflater)
        setContentView(binding.root)

        window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width =
                (Resources.getSystem().displayMetrics.widthPixels * WIDTH_PERCENTAGE).toInt()
            val height =
                (Resources.getSystem().displayMetrics.heightPixels * HEIGHT_PERCENTAGE).toInt()
            setLayout(width, height)
            setGravity(Gravity.CENTER)
        }

        binding.tvDialogTitle.text = dialogTitle
        binding.tvDialogDesc.text = dialogDesc

        when (dialogType) {
            DialogType.SUCCESS -> {
                binding.ivDialog.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_dialog_success
                    )
                )
            }
            DialogType.ERROR -> {
                binding.ivDialog.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_video_edit
                    )
                )
            }
            DialogType.INFO -> {
                binding.ivDialog.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_sound_edit
                    )
                )
            }
        }

        binding.btnDialog.setOnClickListener {
            dismiss()
        }

    }

    companion object {
        private const val WIDTH_PERCENTAGE = 0.90
        private const val HEIGHT_PERCENTAGE = 0.50
    }
}