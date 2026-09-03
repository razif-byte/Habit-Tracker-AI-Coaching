package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.feed.FeedSource
import com.example.data.remote.feed.RssXmlParser
import com.example.data.remote.feed.TrendingFeedNetworkUtility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Habit Coach", appName)
  }

  @Test
  fun `rss xml parser parses standard rss 2 item correctly`() {
    val xml = """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0">
        <channel>
          <title>Test AI Feeds</title>
          <item>
            <title><![CDATA[New Gemini 1.5 Pro &amp; Flash Models]]></title>
            <link>https://gemini.google.com/updates</link>
            <description>Exciting &lt;b&gt;advancements&lt;/b&gt; in reasoning and speed.</description>
            <pubDate>Mon, 01 Sep 2026 12:00:00 GMT</pubDate>
          </item>
        </channel>
      </rss>
    """.trimIndent()

    val items = RssXmlParser.parseFeed(
      xmlContent = xml,
      category = "AI",
      platform = "WEB",
      sourceName = "Test Source"
    )

    assertEquals(1, items.size)
    assertEquals("New Gemini 1.5 Pro & Flash Models", items[0].title)
    assertEquals("https://gemini.google.com/updates", items[0].link)
    assertEquals("AI", items[0].category)
    assertTrue(items[0].description.contains("advancements"))
  }

  @Test
  fun `rss xml parser parses atom feed correctly`() {
    val xml = """
      <?xml version="1.0" encoding="utf-8"?>
      <feed xmlns="http://www.w3.org/2005/Atom">
        <title>YouTube Karaoke Search</title>
        <entry>
          <title>Bunga Angkasa (Iklim) Karaoke Lirik</title>
          <link href="https://www.youtube.com/watch?v=12345"/>
          <summary>Lagu malar segar rock kapak.</summary>
        </entry>
      </feed>
    """.trimIndent()

    val items = RssXmlParser.parseFeed(
      xmlContent = xml,
      category = "MALAY_KARAOKE",
      platform = "YOUTUBE",
      sourceName = "YouTube Test"
    )

    assertEquals(1, items.size)
    assertEquals("Bunga Angkasa (Iklim) Karaoke Lirik", items[0].title)
    assertEquals("https://www.youtube.com/watch?v=12345", items[0].link)
    assertEquals("YOUTUBE", items[0].platform)
    assertEquals("MALAY_KARAOKE", items[0].category)
  }

  @Test
  fun `json api parser parses algolia hn format correctly`() {
    val json = """
      {
        "hits": [
          {
            "title": "Show HN: Open Source Productivity Suite",
            "url": "https://example.com/productivity",
            "points": 250,
            "num_comments": 42,
            "author": "devuser"
          }
        ]
      }
    """.trimIndent()

    val source = FeedSource(
      url = "https://hn.algolia.com",
      category = "UTILITY",
      platform = "WEB",
      sourceName = "HackerNews API",
      isJson = true
    )

    val items = TrendingFeedNetworkUtility.parseJsonApiFeed(json, source)
    assertEquals(1, items.size)
    assertEquals("Show HN: Open Source Productivity Suite", items[0].title)
    assertEquals("https://example.com/productivity", items[0].link)
    assertEquals("UTILITY", items[0].category)
  }

  @Test
  fun `offline fallbacks provide items for all required categories`() {
    val aiFallbacks = TrendingFeedNetworkUtility.getOfflineCuratedFeedFallbacks("AI")
    val socialFallbacks = TrendingFeedNetworkUtility.getOfflineCuratedFeedFallbacks("SOCIAL")
    val utilityFallbacks = TrendingFeedNetworkUtility.getOfflineCuratedFeedFallbacks("UTILITY")
    val karaokeFallbacks = TrendingFeedNetworkUtility.getOfflineCuratedFeedFallbacks("MALAY_KARAOKE")

    assertTrue(aiFallbacks.isNotEmpty())
    assertTrue(socialFallbacks.isNotEmpty())
    assertTrue(utilityFallbacks.isNotEmpty())
    assertTrue(karaokeFallbacks.isNotEmpty())
  }
}

