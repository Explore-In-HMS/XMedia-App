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
package com.hms.xmedia.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.huawei.agconnect.auth.AGConnectAuth
import com.hms.xmedia.R
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment<SettingsViewModel, FragmentSettingsBinding>() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun getFragmentViewModel(): SettingsViewModel = viewModel

    private val authInstance: AGConnectAuth by lazy {
        AGConnectAuth.getInstance()
    }

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentViewBinding.constraintButtonLogout.setOnClickListener {
            authInstance.signOut()
            findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)
        }

        fragmentViewBinding.constraintButtonAboutUs.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutUsFragment)
        }

        fragmentViewBinding.constraintButtonPrivacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)
        }

        fragmentViewBinding.constraintButtonTermsAndConditions.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_termsAndConditionsFragment)
        }
    }

}