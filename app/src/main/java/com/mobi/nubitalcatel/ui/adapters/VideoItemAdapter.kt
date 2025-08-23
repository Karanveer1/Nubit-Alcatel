package com.mobi.nubitalcatel.ui.adapters

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobi.nubitalcatel.core.models.NewsVideoPojo
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.databinding.VideoItemBinding

class VideoItemAdapter(
    private val items: List<NewsVideoPojo>,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<VideoItemAdapter.VideoViewHolder>() {

    private val activePlayers = linkedMapOf<Int, ExoPlayer>()
    private var currentlyPlayingPlayer: ExoPlayer? = null
    private var currentlyPlayingPosition = RecyclerView.NO_POSITION

    private var mediaCodecFailureCount = 0
    private var videoPlaybackDisabled = false

    companion object {
        private const val MAX_CONCURRENT_PLAYERS = 3
        private const val MAX_MEDIACODEC_FAILURES = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val item = items[position]
        if (item.action == "banner") {
            holder.bindBanner(item)
        } else {
            holder.bindVideo(item.url, position, item.redirectLink)
        }
    }

    override fun getItemCount() = items.size

    fun playVideoAtPosition(position: Int) {
        // pause others
        activePlayers.forEach { (key, player) ->
            if (key != position && player.isPlaying) {
                player.pause(); player.playWhenReady = false
            }
        }

        currentlyPlayingPosition = position
        (recyclerView.findViewHolderForAdapterPosition(position) as? VideoViewHolder)?.let { vh ->
            vh.playVideo()
            currentlyPlayingPlayer = vh.getExoPlayer()
        }
    }

    fun playVisibleVideo() {
        val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val first = lm.findFirstVisibleItemPosition()
        val last = lm.findLastVisibleItemPosition()
        var mostVisiblePos = RecyclerView.NO_POSITION
        var highest = 0f
        for (i in first..last) {
            val v = lm.findViewByPosition(i) ?: continue
            val vh = recyclerView.findViewHolderForAdapterPosition(i) as? VideoViewHolder ?: continue
            if (vh.isBanner()) continue
            val h = v.height
            val visible = (minOf(v.bottom, recyclerView.height) - maxOf(v.top, 0))
            val percent = visible.toFloat() / h
            if (percent > 0.6f && percent > highest) {
                highest = percent
                mostVisiblePos = i
            }
        }
        if (mostVisiblePos != RecyclerView.NO_POSITION && mostVisiblePos != currentlyPlayingPosition) {
            stopAllExcept(mostVisiblePos)
            currentlyPlayingPosition = mostVisiblePos
            (recyclerView.findViewHolderForAdapterPosition(mostVisiblePos) as? VideoViewHolder)?.playVideo()
        }
    }

    fun pauseAll() {
        activePlayers.values.forEach { it.playWhenReady = false }
        currentlyPlayingPosition = RecyclerView.NO_POSITION
    }

    fun releaseAll() {
        activePlayers.values.forEach {
            try { it.playWhenReady = false; it.stop(); it.release() } catch (_: Exception) {}
        }
        activePlayers.clear()
        currentlyPlayingPlayer = null
        currentlyPlayingPosition = RecyclerView.NO_POSITION
    }

    private fun stopAllExcept(skipPos: Int) {
        activePlayers.forEach { (pos, player) ->
            if (pos != skipPos && player.isPlaying) player.playWhenReady = false
        }
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.getExoPlayer()?.let { player ->
            try { player.playWhenReady = false; player.stop(); player.release() } catch (_: Exception) {}
            activePlayers.values.remove(player)
        }
        holder.resetListeners()
    }

    inner class VideoViewHolder(private val binding: VideoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var exoPlayer: ExoPlayer? = null
        private var videoUrl: String? = null
        private var videoPosition: Int = RecyclerView.NO_POSITION
        private var listenersSet = false
        private var isMuted = false
        private var bannerMode = false

        private val handler = Handler(Looper.getMainLooper())
        private val hidePlayPause = Runnable { binding.btnPlayPause.visibility = View.GONE }

        fun isBanner() = bannerMode

        fun bindBanner(item: NewsVideoPojo) {
            bannerMode = true
            binding.bottomAdvBanner.visibility = View.VISIBLE
            binding.playerView.visibility = View.GONE
            binding.btnMute.visibility = View.GONE
            binding.btnFullScreen.visibility = View.GONE
            binding.btnPlayPause.visibility = View.GONE

            Glide.with(binding.root.context)
                .load(item.url)
                .thumbnail(0.25f)
                .placeholder(R.drawable.placeholder_apps)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .centerCrop()
                .into(binding.bottomAdvBanner)

            binding.root.setOnClickListener {
//                MyUtility.handleItemClick(
//                    binding.root.context, "", item.redirectLink, item.redirectLink,
//                    "News Video", "1", "Live"
//                )
            }
        }

        fun bindVideo(url: String, position: Int, redirectLink: String?) {
            bannerMode = false
            videoUrl = url
            videoPosition = position

            binding.bottomAdvBanner.visibility = View.GONE
            binding.playerView.visibility = View.VISIBLE
            binding.btnFullScreen.visibility = View.VISIBLE
            binding.btnPlayPause.visibility = View.GONE
            binding.btnMute.visibility = View.VISIBLE

            if (videoPlaybackDisabled) {
                Log.w("VideoItemAdapter", "Playback disabled due to codec failures")
                return
            }

            // Show/hide overlay on tap
            binding.playerView.setOnClickListener {
                binding.btnPlayPause.visibility = View.VISIBLE
                handler.removeCallbacks(hidePlayPause)
                handler.postDelayed(hidePlayPause, 2000)
            }

            if (exoPlayer == null) {
                cleanupOldestPlayers()
                val ctx = binding.root.context

                // Renderers with decoder fallback; keep your software-only attempt (best effort)
                val renderersFactory = DefaultRenderersFactory(ctx)
                    .setEnableDecoderFallback(true)
                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                    .setMediaCodecSelector(object : MediaCodecSelector {
                        override fun getDecoderInfos(
                            mimeType: String,
                            requiresSecureDecoder: Boolean,
                            requiresTunnelingDecoder: Boolean
                        ): MutableList<MediaCodecInfo> {
                            val result = mutableListOf<MediaCodecInfo>()
                            val all = MediaCodecUtil.getDecoderInfos(
                                mimeType, requiresSecureDecoder, requiresTunnelingDecoder
                            )
                            for (info in all) if (info.softwareOnly) result.add(info)
                            if (result.isEmpty()) return mutableListOf() // force fallback
                            return result
                        }
                    })

                try {
                    exoPlayer = ExoPlayer.Builder(ctx)
                        .setRenderersFactory(renderersFactory)
                        .build().also { player ->
                            binding.playerView.player = player
                            activePlayers[position] = player
                            Log.d("VideoItemAdapter", "ExoPlayer created @ $position")
                        }
                } catch (e: Exception) {
                    Log.e("VideoItemAdapter", "Player create failed: ${e.message}", e)
                    mediaCodecFailureCount++
                    if (mediaCodecFailureCount >= MAX_MEDIACODEC_FAILURES) videoPlaybackDisabled = true
                    return
                }
            }

            try {
                exoPlayer?.setMediaItem(MediaItem.fromUri(url))
                exoPlayer?.prepare()
                exoPlayer?.playWhenReady = false
            } catch (e: Exception) {
                Log.e("VideoItemAdapter", "Prepare failed @ $position: ${e.message}", e)
                mediaCodecFailureCount++
                if (mediaCodecFailureCount >= MAX_MEDIACODEC_FAILURES) videoPlaybackDisabled = true
                activePlayers.remove(videoPosition)
                return
            }

            if (!listenersSet) {
                binding.btnFullScreen.setOnClickListener {
                    exoPlayer?.pause()
                    binding.btnPlayPause.setImageResource(R.drawable.play_new)
//                    val intent = Intent(binding.root.context, FullscreenVideoActivity::class.java)
//                    intent.putExtra("VIDEO_URL", url)
//                    binding.root.context.startActivity(intent)
                }

                binding.btnMute.setOnClickListener {
                    if (isMuted) {
                        unmutePlayer()
                        binding.btnMute.setImageResource(R.drawable.news_video_unmute)
                    } else {
                        mutePlayer()
                        binding.btnMute.setImageResource(R.drawable.news_video_mute)
                    }
                }

                binding.btnPlayPause.setOnClickListener {
                    exoPlayer?.let { player ->
                        if (player.isPlaying) {
                            player.pause()
                            binding.btnPlayPause.setImageResource(R.drawable.play_new)
                        } else {
                            currentlyPlayingPlayer?.pause()
                            currentlyPlayingPlayer = player
                            currentlyPlayingPosition = position
                            player.play()
                            binding.btnPlayPause.setImageResource(R.drawable.pause_new)
                        }
                    }
                    handler.removeCallbacks(hidePlayPause)
                    handler.postDelayed(hidePlayPause, 2000)
                }

                listenersSet = true
            }
        }

        fun playVideo() {
            val url = videoUrl ?: return
            exoPlayer?.apply {
                if (currentMediaItem == null ||
                    currentMediaItem?.playbackProperties?.uri != Uri.parse(url)
                ) {
                    setMediaItem(MediaItem.fromUri(url))
                    prepare()
                }
                playWhenReady = true
                play()
            }
        }

        fun getExoPlayer(): ExoPlayer? = exoPlayer

        fun resetListeners() { listenersSet = false }

        private fun mutePlayer() { exoPlayer?.volume = 0f; isMuted = true }

        private fun unmutePlayer() {
            exoPlayer?.volume = 1f; isMuted = false
            val am = binding.root.context.getSystemService(AudioManager::class.java)
            val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (cur == 0) am.setStreamVolume(AudioManager.STREAM_MUSIC, max / 2, AudioManager.FLAG_SHOW_UI)
        }

        private fun cleanupOldestPlayers() {
            if (activePlayers.size >= MAX_CONCURRENT_PLAYERS) {
                val oldestKey = activePlayers.keys.firstOrNull() ?: return
                activePlayers.remove(oldestKey)?.let { oldest ->
                    try { oldest.playWhenReady = false; oldest.stop(); oldest.release() } catch (_: Exception) {}
                    Log.d("VideoItemAdapter", "Cleaned oldest player @$oldestKey")
                }
            }
        }
    }
}
