package com.zxn.netease.nimsdk.business.session.viewholder

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.business.session.audio.MessageAudioControl
import com.zxn.netease.nimsdk.common.media.audioplayer.BaseAudioControl.AudioControlListener
import com.zxn.netease.nimsdk.common.media.audioplayer.Playable
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.zxn.netease.nimsdk.common.util.sys.ScreenUtil
import com.zxn.netease.nimsdk.common.util.sys.TimeUtil
import com.zxn.netease.nimsdk.impl.NimUIKitImpl

/**
 * 语音消息展示
 */
class MsgViewHolderAudio(adapter: BaseMultiItemFetchLoadAdapter<*, *>) :
    MsgViewHolderBase(adapter) {

    private lateinit var durationLabel: TextView
    private var containerView: View? = null
    private var unreadIndicator: View? = null
    private lateinit var animationView: ImageView
    private var audioControl: MessageAudioControl? = null

    override val contentResId: Int = R.layout.nim_message_item_audio

    override fun inflateContentView() {
        durationLabel = findViewById(R.id.message_item_audio_duration)
        containerView = findViewById(R.id.message_item_audio_container)
        unreadIndicator = findViewById(R.id.message_item_audio_unread_indicator)
        animationView = findViewById(R.id.message_item_audio_playing_animation)
        animationView?.setBackgroundResource(0)
        audioControl = MessageAudioControl.getInstance(context)
    }

    override fun bindContentView() {
        layoutByDirection()
        refreshStatus()
        controlPlaying()
    }

    override fun onItemClick() {
        if (audioControl != null) {
            var message = this.message!!
            if (message.direct == MsgDirectionEnum.In && message.attachStatus != AttachStatusEnum.transferred) {
                if (message.attachStatus == AttachStatusEnum.fail || message.attachStatus == AttachStatusEnum.def) {
                    NIMClient.getService(MsgService::class.java).downloadAttachment(message, false)
                }
                return
            }
            if (message.status != MsgStatusEnum.read) {
                // 将未读标识去掉,更新数据库
                unreadIndicator!!.visibility = View.GONE
            }
            initPlayAnim() // 设置语音播放动画
            audioControl!!.startPlayAudioDelay(
                CLICK_TO_PLAY_AUDIO_DELAY.toLong(),
                message,
                onPlayListener
            )
            audioControl!!.setPlayNext(
                !NimUIKitImpl.getOptions().disableAutoPlayNextAudio,
                adapter,
                message
            )
        }
    }

    private fun layoutByDirection() {
        if (isReceivedMessage) {
            setGravity(animationView, Gravity.LEFT or Gravity.CENTER_VERTICAL)
            setGravity(durationLabel, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
            containerView!!.setBackgroundResource(NimUIKitImpl.getOptions().messageLeftBackground)
            containerView!!.setPadding(
                ScreenUtil.dip2px(15f),
                ScreenUtil.dip2px(8f),
                ScreenUtil.dip2px(10f),
                ScreenUtil.dip2px(8f)
            )
            animationView!!.setBackgroundResource(R.drawable.nim_audio_animation_list_left)
            durationLabel!!.setTextColor(Color.WHITE)
        } else {
            setGravity(animationView, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
            setGravity(durationLabel, Gravity.LEFT or Gravity.CENTER_VERTICAL)
            unreadIndicator!!.visibility = View.GONE
            containerView!!.setBackgroundResource(NimUIKitImpl.getOptions().messageRightBackground)
            containerView!!.setPadding(
                ScreenUtil.dip2px(10f),
                ScreenUtil.dip2px(8f),
                ScreenUtil.dip2px(15f),
                ScreenUtil.dip2px(8f)
            )
            animationView!!.setBackgroundResource(R.drawable.nim_audio_animation_list_right)
            durationLabel!!.setTextColor(Color.WHITE)
        }
    }

    private fun refreshStatus() { // 消息状态
        var message = this.message!!
        val attachment = message.attachment as AudioAttachment
        val status = message.status
        val attachStatus = message.attachStatus

        // alert button
        if (TextUtils.isEmpty(attachment.path)) {
            if (attachStatus == AttachStatusEnum.fail || status == MsgStatusEnum.fail) {
                alertButton.visibility = View.VISIBLE
            } else {
                alertButton.visibility = View.GONE
            }
        }

        // progress bar indicator
        if (status == MsgStatusEnum.sending || attachStatus == AttachStatusEnum.transferring) {
            progressBar?.visibility = View.VISIBLE
        } else {
            progressBar?.visibility = View.GONE
        }

        // unread indicator
        if (!NimUIKitImpl.getOptions().disableAudioPlayedStatusIcon
            && isReceivedMessage
            && attachStatus == AttachStatusEnum.transferred && status != MsgStatusEnum.read
        ) {
            unreadIndicator!!.visibility = View.VISIBLE
        } else {
            unreadIndicator!!.visibility = View.GONE
        }
    }

    private fun controlPlaying() {
        var message = this.message!!
        val msgAttachment = message.attachment as AudioAttachment
        val duration = msgAttachment.duration
        setAudioBubbleWidth(duration)
        durationLabel!!.tag = message.uuid
        if (!isMessagePlaying(audioControl, message)) {
            if (audioControl!!.audioControlListener != null && audioControl!!.audioControlListener == onPlayListener) {
                audioControl!!.changeAudioControlListener(null)
            }
            updateTime(duration)
            stop()
        } else {
            audioControl!!.changeAudioControlListener(onPlayListener)
            play()
        }
    }

    private fun setAudioBubbleWidth(milliseconds: Long) {
        val seconds = TimeUtil.getSecondsByMilliseconds(milliseconds)
        val currentBubbleWidth =
            calculateBubbleWidth(seconds, NimUIKitImpl.getOptions().audioRecordMaxTime)
        val layoutParams = containerView!!.layoutParams
        layoutParams.width = currentBubbleWidth
        containerView!!.layoutParams = layoutParams
    }

    private fun calculateBubbleWidth(seconds: Long, MAX_TIME: Int): Int {
        val maxAudioBubbleWidth = audioMaxEdge
        val minAudioBubbleWidth = audioMinEdge
        var currentBubbleWidth: Int
        currentBubbleWidth = if (seconds <= 0) {
            minAudioBubbleWidth
        } else if (seconds > 0 && seconds <= MAX_TIME) {
            (((maxAudioBubbleWidth - minAudioBubbleWidth) * (2.0 / Math.PI)
                    * Math.atan(seconds / 10.0)) + minAudioBubbleWidth).toInt()
        } else {
            maxAudioBubbleWidth
        }
        if (currentBubbleWidth < minAudioBubbleWidth) {
            currentBubbleWidth = minAudioBubbleWidth
        } else if (currentBubbleWidth > maxAudioBubbleWidth) {
            currentBubbleWidth = maxAudioBubbleWidth
        }
        return currentBubbleWidth
    }

    private fun updateTime(milliseconds: Long) {
        val seconds = TimeUtil.getSecondsByMilliseconds(milliseconds)
        if (seconds >= 0) {
            durationLabel!!.text = seconds.toString() + "\""
        } else {
            durationLabel!!.text = ""
        }
    }

    protected fun isMessagePlaying(
        audioControl: MessageAudioControl?,
        message: IMMessage?
    ): Boolean {
        return audioControl!!.playingAudio != null && audioControl.playingAudio.isTheSame(message)
    }

    private val onPlayListener: AudioControlListener = object : AudioControlListener {
        override fun updatePlayingProgress(playable: Playable, curPosition: Long) {
            if (!isTheSame(message!!.uuid)) {
                return
            }
            if (curPosition > playable.duration) {
                return
            }
            updateTime(curPosition)
        }

        override fun onAudioControllerReady(playable: Playable) {
            if (!isTheSame(message!!.uuid)) {
                return
            }
            play()
        }

        override fun onEndPlay(playable: Playable) {
            if (!isTheSame(message!!.uuid)) {
                return
            }
            updateTime(playable.duration)
            stop()
        }
    }

    private fun play() {
        if (animationView!!.background is AnimationDrawable) {
            val animation = animationView!!.background as AnimationDrawable
            animation.start()
        }
    }

    private fun stop() {
        if (animationView!!.background is AnimationDrawable) {
            val animation = animationView!!.background as AnimationDrawable
            animation.stop()
            endPlayAnim()
        }
    }

    private fun initPlayAnim() {
        if (isReceivedMessage) {
            animationView!!.setBackgroundResource(R.drawable.nim_audio_animation_list_left)
        } else {
            animationView!!.setBackgroundResource(R.drawable.nim_audio_animation_list_right)
        }
    }

    private fun endPlayAnim() {
        if (isReceivedMessage) {
            animationView!!.setBackgroundResource(R.mipmap.nim_audio_animation_list_left_1)
        } else {
            animationView!!.setBackgroundResource(R.mipmap.nim_audio_animation_list_left_1)
        }
    }

    private fun isTheSame(uuid: String): Boolean {
        val current = durationLabel!!.tag.toString()
        return !TextUtils.isEmpty(uuid) && uuid == current
    }

    override fun leftBackground(): Int {
        return R.drawable.nim_message_left_white_bg
    }

    override fun rightBackground(): Int {
        return R.drawable.nim_message_right_blue_bg
    }

    companion object {
        const val CLICK_TO_PLAY_AUDIO_DELAY = 500
        val audioMaxEdge: Int
            get() = (0.6 * ScreenUtil.screenMin).toInt()
        val audioMinEdge: Int
            get() = (0.1875 * ScreenUtil.screenMin).toInt()
    }
}