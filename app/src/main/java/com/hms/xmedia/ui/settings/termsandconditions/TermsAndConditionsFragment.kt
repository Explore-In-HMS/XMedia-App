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
package com.hms.xmedia.ui.settings.termsandconditions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.databinding.FragmentTermsAndConditionsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsAndConditionsFragment :
    BaseFragment<TermsAndConditionsViewModel, FragmentTermsAndConditionsBinding>() {

    private val viewModel: TermsAndConditionsViewModel by viewModels()

    override fun getFragmentViewModel(): TermsAndConditionsViewModel = viewModel

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTermsAndConditionsBinding {
        return FragmentTermsAndConditionsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentViewBinding.imageViewBackButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}