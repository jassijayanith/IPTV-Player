package com.jassijayanith.iptvplayer

import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class Channel(val name: String, val url: String, val country: String = "", val group: String = "")

class MainActivity : ComponentActivity() {
    private val bg = Color.rgb(11, 18, 32)
    private val panel = Color.rgb(18, 29, 48)
    private val accent = Color.rgb(77, 225, 179)
    private val muted = Color.rgb(159, 176, 200)
    private val playlistUrl = "https://iptv-org.github.io/iptv/index.m3u"
    private val channels = mutableListOf<Channel>()
    private val favorites = linkedSetOf<String>()
    private var showFavorites = false
    private var selected: Channel? = null
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private lateinit var country: Spinner
    private lateinit var status: TextView
    private lateinit var nowPlaying: TextView
    private lateinit var favButton: Button
    private val countryCodes = listOf("", "LK", "IN", "GB", "US")
    private val countryNames = listOf("All countries", "Sri Lanka", "India", "United Kingdom", "United States")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        player = ExoPlayer.Builder(this).build()
        buildUi()
        loadPlaylist()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }
        root.addView(TextView(this).apply {
            text = "📺  My IPTV Player"; textSize = 23f
            setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "Live TV • Free public streams"; textSize = 13f; setTextColor(muted)
        })
        nowPlaying = TextView(this).apply {
            text = "Choose a channel"; textSize = 17f
            typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.WHITE)
            setPadding(0, dp(10), 0, dp(6))
        }
        root.addView(nowPlaying)
        playerView = PlayerView(this).apply {
            player = this@MainActivity.player
            useController = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            setBackgroundColor(Color.BLACK)
        }
        root.addView(playerView, LinearLayout.LayoutParams(-1, dp(210)))
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        controls.addView(button("Refresh channels") { loadPlaylist() },
            LinearLayout.LayoutParams(0, dp(44), 1f).apply { marginEnd = dp(6) })
        favButton = button("☆ Favorite") { toggleFavorite() }
        controls.addView(favButton, LinearLayout.LayoutParams(0, dp(44), 1f))
        root.addView(controls)
        status = TextView(this).apply {
            text = "Loading channels…"; textSize = 12f; setTextColor(muted)
            setPadding(0, dp(4), 0, dp(4))
        }
        root.addView(status)
        search = EditText(this).apply {
            hint = "Search channels"; setSingleLine(true); textSize = 15f
            setTextColor(Color.WHITE); setHintTextColor(muted); setPadding(dp(10), 0, dp(10), 0)
            setBackgroundColor(panel)
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(44)))
        country = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, countryNames)
        }
        root.addView(country, LinearLayout.LayoutParams(-1, dp(42)))
        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        tabs.addView(button("All channels") { showFavorites = false; renderChannels() },
            LinearLayout.LayoutParams(0, dp(40), 1f).apply { marginEnd = dp(5) })
        tabs.addView(button("★ Favorites") { showFavorites = true; renderChannels() },
            LinearLayout.LayoutParams(0, dp(40), 1f))
        root.addView(tabs)
        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(5), 0, 0) }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        search.addTextChangedListener(SimpleTextWatcher { renderChannels() })
        country.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { renderChannels() }
        }
        setContentView(root)
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label; isAllCaps = false; textSize = 13f
        setTextColor(if (label.startsWith("Refresh")) bg else Color.WHITE)
        setBackgroundColor(if (label.startsWith("Refresh")) accent else panel)
        setOnClickListener { action() }
    }

    private fun loadPlaylist() {
        status.text = "Loading public channel playlist…"
        lifecycleScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    val conn = (URL(playlistUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 20000; readTimeout = 30000
                        setRequestProperty("User-Agent", "MyIPTVPlayer/2.0")
                    }
                    try { conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() } }
                    finally { conn.disconnect() }
                }
                channels.clear()
                channels.addAll(parseM3U(content))
                status.text = "Loaded ${channels.size} channel entries."
                renderChannels()
            } catch (_: Exception) {
                status.text = "Could not load playlist. Check internet and tap Refresh."
            }
        }
    }

    private fun parseM3U(text: String): List<Channel> {
        val out = mutableListOf<Channel>()
        var name = ""; var code = ""; var group = ""
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            if (line.startsWith("#EXTINF:", true)) {
                name = line.substringAfterLast(',').trim().ifBlank { "Unnamed channel" }
                code = Regex("""tvg-country="([^"]*)"""").find(line)?.groupValues?.get(1)?.split(",", ";")?.firstOrNull()?.trim().orEmpty()
                group = Regex("""group-title="([^"]*)"""").find(line)?.groupValues?.get(1).orEmpty()
            } else if (line.isNotBlank() && !line.startsWith("#") && name.isNotBlank()) {
                out.add(Channel(name, line, code, group))
                name = ""; code = ""; group = ""
            }
        }
        return out
    }

    private fun renderChannels() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val query = if (::search.isInitialized) search.text.toString().trim().lowercase(Locale.ROOT) else ""
        val code = if (::country.isInitialized) countryCodes.getOrElse(country.selectedItemPosition) { "" } else ""
        val filtered = channels.asSequence()
            .filter { query.isEmpty() || it.name.lowercase(Locale.ROOT).contains(query) }
            .filter { code.isEmpty() || it.country.split(",", ";").any { c -> c.trim().equals(code, true) } }
            .filter { !showFavorites || favorites.contains(it.url) }
            .take(400).toList()
        for (c in filtered) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10), dp(3), dp(5), dp(3)); setBackgroundColor(panel)
            }
            row.addView(TextView(this).apply {
                text = "▶"; textSize = 16f; setTextColor(accent); gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(dp(34), dp(48)))
            row.addView(TextView(this).apply {
                text = c.name + if (c.group.isNotBlank()) "\n${c.group}" else ""
                textSize = 14f; setTextColor(Color.WHITE); maxLines = 2; setPadding(dp(6), dp(4), dp(4), dp(4))
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(TextView(this).apply {
                text = if (favorites.contains(c.url)) "★" else "☆"; textSize = 20f
                setTextColor(accent); gravity = Gravity.CENTER; setPadding(dp(8), dp(8), dp(8), dp(8))
                setOnClickListener {
                    if (favorites.contains(c.url)) favorites.remove(c.url) else favorites.add(c.url)
                    renderChannels(); updateFavoriteButton()
                }
            })
            row.setOnClickListener { playChannel(c) }
            list.addView(row, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(5) })
        }
        status.text = if (channels.isEmpty()) "No channels loaded yet." else "Showing ${filtered.size} channels • ${channels.size} loaded"
    }

    private fun playChannel(channel: Channel) {
        selected = channel
        nowPlaying.text = channel.name
        player.setMediaItem(MediaItem.fromUri(channel.url))
        player.prepare()
        player.playWhenReady = true
        updateFavoriteButton()
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                status.text = "This stream could not play. Try another channel."
            }
        })
    }

    private fun toggleFavorite() {
        val c = selected ?: return
        if (favorites.contains(c.url)) favorites.remove(c.url) else favorites.add(c.url)
        updateFavoriteButton(); renderChannels()
    }

    private fun updateFavoriteButton() {
        favButton.text = if (selected != null && favorites.contains(selected!!.url)) "★ Favorited" else "☆ Favorite"
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}

private class SimpleTextWatcher(private val changed: () -> Unit) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { changed() }
    override fun afterTextChanged(s: android.text.Editable?) {}
}
