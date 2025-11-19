package com.mobi.nubitalcatel.ui.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.mobi.nubitalcatel.R


class FullscreenVideoActivity : AppCompatActivity() {
    private var exoPlayer: ExoPlayer? = null
    private var playerView: StyledPlayerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_video)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_fullscreen_video)
        playerView = findViewById<StyledPlayerView>(R.id.fullscreen_player_view)
        val btnExit = findViewById<ImageView>(R.id.btn_exit_fullscreen)

        // Get the video URL from intent
        val intent = intent
        val videoUrl = intent.getStringExtra("VIDEO_URL")

        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView!!.setPlayer(exoPlayer)
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer!!.setMediaItem(mediaItem)
        exoPlayer!!.prepare()
        exoPlayer!!.playWhenReady = true

        // Exit Fullscreen on button click
        btnExit.setOnClickListener { v: View? -> finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayer != null) {
            exoPlayer!!.release()
        }
    }
}