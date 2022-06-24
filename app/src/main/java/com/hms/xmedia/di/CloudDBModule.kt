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
import com.huawei.agconnect.AGCRoutePolicy
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import com.huawei.agconnect.cloud.database.ObjectTypeInfo
import com.hms.xmedia.utils.ObjectTypeInfoHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CloudDBModule {

    @Provides
    @Singleton
    fun provideCloudDbZoneConfig(): CloudDBZoneConfig {
        return CloudDBZoneConfig(
            "XMediaDB",
            CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
            CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
        )
    }

    @Provides
    @Singleton
    fun provideObjectTypeInfoHelper(): ObjectTypeInfo {
        return ObjectTypeInfoHelper.getObjectTypeInfo()
    }

    @Provides
    @Singleton
    fun provideCloudDB(
        @ApplicationContext context: Context,
        objectTypeInfo: ObjectTypeInfo,
        agConnectAuth: AGConnectAuth
    ): AGConnectCloudDB {
        val agConnectOptions =
            AGConnectOptionsBuilder().setRoutePolicy(AGCRoutePolicy.SINGAPORE).build(context)
        val agConnectInstance = AGConnectInstance.buildInstance(agConnectOptions)
        AGConnectCloudDB.initialize(context)
        val cloudDB = AGConnectCloudDB.getInstance(agConnectInstance, agConnectAuth)
        cloudDB.createObjectType(objectTypeInfo)
        return cloudDB
    }
}