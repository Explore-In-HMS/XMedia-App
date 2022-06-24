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
package com.hms.xmedia.ui.audioeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.audioeditor.sdk.HAEConstant
import com.hms.xmedia.utils.Constant
import com.hms.xmedia.utils.FileUtils

class AudioEditorUIActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performFileSearch()
    }

    // Select File
    private fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        // Allow multiple selections, press and hold multiple selections
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        // Restrict the selection of audio types.
        intent.type = "audio/*"
        startActivityForResult(intent, Constant.AUDIO_EDITOR_REQUEST_CODE)
    }

    // Receive the return value.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constant.AUDIO_EDITOR_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && data != null) {
                    val uris: MutableList<Uri?> = ArrayList()
                    // Return to when a single file is selected
                    if (data.data != null) {
                        val uri = data.data
                        uris.add(uri)
                    } else {
                        // Multiple Choices
                        val clipData = data.clipData
                        if (clipData != null) {
                            var i = 0
                            while (i < clipData.itemCount) {
                                uris.add(clipData.getItemAt(i).uri)
                                i++
                            }
                        }
                    }
                    handleSelectedAudios(uris)
                }
                finish()
            }
        }
    }

    // Convert the URI to the path we need.
    private fun handleSelectedAudios(uriList: List<Uri?>?) {
        if (uriList == null || uriList.isEmpty()) {
            return
        }
        val audioList = ArrayList<String>()
        for (uri in uriList) {
            val filePath: String? = uri?.let { FileUtils.getRealPath(this, it) }
            if (filePath != null) {
                audioList.add(filePath)
            }
        }
        val intent = Intent()
        intent.putExtra(HAEConstant.AUDIO_PATH_LIST, audioList)
        this.setResult(HAEConstant.RESULT_CODE, intent)
        finish()
    }
}