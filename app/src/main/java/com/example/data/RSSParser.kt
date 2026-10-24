package com.example.data

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NewsItem(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val source: String
)

class RSSParser {
    suspend fun fetchNews(rssUrl: String): List<NewsItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<NewsItem>()
        var connection: HttpURLConnection? = null
        try {
            val url = URL(rssUrl)
            connection = url.openConnection() as HttpURLConnection
            val inputStream = connection.inputStream
            
            val parser = Xml.newPullParser()
            parser.setInput(inputStream, null)
            
            var eventType = parser.eventType
            var currentTitle = ""
            var currentLink = ""
            var currentDesc = ""
            var currentPubDate = ""
            var insideItem = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (name == "item") insideItem = true
                        else if (insideItem) {
                            when (name) {
                                "title" -> currentTitle = parser.nextText()
                                "link" -> currentLink = parser.nextText()
                                "description" -> currentDesc = parser.nextText()
                                "pubDate" -> currentPubDate = parser.nextText()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (name == "item") {
                            items.add(NewsItem(currentTitle, currentLink, currentDesc, currentPubDate, "Source"))
                            insideItem = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        items
    }
}
