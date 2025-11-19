package com.mobi.nubitalcatel.ui.adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.NewsVideoPojo
import com.mobi.nubitalcatel.databinding.VideoItemBinding
import com.mobi.nubitalcatel.ui.activity.FullscreenVideoActivity
import com.mobi.nubitalcatel.ui.activity.WebviewActivity
import com.mobi.nubitalcatel.utils.CommonMethods.Companion.ACTION_STOP_AUDIO

class VideoItemAdapter(
    private val items: List<NewsVideoPojo>,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<VideoItemAdapter.VideoViewHolder>() {

    private val activePlayers = linkedMapOf<Int, ExoPlayer>()
    private var currentlyPlayingPlayer: ExoPlayer? = null
    private var currentlyPlayingPosition = RecyclerView.NO_POSITION
    private var mediaCodecFailureCount = 0
    private var videoPlaybackDisabled = false
    private var isHandlingScrollJump = false // Flag to prevent multiple jumps
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    // When scrolling stops, play only the visible video
                    playVisibleVideo()
                    handleEndlessScroll(recyclerView)
                }
                RecyclerView.SCROLL_STATE_DRAGGING,
                RecyclerView.SCROLL_STATE_SETTLING -> {
                    // Pause all videos while scrolling
                    pauseAllVisibleVideos(recyclerView)
                }
            }
        }
        
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // Pause all visible videos during scrolling
            pauseAllVisibleVideos(recyclerView)
            // Don't handle endless scroll during active scrolling to avoid position jumps
        }
    }
    
    private fun handleEndlessScroll(recyclerView: RecyclerView) {
        if (items.isEmpty() || isHandlingScrollJump) return
        
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        
        if (firstVisiblePosition < 0) return
        
        val totalItems = items.size * 1000
        val centerStart = items.size * 500
        val bufferSize = items.size * 3 // Increased buffer to avoid frequent jumps
        
        // If we're near the end (within bufferSize from the end), jump to equivalent position near center
        if (firstVisiblePosition > totalItems - bufferSize) {
            isHandlingScrollJump = true
            val offset = firstVisiblePosition - (totalItems - bufferSize)
            val targetPosition = centerStart + offset
            
            recyclerView.post {
                // Pause all before jumping
                pauseAllVisibleVideos(recyclerView)
                recyclerView.scrollToPosition(targetPosition)
                
                // Wait for layout to complete before playing video
                recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        recyclerView.postDelayed({
                            isHandlingScrollJump = false
                            playVisibleVideo()
                        }, 150) // Delay to ensure all views are properly bound
                    }
                })
            }
        }
        // If we're near the beginning (within bufferSize from start), jump to equivalent position near center
        else if (firstVisiblePosition < bufferSize) {
            isHandlingScrollJump = true
            val offset = firstVisiblePosition
            val targetPosition = centerStart + offset
            
            recyclerView.post {
                // Pause all before jumping
                pauseAllVisibleVideos(recyclerView)
                recyclerView.scrollToPosition(targetPosition)
                
                // Wait for layout to complete before playing video
                recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        recyclerView.postDelayed({
                            isHandlingScrollJump = false
                            playVisibleVideo()
                        }, 150) // Delay to ensure all views are properly bound
                    }
                })
            }
        }
    }
    private lateinit var context: Context

    private val stopAudioReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_STOP_AUDIO) {
                try {
                    releaseAll()
                } catch (e: Exception) {
                    Log.e("VideoItemAdapter", "Error releasing all players on broadcast: ${e.message}")
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
        val filter = IntentFilter(ACTION_STOP_AUDIO)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(stopAudioReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(stopAudioReceiver, filter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        try {
            context.unregisterReceiver(stopAudioReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        recyclerView.addOnScrollListener(scrollListener)
    }

    companion object {
        private const val MAX_CONCURRENT_PLAYERS = 3
        private const val MAX_MEDIACODEC_FAILURES = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        // Use modulo to create endless scroll effect
        val actualPosition = position % items.size
        val item = items[actualPosition]
        if (item.action == "banner") {
            holder.bindBanner(item)
        } else {
            // Store actual position for player management
            holder.bindVideo(item.url, actualPosition, item.redirectLink)
        }
    }

    override fun getItemCount(): Int {
        // Return a large multiplier of items to enable endless scrolling
        // Using a multiplier (like 1000) allows smooth scrolling in both directions
        return if (items.isEmpty()) 0 else items.size * 1000
    }
    
    // Helper function to get actual position from adapter position
    private fun getActualPosition(adapterPosition: Int): Int {
        return if (items.isEmpty()) 0 else adapterPosition % items.size
    }
    
    // Get the center position for initial scroll
    fun getCenterPosition(): Int {
        return if (items.isEmpty()) 0 else (items.size * 500)
    }

    fun playVideoAtPosition(position: Int) {
        // Convert adapter position to actual position
        val actualPosition = getActualPosition(position)
        
        // Pause all other players
        activePlayers.forEach { (key, player) ->
            if (key != actualPosition && player.isPlaying) {
                player.playWhenReady = false
                player.pause()
            }
        }

        currentlyPlayingPosition = actualPosition
        // Find view holder by actual position - need to search through all view holders
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val viewHolder = recyclerView.getChildViewHolder(child) as? VideoViewHolder
            if (viewHolder != null && getActualPosition(viewHolder.adapterPosition) == actualPosition) {
                viewHolder.playVideo()
                currentlyPlayingPlayer = viewHolder.getExoPlayer()
                break
            }
        }
    }

    fun playVisibleVideo() {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        if (firstVisiblePosition < 0 || lastVisiblePosition < 0) return

        // First, pause all visible videos to ensure clean state
        pauseAllVisibleVideos(recyclerView)

        var mostVisiblePosition = RecyclerView.NO_POSITION
        var highestVisibility = 0f
        var mostVisibleViewHolder: VideoViewHolder? = null

        for (i in firstVisiblePosition..lastVisiblePosition) {
            if (i < 0) continue
            val view = layoutManager.findViewByPosition(i) ?: continue
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? VideoViewHolder ?: continue
            if (viewHolder.isBanner()) continue

            val viewHeight = view.height
            if (viewHeight <= 0) continue
            
            val visibleHeight = minOf(view.bottom, recyclerView.height) - maxOf(view.top, 0)
            val visiblePercentage = visibleHeight.toFloat() / viewHeight

            if (visiblePercentage > highestVisibility && visiblePercentage > 0.6f) {
                highestVisibility = visiblePercentage
                mostVisiblePosition = getActualPosition(i)
                mostVisibleViewHolder = viewHolder
            }
        }

        // Play only the most visible video
        if (mostVisiblePosition != RecyclerView.NO_POSITION && mostVisibleViewHolder != null) {
            // Ensure all other players are stopped
            stopAllExcept(mostVisiblePosition)
            
            // Play the most visible video
            currentlyPlayingPosition = mostVisiblePosition
            mostVisibleViewHolder.playVideo()
            currentlyPlayingPlayer = mostVisibleViewHolder.getExoPlayer()
        }
    }

    fun pauseAll() {
        activePlayers.values.forEach { player ->
            try {
                player.playWhenReady = false
                player.pause()
            } catch (e: Exception) {
                Log.e("VideoItemAdapter", "Error pausing player: ${e.message}")
            }
        }
        currentlyPlayingPosition = RecyclerView.NO_POSITION
        currentlyPlayingPlayer = null
    }
    
    // Pause all visible videos in the RecyclerView
    private fun pauseAllVisibleVideos(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()
        
        // Pause all players in activePlayers first
        pauseAll()
        
        // Also pause any visible view holders that might have players
        for (i in firstVisible..lastVisible) {
            if (i < 0) continue
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? VideoViewHolder
            viewHolder?.getExoPlayer()?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.playWhenReady = false
                        player.pause()
                    } else {

                    }
                } catch (e: Exception) {
                    Log.e("VideoItemAdapter", "Error pausing visible player: ${e.message}")
                }
            }
        }
    }

    fun releaseAll() {
        activePlayers.forEach { (_, player) ->
            try {
                player.playWhenReady = false
                player.stop()
                player.release()
            } catch (e: Exception) {
                Log.e("VideoItemAdapter", "Error releasing player: ${e.message}")
            }
        }
        activePlayers.clear()
        currentlyPlayingPlayer = null
        currentlyPlayingPosition = RecyclerView.NO_POSITION
        Log.d("VideoItemAdapter", "All players released successfully")
    }

    private fun stopAllExcept(skipPos: Int) {
        // Stop all players in activePlayers except the one at skipPos
        activePlayers.forEach { (pos, player) ->
            if (pos != skipPos) {
                try {
                    if (player.isPlaying) {
                        player.playWhenReady = false
                        player.pause()
                    }
                } catch (e: Exception) {
                    Log.e("VideoItemAdapter", "Error stopping player: ${e.message}")
                }
            }
        }
        
        // Also stop any visible view holders that might have players
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()
        
        for (i in firstVisible..lastVisible) {
            if (i < 0) continue
            val actualPos = getActualPosition(i)
            if (actualPos != skipPos) {
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? VideoViewHolder
                viewHolder?.getExoPlayer()?.let { player ->
                    try {
                        if (player.isPlaying) {
                            player.playWhenReady = false
                            player.pause()
                        } else {

                        }
                    } catch (e: Exception) {
                        Log.e("VideoItemAdapter", "Error stopping visible player: ${e.message}")
                    }
                }
            }
        }
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.getExoPlayer()?.let { player ->
            try {
                val actualPosition = getActualPosition(holder.adapterPosition)
                // Only pause, don't release - player will be reused
                player.playWhenReady = false
                player.pause()
                // Player will be detached in resetPlayer()
                // Don't remove from activePlayers - we want to reuse players
                if (actualPosition == currentlyPlayingPosition) {
                    currentlyPlayingPlayer = null
                    currentlyPlayingPosition = RecyclerView.NO_POSITION
                } else {

                }
            } catch (e: Exception) {
                Log.e("VideoItemAdapter", "Error pausing player on recycle: ${e.message}")
            }
        }
        // Reset the exoPlayer reference in holder to null so it can be reassigned on rebind
        holder.resetPlayer()
        // Reset listeners flag so they can be set up again when rebound
        holder.listenersSet = false
    }

    private fun cleanupOldestPlayers() {
        if (activePlayers.size >= MAX_CONCURRENT_PLAYERS) {
            activePlayers.keys.firstOrNull()?.let { oldestKey ->
                activePlayers.remove(oldestKey)?.let { oldest ->
                    try {
                        oldest.playWhenReady = false
                        oldest.stop()
                        oldest.release()
                        Log.d("VideoItemAdapter", "Cleaned oldest player @$oldestKey")
                    } catch (e: Exception) {
                        Log.e("VideoItemAdapter", "Error cleaning up oldest player: ${e.message}")
                    }
                }
            }
        }
    }

    inner class VideoViewHolder(private val binding: VideoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var exoPlayer: ExoPlayer? = null
        private var videoUrl: String? = null
        private var videoPosition: Int = RecyclerView.NO_POSITION
        var listenersSet = false
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
                .error(R.drawable.placeholder_you_tube)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .centerCrop()
                .into(binding.bottomAdvBanner)

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, WebviewActivity::class.java)
                intent.putExtra("url", item.redirectLink)
                intent.putExtra("title", item.title)
                binding.root.context.startActivity(intent)
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

            // Detach any existing player from this view holder first
            if (exoPlayer != null && binding.playerView.player == exoPlayer) {
                binding.playerView.player = null
            }

            // Try to reuse existing player for this position, or create new one
            val existingPlayer = activePlayers[position]
            if (existingPlayer != null && existingPlayer.playbackState != ExoPlayer.STATE_IDLE) {
                // Reuse existing player for this position
                exoPlayer = existingPlayer
                // Ensure player is attached to playerView
                if (binding.playerView.player != exoPlayer) {
                    binding.playerView.player = exoPlayer
                }
            } else {
                // Create new player
                cleanupOldestPlayers()
                val context = binding.root.context

                val renderersFactory = DefaultRenderersFactory(context)
                    .setEnableDecoderFallback(true)
                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                    .setMediaCodecSelector { mimeType, _, _ ->
                        val decoders = MediaCodecUtil.getDecoderInfos(mimeType, false, false)
                        decoders.filter { it.softwareOnly }.ifEmpty {
                            Log.w("VideoItemAdapter", "No software decoders for $mimeType")
                            emptyList()
                        }
                    }

                try {
                    exoPlayer = ExoPlayer.Builder(context)
                        .setRenderersFactory(renderersFactory)
                        .build().also { player ->
                            binding.playerView.player = player
                            // Use actual position for player tracking
                            activePlayers[position] = player
                            Log.d("VideoItemAdapter", "ExoPlayer created @ actual position $position")
                        }
                } catch (e: Exception) {
                    Log.e("VideoItemAdapter", "Player create failed: ${e.message}", e)
                    mediaCodecFailureCount++
                    if (mediaCodecFailureCount >= MAX_MEDIACODEC_FAILURES) {
                        videoPlaybackDisabled = true
                        Log.e("VideoItemAdapter", "Playback disabled after $mediaCodecFailureCount failures")
                    }
                    return
                }
            }

            // Always ensure player is attached to playerView
            if (binding.playerView.player != exoPlayer) {
                binding.playerView.player = exoPlayer
            }

            // Always set up media item - check if it needs to be updated
            try {
                val currentUri = exoPlayer?.currentMediaItem?.playbackProperties?.uri
                val newUri = Uri.parse(url)
                
                // Always set media item if URL is different or player is in bad state
                if (currentUri != newUri || exoPlayer?.currentMediaItem == null || 
                    exoPlayer?.playbackState == ExoPlayer.STATE_IDLE) {
                    exoPlayer?.stop() // Stop current playback before changing media
                    exoPlayer?.clearMediaItems() // Clear any existing media items
                    exoPlayer?.setMediaItem(MediaItem.fromUri(newUri))
                    exoPlayer?.prepare()
                    Log.d("VideoItemAdapter", "Media item set for position $position: $url")
                }
                exoPlayer?.playWhenReady = false
            } catch (e: Exception) {
                Log.e("VideoItemAdapter", "Prepare failed @ $position: ${e.message}", e)
                mediaCodecFailureCount++
                if (mediaCodecFailureCount >= MAX_MEDIACODEC_FAILURES) {
                    videoPlaybackDisabled = true
                    Log.e("VideoItemAdapter", "Playback disabled after $mediaCodecFailureCount failures")
                }
                // Remove and recreate player if it's in bad state
                activePlayers.remove(videoPosition)
                exoPlayer = null
                return
            }

            // Set up listeners
            if (!listenersSet) {
                binding.btnFullScreen.setOnClickListener {
                    exoPlayer?.pause()
                    binding.btnPlayPause.setImageResource(R.drawable.play_new)
                     val intent = Intent(binding.root.context, FullscreenVideoActivity::class.java)
                     intent.putExtra("VIDEO_URL", url)
                     binding.root.context.startActivity(intent)
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

                binding.playerView.setOnClickListener {
                    binding.btnPlayPause.visibility = View.VISIBLE
                    handler.removeCallbacks(hidePlayPause)
                    handler.postDelayed(hidePlayPause, 2000)
                }

                binding.btnPlayPause.setOnClickListener {
                    exoPlayer?.let { player ->
                        if (player.isPlaying) {
                            player.pause()
                            binding.btnPlayPause.setImageResource(R.drawable.play_new)
                        } else {
                            if (currentlyPlayingPlayer != null && currentlyPlayingPosition != videoPosition) {
                                currentlyPlayingPlayer?.pause()
                            }
                            currentlyPlayingPlayer = player
                            currentlyPlayingPosition = videoPosition
                            player.playWhenReady = true
                            player.play()
                            binding.btnPlayPause.setImageResource(R.drawable.pause_new)
                        }
                        handler.removeCallbacks(hidePlayPause)
                        handler.postDelayed(hidePlayPause, 2000)
                    }
                }

                listenersSet = true
            }
        }

        fun playVideo() {
            val url = videoUrl ?: return
            exoPlayer?.apply {
                try {
                    val currentUri = currentMediaItem?.playbackProperties?.uri
                    val targetUri = Uri.parse(url)
                    
                    // Ensure media item is set correctly
                    if (currentUri != targetUri || currentMediaItem == null || 
                        playbackState == ExoPlayer.STATE_IDLE) {
                        stop()
                        clearMediaItems()
                        setMediaItem(MediaItem.fromUri(targetUri))
                        prepare()
                    }
                    
                    // Ensure player is ready before playing
                    if (playbackState == ExoPlayer.STATE_READY || playbackState == ExoPlayer.STATE_BUFFERING) {
                        playWhenReady = true
                        play()
                        Log.d("VideoItemAdapter", "Video started playing @ $videoPosition")
                    } else {
                        // Wait for player to be ready
                        playWhenReady = true
                        Log.d("VideoItemAdapter", "Video queued to play @ $videoPosition, waiting for ready state")
                    }
                } catch (e: Exception) {
                    Log.e("VideoItemAdapter", "Error playing video @ $videoPosition: ${e.message}", e)
                }
            }
        }

        fun getExoPlayer(): ExoPlayer? = exoPlayer
        
        fun resetPlayer() {
            // Detach player from view before resetting
            if (exoPlayer != null && binding.playerView.player == exoPlayer) {
                binding.playerView.player = null
            }
            exoPlayer = null
        }

        private fun mutePlayer() {
            exoPlayer?.volume = 0f
            isMuted = true
        }

        private fun unmutePlayer() {
            exoPlayer?.volume = 1f
            isMuted = false
            val audioManager = binding.root.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (currentVolume == 0) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, AudioManager.FLAG_SHOW_UI)
            }
        }
    }
}

