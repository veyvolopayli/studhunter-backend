package com.studhunter.api.common
object Constants {
    private const val PUBLICATIONS = "publications"
    private const val USERS = "users"
    const val PUB_IMAGES = "$PUBLICATIONS/images"
    const val USER_IMAGES = "$USERS/profile_images"

    val priceTypes = listOf("р", "р/час", "Бесплатно", "Бартер", "В проект")

    const val USERS_DATA_PATH = "users_data"
    const val HOST = "http://5.181.255.253"

    const val AVATARS_PATH = "users/avatars/"
    const val JPEG = ".jpeg"

    const val Y_CLOUD_PUB_IMAGES_PATH = "https://storage.yandexcloud.net/stud-hunter-bucket/publications/images/"

    val cardImagePath : (pubID: String) -> String = { "$Y_CLOUD_PUB_IMAGES_PATH$it/0" }
}