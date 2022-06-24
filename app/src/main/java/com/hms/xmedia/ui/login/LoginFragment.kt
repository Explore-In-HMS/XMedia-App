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
package com.hms.xmedia.ui.login

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import com.hms.xmedia.R
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.databinding.FragmentLoginBinding
import com.hms.xmedia.ui.main.MainViewModel
import com.hms.xmedia.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment<LoginViewModel, FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var service: HuaweiIdAuthService

    override fun getFragmentViewModel(): LoginViewModel = viewModel

    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(inflater, container, false)
    }

    private fun signIn() {
        startActivityForResult(service.signInIntent, Constant.loginRequestCode)
    }

    override fun setupUi() {
        super.setupUi()

        fragmentViewBinding.buttonLogin.setOnClickListener {
            signIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.loginRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                mainViewModel.userSignedIn(data)
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }
    }
}