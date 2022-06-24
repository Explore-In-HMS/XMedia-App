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

import com.hms.xmedia.data.local.MediaStoreDataSource
import com.hms.xmedia.data.remote.CloudDBDataSource
import com.hms.xmedia.data.remote.CloudStorageDataSource
import com.hms.xmedia.data.repository.CloudRepository
import com.hms.xmedia.data.repository.CloudRepositoryImp
import com.hms.xmedia.data.repository.MediaStoreRepository
import com.hms.xmedia.data.repository.MediaStoreRepositoryImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object RepositoryModule {

    @Provides
    @ActivityRetainedScoped
    fun provideMediaStoreRepository(
        mediaStoreDataSource: MediaStoreDataSource
    ): MediaStoreRepository {
        return MediaStoreRepositoryImp(mediaStoreDataSource)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideCloudRepository(
        cloudDBDataSource: CloudDBDataSource,
        cloudStorageDataSource: CloudStorageDataSource
    ): CloudRepository {
        return CloudRepositoryImp(cloudDBDataSource, cloudStorageDataSource)
    }


}