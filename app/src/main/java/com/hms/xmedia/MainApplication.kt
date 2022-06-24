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
package com.hms.xmedia

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.huawei.hms.audioeditor.common.agc.HAEApplication
import com.huawei.hms.videoeditor.ui.api.MediaApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    companion object {
        const val TAG = "MainApplication"
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        val apiKey = BuildConfig.API_KEY
        HAEApplication.initialize(this)
        HAEApplication.getInstance().apiKey = apiKey
        MediaApplication.getInstance().setApiKey(apiKey)
        MediaApplication.getInstance().setLicenseId("1987")
    }
}