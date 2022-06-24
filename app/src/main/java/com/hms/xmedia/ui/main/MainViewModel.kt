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
package com.hms.xmedia.ui.main

import android.content.Intent
import com.huawei.agconnect.auth.AGConnectUser
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.hms.xmedia.base.BaseViewModel
import com.hms.xmedia.listener.IServiceListener
import com.hms.xmedia.service.AuthenticationService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val authenticationService: AuthenticationService) :
    BaseViewModel() {

    private var agConnectUser: AGConnectUser? = null

    fun userSignedIn(data: Intent?) {
        authenticationService.getSignedInUser(data, object : IServiceListener<AGConnectUser> {
            override fun onSuccess(successResult: AGConnectUser) {
                agConnectUser = successResult
            }

            override fun onError() {
                agConnectUser = null
            }
        })
    }

    fun userSignedIn(authHuaweiId: AuthHuaweiId) {
        authenticationService.getSignedInUser(
            authHuaweiId,
            object : IServiceListener<AGConnectUser> {
                override fun onSuccess(successResult: AGConnectUser) {
                    agConnectUser = successResult
                }

                override fun onError() {
                    agConnectUser = null
                }
            })
    }

    fun getAgConnectUser() = agConnectUser

}