//class VideoItemAdapter(
//    private val items: List<NewsVideoPojo>,
//    private val recyclerView: RecyclerView
//) : RecyclerView.Adapter<VideoItemAdapter.VideoViewHolder>() {
//
//    private val activePlayers = linkedMapOf<Int, ExoPlayer>()
//    private var currentlyPlayingPlayer: ExoPlayer? = null
//    private var currentlyPlayingPosition = RecyclerView.NO_POSITION
//
//    private var mediaCodecFailureCount = 0
//    private var videoPlaybackDisabled = false
//
//    companion object {
//        private const val MAX_CONCURRENT_PLAYERS = 3
//        private const val MAX_MEDIACODEC_FAILURES = 3
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
//        val binding = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return VideoViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
//        val item = items[position]
//        if (item.action == "banner") {
//            holder.bindBanner(item)
//        } else {
//            holder.bindVideo(item.url, position, item.redirectLink)
//        }
//    }
//
//    override fun getItemCount() = items.size
//
//    fun playVideoAtPosition(position: Int) {
//        // pause others
//        activePlayers.forEach { (key, player) ->
//            if (key != position && player.isPlaying) {
//                player.pause(); player.playWhenReady = false
//            }
//        }
//
//        currentlyPlayingPosition = position
//        (recyclerView.findViewHolderForAdapterPosition(position) as? VideoViewHolder)?.let { vh ->
//            vh.playVideo()
//            currentlyPlayingPlayer = vh.getExoPlayer()
//        }
//    }
//
//    fun playVisibleVideo() {
//        val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
//        val first = lm.findFirstVisibleItemPosition()
//        val last = lm.findLastVisibleItemPosition()
//        var mostVisiblePos = RecyclerView.NO_POSITION
//        var highest = 0f
//        for (i in first..last) {
//            val v = lm.findViewByPosition(i) ?: continue
//            val vh = recyclerView.findViewHolderForAdapterPosition(i) as? VideoViewHolder ?: continue
//            if (vh.isBanner()) continue
//            val h = v.height
//            val visible = (minOf(v.bottom, recyclerView.height) - maxOf(v.top, 0))
//            val percent = visible.toFloat() / h
//            if (percent > 0.6f && percent > highest) {
//                highest = percent
//                mostVisiblePos = i
//            }
//        }
//        if (mostVisiblePos != RecyclerView.NO_POSITION && mostVisiblePos != currentlyPlayingPosition) {
//            stopAllExcept(mostVisiblePos)
//            currentlyPlayingPosition = mostVisiblePos
//            (recyclerView.findViewHolderForAdapterPosition(mostVisiblePos) as? VideoViewHolder)?.playVideo()
//        }
//    }
//
//    fun pauseAll() {
//        activePlayers.values.forEach { it.playWhenReady = false }
//        currentlyPlayingPosition = RecyclerView.NO_POSITION
//    }
//
//    fun releaseAll() {
//        activePlayers.values.forEach {
//            try { it.playWhenReady = false; it.stop(); it.release() } catch (_: Exception) {}
//        }
//        activePlayers.clear()
//        currentlyPlayingPlayer = null
//        currentlyPlayingPosition = RecyclerView.NO_POSITION
//    }
//
//    private fun stopAllExcept(skipPos: Int) {
//        activePlayers.forEach { (pos, player) ->
//            if (pos != skipPos && player.isPlaying) player.playWhenReady = false
//        }
//    }
//
//    override fun onViewRecycled(holder: VideoViewHolder) {
//        super.onViewRecycled(holder)
//
//        val player = holder.getExoPlayer()
//        if (player != null) {
//            try {
//                player.playWhenReady = false
//                player.stop() // Stops playback
//                player.release() // Frees native resources
//                // Remove from active players map
//                activePlayers.values.remove(player)
//            } catch (e: java.lang.Exception) {
//                Log.e("VideoAdapter", "Error releasing player: " + e.message)
//            }
//        }
//        holder.listenersSet = false
//
////        holder.getExoPlayer()?.let { player ->
////            try { player.playWhenReady = false; player.stop(); player.release() } catch (_: Exception) {}
////            activePlayers.values.remove(player)
////        }
////        holder.resetListeners()
//    }
//
//    inner class VideoViewHolder(private val binding: VideoItemBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        private var exoPlayer: ExoPlayer? = null
//        private var videoUrl: String? = null
//        private var videoPosition: Int = RecyclerView.NO_POSITION
//        var listenersSet = false
//        private var isMuted = false
//        private var bannerMode = false
//
//        private val handler = Handler(Looper.getMainLooper())
//        private val hidePlayPause = Runnable { binding.btnPlayPause.visibility = View.GONE }
//
//        fun isBanner() = bannerMode
//
//        fun bindBanner(item: NewsVideoPojo) {
//            bannerMode = true
//            binding.bottomAdvBanner.visibility = View.VISIBLE
//            binding.playerView.visibility = View.GONE
//            binding.btnMute.visibility = View.GONE
//            binding.btnFullScreen.visibility = View.GONE
//            binding.btnPlayPause.visibility = View.GONE
//
//            Glide.with(binding.root.context)
//                .load(item.url)
//                .thumbnail(0.25f)
//                .placeholder(R.drawable.placeholder_apps)
//                .diskCacheStrategy(DiskCacheStrategy.DATA)
//                .centerCrop()
//                .into(binding.bottomAdvBanner)
//
//            binding.root.setOnClickListener {
////                MyUtility.handleItemClick(
////                    binding.root.context, "", item.redirectLink, item.redirectLink,
////                    "News Video", "1", "Live"
////                )
//            }
//        }
//
//        fun bindVideo(url: String, position: Int, redirectLink: String?) {
//            bannerMode = false
//            videoUrl = url
//            videoPosition = position
//
//            binding.bottomAdvBanner.visibility = View.GONE
//            binding.playerView.visibility = View.VISIBLE
//            binding.btnFullScreen.visibility = View.VISIBLE
//            binding.btnPlayPause.visibility = View.GONE
//            binding.btnMute.visibility = View.VISIBLE
//
//            if (videoPlaybackDisabled) {
//                Log.w("VideoItemAdapter", "Playback disabled due to codec failures")
//                return
//            }
//
//            // Show/hide overlay on tap
//            binding.playerView.setOnClickListener {
//                binding.btnPlayPause.visibility = View.VISIBLE
//                handler.removeCallbacks(hidePlayPause)
//                handler.postDelayed(hidePlayPause, 2000)
//            }
//
//            if (exoPlayer == null) {
//                cleanupOldestPlayers()
//                val ctx = binding.root.context
//
//                // Renderers with decoder fallback; keep your software-only attempt (best effort)
//                val renderersFactory = DefaultRenderersFactory(ctx)
//                    .setEnableDecoderFallback(true)
//                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
//                    .setMediaCodecSelector(object : MediaCodecSelector {
//                        override fun getDecoderInfos(
//                            mimeType: String,
//                            requiresSecureDecoder: Boolean,
//                            requiresTunnelingDecoder: Boolean
//                        ): MutableList<MediaCodecInfo> {
//                            val result = mutableListOf<MediaCodecInfo>()
//                            val all = MediaCodecUtil.getDecoderInfos(
//                                mimeType, requiresSecureDecoder, requiresTunnelingDecoder
//                            )
//                            for (info in all) if (info.softwareOnly) result.add(info)
//                            if (result.isEmpty()) return mutableListOf() // force fallback
//                            return result
//                        }
//                    })
//
//                try {
//                    exoPlayer = ExoPlayer.Builder(ctx)
//                        .setRenderersFactory(renderersFactory)
//                        .build().also { player ->
//                            binding.playerView.player = player
//                            activePlayers[position] = player
//                            Log.d("VideoItemAdapter", "ExoPlayer created @ $position")
//                        }
//                } catch (e: Exception) {
//                    Log.e("VideoItemAdapter", "Player create failed: ${e.message}", e)
//                    mediaCodecFailureCount++
//                    if (mediaCodecFailureCount >= MAX_MEDIACODEC_FAILURES) videoPlaybackDisabled = true
//                    return
//                }
//            }
//
//            try {
//                exoPlayer?.setMediaItem(MediaItem.fromUri(url))
//                exoPlayer?.prepare()
//                exoPlayer?.playWhenReady = false
//            } catch (e: Exception) {
//                Log.e("VideoItemAdapter", "Prepare failed @ $position: ${e.message}", e)
//                mediaCodecFailureCount++
//                if (mediaCodecFailureCount >= MAX_MEDIACODEC_FAILURES) videoPlaybackDisabled = true
//                activePlayers.remove(videoPosition)
//                return
//            }
//
//            if (!listenersSet) {
//                binding.btnFullScreen.setOnClickListener {
//                    exoPlayer?.pause()
//                    binding.btnPlayPause.setImageResource(R.drawable.play_new)
////                    val intent = Intent(binding.root.context, FullscreenVideoActivity::class.java)
////                    intent.putExtra("VIDEO_URL", url)
////                    binding.root.context.startActivity(intent)
//                }
//
//                binding.btnMute.setOnClickListener {
//                    if (isMuted) {
//                        unmutePlayer()
//                        binding.btnMute.setImageResource(R.drawable.news_video_unmute)
//                    } else {
//                        mutePlayer()
//                        binding.btnMute.setImageResource(R.drawable.news_video_mute)
//                    }
//                }
//
//                binding.btnPlayPause.setOnClickListener {
//                    exoPlayer?.let { player ->
//                        if (player.isPlaying) {
//                            player.pause()
//                            binding.btnPlayPause.setImageResource(R.drawable.play_new)
//                        } else {
//                            currentlyPlayingPlayer?.pause()
//                            currentlyPlayingPlayer = player
//                            currentlyPlayingPosition = position
//                            player.play()
//                            binding.btnPlayPause.setImageResource(R.drawable.pause_new)
//                        }
//                    }
//                    handler.removeCallbacks(hidePlayPause)
//                    handler.postDelayed(hidePlayPause, 2000)
//                }
//
//                listenersSet = true
//            }
//        }
//
//        fun playVideo() {
//            val url = videoUrl ?: return
//            exoPlayer?.apply {
//                if (currentMediaItem == null ||
//                    currentMediaItem?.playbackProperties?.uri != Uri.parse(url)
//                ) {
//                    setMediaItem(MediaItem.fromUri(url))
//                    prepare()
//                }
//                playWhenReady = true
//                play()
//            }
//        }
//
//        fun getExoPlayer(): ExoPlayer? = exoPlayer
//
//        fun resetListeners() { listenersSet = false }
//
//        private fun mutePlayer() { exoPlayer?.volume = 0f; isMuted = true }
//
//        private fun unmutePlayer() {
//            exoPlayer?.volume = 1f; isMuted = false
//            val am = binding.root.context.getSystemService(AudioManager::class.java)
//            val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
//            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
//            if (cur == 0) am.setStreamVolume(AudioManager.STREAM_MUSIC, max / 2, AudioManager.FLAG_SHOW_UI)
//        }
//
//        private fun cleanupOldestPlayers() {
//            if (activePlayers.size >= MAX_CONCURRENT_PLAYERS) {
//                val oldestKey = activePlayers.keys.firstOrNull() ?: return
//                activePlayers.remove(oldestKey)?.let { oldest ->
//                    try { oldest.playWhenReady = false; oldest.stop(); oldest.release() } catch (_: Exception) {}
//                    Log.d("VideoItemAdapter", "Cleaned oldest player @$oldestKey")
//                }
//            }
//        }
//    }
//}

