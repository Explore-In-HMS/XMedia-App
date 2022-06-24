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
package com.hms.xmedia.ui.audioplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.huawei.hms.api.bean.HwAudioPlayItem
import com.huawei.hms.audiokit.player.callback.HwAudioConfigCallBack
import com.huawei.hms.audiokit.player.manager.*
import com.hms.xmedia.R
import com.hms.xmedia.base.BaseFragment
import com.hms.xmedia.custom.CustomDialog
import com.hms.xmedia.custom.DialogType
import com.hms.xmedia.data.model.MediaFile
import com.hms.xmedia.data.model.Status
import com.hms.xmedia.databinding.FragmentAudioPlayerBinding
import com.hms.xmedia.ui.main.MainActivity
import com.hms.xmedia.utils.Constant
import com.hms.xmedia.utils.PermissionUtil
import com.hms.xmedia.utils.PermissionUtil.launchMultiplePermission
import com.hms.xmedia.utils.PermissionUtil.registerPermission
import com.hms.xmedia.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AudioPlayerFragment : BaseFragment<AudioPlayerViewModel, FragmentAudioPlayerBinding>() {

    private val viewModel: AudioPlayerViewModel by viewModels()

    override fun getFragmentViewModel(): AudioPlayerViewModel = viewModel

    companion object {
        const val TAG = "AudioPlayerFragment"
    }

    val args: AudioPlayerFragmentArgs by navArgs()

    var audioManager: HwAudioManager? = null
    var audioPlayerManager: HwAudioPlayerManager? = null
    var audioConfigManager: HwAudioConfigManager? = null
    var audioQueueManager: HwAudioQueueManager? = null

    private var mDuration: Long = -1
    private var mTempPosition: Long = -5

    private val storagePermissionForUpload = registerPermission {
        onStoragePermissionForUploadResult(it)
    }


    override fun getFragmentViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAudioPlayerBinding {
        return FragmentAudioPlayerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mediaFile = args.argMediaFile

        if (mediaFile.isLocalFile) {
            fragmentViewBinding.imageViewUpload.visibility = View.VISIBLE
        }

        initAudioPlayer(requireContext())
        startAudioPlayerStatusListener()

        fragmentViewBinding.frameLayoutPlayPause.setOnClickListener {
            if (viewModel.isPlaying) {
                audioPlayerManager?.pause()
            } else {
                audioPlayerManager?.play()
            }
            viewModel.isPlaying = !viewModel.isPlaying
        }

        fragmentViewBinding.frameLayoutForward.setOnClickListener {
            if (!viewModel.isPlaying) return@setOnClickListener
            audioPlayerManager?.let {
                val forwardTime = 10_000L
                val currentPosition = it.offsetTime
                audioPlayerManager?.seekTo((currentPosition + forwardTime).toInt())
            }
        }

        fragmentViewBinding.frameLayoutBackward.setOnClickListener {
            if (!viewModel.isPlaying) return@setOnClickListener
            audioPlayerManager?.let {
                val backwardTime = 10_000L
                val currentPosition = it.offsetTime
                audioPlayerManager?.seekTo((currentPosition - backwardTime).toInt())
            }
        }

        fragmentViewBinding.imageViewBackButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        fragmentViewBinding.imageViewUpload.setOnClickListener {
            storagePermissionForUpload.launchMultiplePermission(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goPreviousFragmentWithBackStack(viewModel.isAudioUploaded)
        }
    }

    private fun goPreviousFragmentWithBackStack(isShouldRefreshCloudProject: Boolean) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            Constant.KEY_SHOULD_REFRESH_PROJECT,
            isShouldRefreshCloudProject
        )
        findNavController().popBackStack()
    }


    private fun startAudioPlayerStatusListener() {
        audioManager?.addPlayerStatusListener(object : HwAudioStatusListener {
            override fun onSongChange(audioPlayItem: HwAudioPlayItem?) {
                audioPlayItem?.let {
                    fragmentViewBinding.tvAudioTitle.text = audioPlayItem.audioTitle
                    fragmentViewBinding.tvAudioArtistName.text = audioPlayItem.singer
                }
            }

            override fun onQueueChanged(infos: MutableList<HwAudioPlayItem>?) {

            }

            override fun onBufferProgress(percent: Int) {

            }

            override fun onPlayProgress(currentPosition: Long, duration: Long) {
                currentPosition.let {
                    val calculatedDuration = Utils.millisecondToString(currentPosition)
                    val remainingTime = Utils.millisecondToString(duration - currentPosition)

                    mDuration = duration
                    mTempPosition = currentPosition
                    fragmentViewBinding.seekBarAudioProgress.max = duration.toInt()
                    fragmentViewBinding.seekBarAudioProgress.setProgress(
                        currentPosition.toInt(),
                        true
                    )
                    fragmentViewBinding.tvCurrentStamp.text = calculatedDuration
                    fragmentViewBinding.tvRemainingStamp.text = remainingTime
                }
            }

            override fun onPlayCompleted(isStopped: Boolean) {

            }

            override fun onPlayError(errorCode: Int, isUserForcePlay: Boolean) {

            }

            override fun onPlayStateChange(isPlaying: Boolean, isBuffering: Boolean) {
                if (isPlaying) {
                    fragmentViewBinding.ivPlayPause.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_audio_pause
                        )
                    )
                } else {
                    fragmentViewBinding.ivPlayPause.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_audio_play
                        )
                    )
                }
                viewModel.isPlaying = isPlaying
            }
        })
    }

    private fun initAudioPlayer(context: Context) {
        val hwAudioPlayerConfig = HwAudioPlayerConfig(context)
        HwAudioManagerFactory.createHwAudioManager(hwAudioPlayerConfig,
            object : HwAudioConfigCallBack {
                override fun onSuccess(hwAudioManager: HwAudioManager?) {
                    try {
                        audioManager = hwAudioManager
                        audioPlayerManager = hwAudioManager?.playerManager
                        audioConfigManager = hwAudioManager?.configManager
                        audioQueueManager = hwAudioManager?.queueManager
                        try {
                            val mediaFile = args.argMediaFile
                            val mediaFileList = mutableListOf(mediaFile)
                            val playList =
                                convertMediaFileToHwAudioPlayItem(
                                    mediaFileList,
                                    mediaFile.isLocalFile
                                )

                            audioPlayerManager?.playList(playList, 0, 0)
                            viewModel.isPlaying = true
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                setCustomNotificationPlayer()
                            }
                        } catch (er: Error) {
                            Log.e(TAG, "Error: ${er.message.toString()}")
                        }

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }

                override fun onError(errorCode: Int) {
                    Log.e(TAG, "initPlayer error: $errorCode")
                }
            })
    }

    fun convertMediaFileToHwAudioPlayItem(
        mediaFileList: List<MediaFile>,
        isLocalFile: Boolean = true
    ): List<HwAudioPlayItem> {
        val playItemList: MutableList<HwAudioPlayItem> = ArrayList()
        mediaFileList.forEach {
            val audioPlayItem = HwAudioPlayItem()
            if (isLocalFile) {
                audioPlayItem.apply {
                    setOnline(0)
                    audioId = it.path.hashCode().toString()
                    singer = it.artist
                    audioTitle = it.title
                    filePath = it.path
                    bigImageURL = it.path
                }
            } else {
                audioPlayItem.apply {
                    setOnline(1)
                    audioId = it.Id
                    singer = "Unknown"
                    audioTitle = it.title
                    filePath = it.downloadUri
                    bigImageURL = null
                    onlinePath = it.downloadUri
                }
            }

            playItemList.add(audioPlayItem)
        }
        viewModel.playlistSize = playItemList.size
        return playItemList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCustomNotificationPlayer() {
        audioConfigManager?.setNotificationFactory { notificationConfig ->
            val notificationBuilder =
                NotificationCompat.Builder(requireContext(), Constant.MUSIC_NOTIFY_CHANNEL_ID_PLAY)
            val remoteViews =
                RemoteViews(requireContext().packageName, R.layout.notification_player)
            val playItem: HwAudioPlayItem = audioQueueManager?.currentPlayItem
                ?: HwAudioPlayItem()
            val notificationChannel = NotificationChannel(
                Constant.MUSIC_NOTIFY_CHANNEL_ID_PLAY,
                "Play",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationBuilder.apply {
                setContent(remoteViews)
                setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                setSmallIcon(R.mipmap.ic_launcher)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                setCustomBigContentView(remoteViews)
                setGroup(Constant.MUSIC_NOTIFY_CHANNEL_ID_PLAY)
                setChannelId(Constant.MUSIC_NOTIFY_CHANNEL_ID_PLAY)
            }

            notificationChannel.enableVibration(false)
            notificationChannel.setSound(null, null)
            (requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                notificationChannel
            )

            remoteViews.setImageViewResource(
                R.id.imageViewPlayPause,
                if (viewModel.isPlaying) R.drawable.ic_audio_pause else R.drawable.ic_audio_play
            )

            remoteViews.setTextViewText(R.id.textViewSong, playItem.audioTitle)
            remoteViews.setTextViewText(R.id.textViewArtist, playItem.singer)
            remoteViews.setImageViewResource(
                R.id.imageViewPrevious,
                R.drawable.ic_audio_skip_previous
            )
            remoteViews.setImageViewResource(R.id.imageViewNext, R.drawable.ic_audio_skip_next)
            remoteViews.setOnClickPendingIntent(
                R.id.imageViewPrevious,
                notificationConfig?.prePendingIntent
            )
            remoteViews.setOnClickPendingIntent(
                R.id.imageViewPlayPause,
                notificationConfig?.playPendingIntent
            )
            remoteViews.setOnClickPendingIntent(
                R.id.imageViewNext,
                notificationConfig?.nextPendingIntent
            )
            remoteViews.setOnClickPendingIntent(R.id.imageViewClose, getCancelPendingIntent())
            remoteViews.setOnClickPendingIntent(R.id.layout_content, getMainIntent())
            notificationBuilder.build()
        }
    }

    private fun getCancelPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            requireContext(), 2, Intent(
                "com.huawei.hms.mediacenter.cancel_notification"
            )
                .setPackage(requireContext().packageName), PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getMainIntent(): PendingIntent {
        return PendingIntent.getActivity(
            requireContext(), 0, Intent("android.intent.action.MAIN")
                .addCategory("android.intent.category.LAUNCHER")
                .setClass(requireContext(), MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED), 0
        )
    }

    private fun onStoragePermissionForUploadResult(state: PermissionUtil.PermissionState) {
        when (state) {
            PermissionUtil.PermissionState.Denied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_upload),
                    Toast.LENGTH_SHORT
                ).show()
            }
            PermissionUtil.PermissionState.Granted -> {
                val mediaFile = args.argMediaFile
                uploadFile(mediaFile)
            }
            PermissionUtil.PermissionState.PermanentlyDenied -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_upload),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun uploadFile(mediaFile: MediaFile) {
        lifecycleScope.launch {
            viewModel.uploadMediaFile(mediaFile).collect { resource ->
                when (resource.status) {
                    Status.SUCCESSFUL -> {
                        if (resource.data != true) return@collect
                        fragmentViewBinding.progressBar.visibility = View.INVISIBLE
                        val customDialog = CustomDialog(
                            requireContext(),
                            DialogType.SUCCESS,
                            "Upload Audio",
                            "Audio successfully uploaded."
                        )
                        customDialog.show()
                    }
                    Status.ERROR -> {
                        val customDialog = CustomDialog(
                            requireContext(),
                            DialogType.ERROR,
                            "Upload Audio",
                            resource.error?.errorMessage ?: "Error"
                        )
                        customDialog.show()
                        fragmentViewBinding.progressBar.visibility = View.INVISIBLE
                    }
                    Status.LOADING -> {
                        fragmentViewBinding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerManager?.stop()
    }

}