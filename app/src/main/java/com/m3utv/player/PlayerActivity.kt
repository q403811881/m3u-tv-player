package com.m3utv.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.m3utv.player.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var channelName: String = ""
    private var channelUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        channelName = intent.getStringExtra("channel_name") ?: "未知频道"
        channelUrl = intent.getStringExtra("channel_url") ?: ""

        if (channelUrl.isEmpty()) {
            Toast.makeText(this, "播放地址为空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvChannelName.text = channelName
        setupPlayer()
        setupControls()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.playerView.player = exoPlayer
                binding.playerView.setKeepScreenOn(true)

                val mediaItem = MediaItem.fromUri(Uri.parse(channelUrl))
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true

                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> {
                                binding.tvBuffering.visibility = View.VISIBLE
                            }
                            Player.STATE_READY -> {
                                binding.tvBuffering.visibility = View.GONE
                            }
                            Player.STATE_ENDED -> {
                                // Some live streams never end, but handle gracefully
                                showControls()
                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        binding.tvBuffering.visibility = View.GONE
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = "播放出错: ${error.localizedMessage ?: "未知错误"}"
                        Toast.makeText(
                            this@PlayerActivity,
                            "播放失败，请检查地址",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
    }

    private fun setupControls() {
        // Toggle info bar on OK/Enter
        binding.playerView.setOnClickListener { toggleControls() }

        // Back button exits
        binding.btnBack.setOnClickListener { finish() }

        // Retry button
        binding.btnRetry.setOnClickListener {
            binding.tvError.visibility = View.GONE
            binding.tvBuffering.visibility = View.VISIBLE
            player?.seekTo(0)
            player?.prepare()
            player?.playWhenReady = true
            hideControls()
        }
    }

    private fun toggleControls() {
        if (binding.controlsOverlay.visibility == View.VISIBLE) {
            hideControls()
        } else {
            showControls()
        }
    }

    private fun showControls() {
        binding.controlsOverlay.visibility = View.VISIBLE
        binding.btnBack.requestFocus()
        // Auto-hide after 5 seconds
        binding.controlsOverlay.postDelayed({
            hideControls()
        }, 5000)
    }

    private fun hideControls() {
        binding.controlsOverlay.visibility = View.GONE
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle TV remote back button
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (binding.controlsOverlay.visibility == View.VISIBLE) {
                hideControls()
                return true
            }
        }
        // MENU button toggles controls
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_INFO) {
            toggleControls()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
