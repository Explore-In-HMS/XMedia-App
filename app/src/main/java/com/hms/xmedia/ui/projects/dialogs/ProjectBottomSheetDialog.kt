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
package com.hms.xmedia.ui.projects.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hms.xmedia.data.model.ProjectSelectedItemType
import com.hms.xmedia.databinding.BottomSheetProjectBinding

class ProjectBottomSheetDialog(
    private val buttonCallbackSelectedItem: (ProjectSelectedItemType) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetProjectBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetProjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.frameShare.setOnClickListener {
            buttonCallbackSelectedItem.invoke(ProjectSelectedItemType.ITEM_SHARE)
            dismiss()
        }

        binding.frameDelete.setOnClickListener {
            buttonCallbackSelectedItem.invoke(ProjectSelectedItemType.ITEM_DELETE)
            dismiss()
        }
    }

}