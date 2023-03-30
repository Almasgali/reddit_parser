package homework03

import com.fasterxml.jackson.annotation.JsonProperty
import com.soywiz.korio.lang.substr

class DataClasses {

    data class CommentSnapshot(
        private val comments: SubtopicComments,
        private val timestamp: Double
    ) {
        val requestTime: Double
            get() = timestamp
        private val linearComments = mutableListOf<LinearComment>()

        init {
            constructLinearComments()
        }

        private fun constructLinearComments(comments: SubtopicComments? = this.comments, parentId: Int = -1) {
            if (comments == null) {
                return
            }
            for (i in 0 until comments.data.children.size) {
                linearComments.add(
                    LinearComment(
                        comments.data.children[i].data.body,
                        comments.data.children[i].data.depth,
                        comments.data.children[i].data.author,
                        linearComments.size,
                        parentId
                    )
                )
                constructLinearComments(comments.data.children[i].data.replies, linearComments.size - 1)
            }
        }

        fun getLinearComments(): List<LinearComment> {
            return linearComments
        }
    }

    data class LinearComment(
        val body: String? = null,
        val depth: Int,
        val author: String,
        val id: Int,
        val replyTo: Int
    ) {
        override fun toString(): String {
            return "Parent id: $replyTo\nId: $id\nBody: $body"
        }
    }

    data class SubtopicComments(@JsonProperty("data") val data: SubtopicCommentsData)

    data class SubtopicCommentsData(@JsonProperty("children") val children: List<CommentsData>)

    data class CommentsData(@JsonProperty("data") val data: CommentData)

    data class CommentData(
        @JsonProperty("replies") val replies: SubtopicComments? = null,
        @JsonProperty("depth") val depth: Int,
        @JsonProperty("body") val body: String? = null,
        @JsonProperty("author") val author: String,
        @JsonProperty("created") val created: Double,
        @JsonProperty("parent_id") val parent_id: String? = null
    )

    data class TopicSnapshot(
        private val subtopics: Subtopics,
        private val about: About,
        private val timestamp: Double
    ) {
        val creationTime: Double
            get() = about.data.created
        val subscribersOnline: Int
            get() = about.data.active_user_count
        val description: String
            get() = about.data.description
        val requestTime: Double
            get() = timestamp
        private val subtopicsList = mutableListOf<BeautySubtopic>()

        init {
            constructSubtopicsList()
        }

        private fun constructSubtopicsList() {
            for (child in subtopics.data.children) {
                subtopicsList.add(
                    BeautySubtopic(
                        child.author,
                        child.createTime,
                        child.upvotes,
                        child.downvotes,
                        child.title,
                        child.text,
                        child.textHtml,
                        child.url,
                        child.permalink.substr(1),
                        child.id
                    )
                )
            }
        }

        fun getSubtopicsList(): List<BeautySubtopic> {
            return subtopicsList
        }
    }

    data class Subtopics(@JsonProperty("data") val data: Data)

    data class Data(@JsonProperty("children") val children: List<Subtopic>)

    data class Subtopic(@JsonProperty("data") val data: SubtopicData) {
        val url: String
            get() = data.url
        val permalink: String
            get() = data.permalink
        val id: String
            get() = data.id
        val author: String
            get() = data.author_fullname
        val createTime: Double
            get() = data.created
        val upvotes: Int
            get() = data.ups
        val downvotes: Int
            get() = data.downs
        val title: String
            get() = data.title
        val text: String
            get() = data.selftext
        val textHtml: String?
            get() = data.selftext_html
    }

    data class SubtopicData(
        @JsonProperty("author_fullname") val author_fullname: String,
        @JsonProperty("created") val created: Double,
        @JsonProperty("ups") val ups: Int,
        @JsonProperty("downs") val downs: Int,
        @JsonProperty("title") val title: String,
        @JsonProperty("selftext") val selftext: String,
        @JsonProperty("selftext_html") val selftext_html: String? = null,
        @JsonProperty("url") val url: String,
        @JsonProperty("permalink") val permalink: String,
        @JsonProperty("id") val id: String
    )

    data class BeautySubtopic(
        val author_fullname: String,
        val created: Double,
        val ups: Int,
        val downs: Int,
        val title: String,
        val selftext: String,
        val selftext_html: String?,
        val url: String,
        val permalink: String,
        val id: String
    )

    data class About(@JsonProperty("data") val data: AboutData)

    data class AboutData(
        @JsonProperty("created") val created: Double,
        @JsonProperty("active_user_count") val active_user_count: Int,
        @JsonProperty("description") val description: String,
    )
}