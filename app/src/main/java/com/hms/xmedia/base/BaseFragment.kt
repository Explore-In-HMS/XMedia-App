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
package com.hms.xmedia.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VM : ViewModel, VB : ViewBinding> : Fragment() {
    lateinit var fragmentViewBinding: VB

    companion object {
        const val BASE_TAG = "BaseFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentViewBinding = getFragmentViewBinding(inflater, container)
        return fragmentViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup()
    }

    abstract fun getFragmentViewModel(): VM

    abstract fun getFragmentViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    private fun setup() {
        setupUi()
        setupListeners()
        setupObservers()
    }


    open fun setupListeners() {}
    open fun setupObservers() {}
    open fun setupUi() {}
}