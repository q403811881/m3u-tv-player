package com.m3utv.player

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.m3utv.player.adapter.ChannelAdapter
import com.m3utv.player.databinding.ActivityMainBinding
import com.m3utv.player.model.Channel
import com.m3utv.player.parser.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var channels = emptyList<Channel>()
    private var currentSourceUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        // Load button
        binding.btnLoad.setOnClickListener { loadPlaylist() }

        // Support pressing "OK" on the remote for TV
        binding.etUrlInput.setOnEditorActionListener { _, _, _ ->
            loadPlaylist()
            true
        }

        // RecyclerView setup
        binding.rvChannelList.setHasFixedSize(true)
        binding.rvChannelList.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun loadPlaylist() {
        val url = binding.etUrlInput.text.toString().trim()
        if (url.isEmpty()) {
            Toast.makeText(this, "请输入 M3U 地址或路径", Toast.LENGTH_SHORT).show()
            return
        }

        currentSourceUrl = url
        showLoading(true)

        lifecycleScope.launch {
            try {
                val content = withContext(Dispatchers.IO) { fetchContent(url) }
                if (content.isNullOrBlank()) {
                    showError("无法获取播放列表内容")
                    return@launch
                }

                val parsed = withContext(Dispatchers.Default) {
                    M3UParser.parse(content)
                }

                if (parsed.isEmpty()) {
                    showError("未解析到任何频道，请检查 M3U 地址")
                    return@launch
                }

                channels = parsed
                showChannels(parsed)
                Toast.makeText(
                    this@MainActivity,
                    "已加载 ${parsed.size} 个频道",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                showError("加载失败: ${e.localizedMessage ?: "未知错误"}")
            }
        }
    }

    private fun fetchContent(url: String): String? {
        // Check if it's a local file path
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            val file = File(url)
            if (file.exists()) {
                return file.readText()
            }
            // Try relative to app files
            val appFile = File(filesDir, url)
            if (appFile.exists()) {
                return appFile.readText()
            }
            return null
        }

        // Remote URL
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "M3UTVPlayer/1.0")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return null

        return response.body?.string()
    }

    private fun showChannels(channels: List<Channel>) {
        val adapter = ChannelAdapter(channels) { channel ->
            playChannel(channel)
        }
        binding.rvChannelList.adapter = adapter
        binding.tvEmptyState.visibility = View.GONE
        binding.rvChannelList.visibility = View.VISIBLE

        // Auto-focus first item for TV
        binding.rvChannelList.post {
            val firstChild = binding.rvChannelList.getChildAt(0)
            firstChild?.requestFocus()
        }
    }

    private fun playChannel(channel: Channel) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("channel_name", channel.name)
            putExtra("channel_url", channel.url)
            putExtra("source_url", currentSourceUrl)
        }
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLoad.isEnabled = !show
        binding.btnLoad.text = if (show) "加载中..." else "加载"
    }

    private fun showError(msg: String) {
        showLoading(false)
        binding.tvEmptyState.text = msg
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.rvChannelList.visibility = View.GONE
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
