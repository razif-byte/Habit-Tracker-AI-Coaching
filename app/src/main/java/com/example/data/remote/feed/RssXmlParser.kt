package com.example.data.remote.feed

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

object RssXmlParser {
    private const val TAG = "RssXmlParser"

    /**
     * Parses an RSS 2.0 or Atom XML feed string into a list of FeedItem objects.
     */
    fun parseFeed(
        xmlContent: String,
        category: String,
        platform: String = "WEB",
        sourceName: String = "RSS Feed",
        defaultIcon: String = "Link",
        colorHex: String = "#6750A4"
    ): List<FeedItem> {
        val items = mutableListOf<FeedItem>()
        if (xmlContent.isBlank()) return items

        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            var inItem = false
            var inEntry = false

            var currentTitle = ""
            var currentLink = ""
            var currentDescription = ""
            var currentPubDate = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name?.lowercase() ?: ""

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tagName) {
                            "item" -> {
                                inItem = true
                                currentTitle = ""
                                currentLink = ""
                                currentDescription = ""
                                currentPubDate = ""
                            }
                            "entry" -> {
                                inEntry = true
                                currentTitle = ""
                                currentLink = ""
                                currentDescription = ""
                                currentPubDate = ""
                            }
                            "title" -> {
                                if (inItem || inEntry) {
                                    currentTitle = safeReadText(parser)
                                }
                            }
                            "link" -> {
                                if (inItem || inEntry) {
                                    // Atom uses <link href="..."/> attribute, RSS uses <link>URL</link>
                                    val hrefAttr = parser.getAttributeValue(null, "href")
                                    if (!hrefAttr.isNullOrBlank()) {
                                        currentLink = hrefAttr
                                    } else {
                                        val text = safeReadText(parser)
                                        if (text.isNotBlank()) {
                                            currentLink = text
                                        }
                                    }
                                }
                            }
                            "description", "summary", "content" -> {
                                if (inItem || inEntry) {
                                    currentDescription = safeReadText(parser)
                                }
                            }
                            "pubdate", "published", "updated" -> {
                                if (inItem || inEntry) {
                                    currentPubDate = safeReadText(parser)
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if ((tagName == "item" && inItem) || (tagName == "entry" && inEntry)) {
                            inItem = false
                            inEntry = false

                            val cleanedTitle = cleanHtml(currentTitle)
                            val cleanedDesc = cleanHtml(currentDescription).take(140)
                            val finalLink = currentLink.trim()

                            if (cleanedTitle.isNotBlank() && finalLink.isNotBlank() && isValidUrl(finalLink)) {
                                // Determine platform or packageName if YouTube / Smule / PlayStore
                                val detectedPlatform = when {
                                    finalLink.contains("youtube.com") || finalLink.contains("youtu.be") -> "YOUTUBE"
                                    finalLink.contains("smule.com") -> "SMULE"
                                    finalLink.contains("play.google.com") -> "PLAY_STORE"
                                    platform.isNotBlank() -> platform
                                    else -> "WEB"
                                }

                                val packageName = when (detectedPlatform) {
                                    "YOUTUBE" -> "com.google.android.youtube"
                                    "SMULE" -> "com.smule.singandroid"
                                    else -> ""
                                }

                                items.add(
                                    FeedItem(
                                        title = cleanedTitle,
                                        description = cleanedDesc,
                                        link = finalLink,
                                        pubDate = currentPubDate,
                                        category = category,
                                        platform = detectedPlatform,
                                        sourceName = sourceName,
                                        packageName = packageName,
                                        iconName = defaultIcon,
                                        colorHex = colorHex
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML feed for category $category", e)
        }

        return items
    }

    private fun safeReadText(parser: XmlPullParser): String {
        return try {
            if (parser.next() == XmlPullParser.TEXT) {
                val text = parser.text ?: ""
                text
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    fun cleanHtml(input: String): String {
        if (input.isBlank()) return ""
        return input
            .replace(Regex("<[^>]*>"), " ") // Remove HTML tags
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&#8217;", "'")
            .replace("&#8220;", "\"")
            .replace("&#8221;", "\"")
            .replace("&#8211;", "-")
            .replace("&#8212;", "—")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
