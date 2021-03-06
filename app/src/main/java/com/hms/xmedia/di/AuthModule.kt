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
package com.hms.xmedia.di

import android.content.Context
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import com.hms.xmedia.service.AuthenticationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AuthModule {
    @Provides
    @Singleton
    fun provideHuaweiIdAuthParams(): HuaweiIdAuthParams {
        return HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setAccessToken()
            .createParams()
    }

    @Provides
    @Singleton
    fun provideHuaweiIdAuthService(
        @ApplicationContext context: Context,
        huaweiIdAuthParams: HuaweiIdAuthParams
    ): HuaweiIdAuthService {
        return HuaweiIdAuthManager.getService(context, huaweiIdAuthParams)
    }

    @Provides
    @Singleton
    fun provideAGConnectAuth(): AGConnectAuth {
        return AGConnectAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthenticationService(
        huaweiIdAuthService: HuaweiIdAuthService,
        agConnectAuth: AGConnectAuth
    ): AuthenticationService {
        return AuthenticationService(huaweiIdAuthService, agConnectAuth)
    }
}
