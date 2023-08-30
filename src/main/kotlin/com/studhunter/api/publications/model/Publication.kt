package com.studhunter.api.publications.model

import com.studhunter.api.common.Constants.HOST
import com.studhunter.api.common.Constants.Y_CLOUD_PUB_IMAGES_PATH
import com.studhunter.api.common.Constants.cardImagePath
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import java.util.*
//        "https://storage.yandexcloud.net/stud-hunter-bucket/publications/images/$pubID/image_$imageIndex"

@Serializable
data class Publication(
    val id: String = UUID.randomUUID().toString(),
    val imageUrl: String = cardImagePath(id),
    val title: String,
    val description: String,
    val price: Int?,
    val priceType: String,
    val district: String?,
    val timestamp: Long = getTimeMillis(),
    val category: String,
    val userId: String,
    val socials: String,
    val approved: Boolean? = null
) {
    fun toMyPublication(views: Long, favorites: Long): MyPublication {
        return MyPublication(
            id = this.id,
            imageUrl = this.imageUrl,
            title = this.title,
            description = this.description,
            price = this.price,
            priceType = this.priceType,
            timestamp = this.timestamp,
            userId = this.userId,
            approved = this.approved,
            views = views,
            favorites = favorites
        )
    }
}