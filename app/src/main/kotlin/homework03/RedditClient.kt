package homework03

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.cfg.ConstructorDetector
import com.soywiz.korio.net.http.createHttpClient
import homework03.DataClasses.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream

class RedditClient {

    private val httpClient = createHttpClient()
    private val objectMapper = ObjectMapper().enable(
        DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT
    ).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    private fun unGZIP(data: ByteArray): String {
        return GZIPInputStream(data.inputStream()).bufferedReader(charset("UTF-8")).use { it.readText() }
    }

    suspend fun getTopic(name: String): TopicSnapshot = coroutineScope {
        val subtopics = async {
            objectMapper.readValue(
                unGZIP(httpClient.readBytes("https://www.reddit.com/r/$name/.json")),
                Subtopics::class.java
            )
        }
        val about = async {
            objectMapper.readValue(
                unGZIP(httpClient.readBytes("https://www.reddit.com/r/$name/about/.json")),
                About::class.java
            )
        }
        TopicSnapshot(
            subtopics.await(), about.await(),
            System.currentTimeMillis().toDouble() / 1000
        )
    }

    suspend fun getComments(link: String): CommentSnapshot = withContext(Dispatchers.IO) {
        val encodedLink = URLEncoder.encode(link, StandardCharsets.UTF_8.toString())
        val data = objectMapper.readValue(
            unGZIP(httpClient.readBytes("https://www.reddit.com/${encodedLink}.json")),
            object : TypeReference<List<SubtopicComments>>() {}
        )
        CommentSnapshot(data[1], System.currentTimeMillis().toDouble() / 1000)
    }
}
