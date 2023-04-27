package com.example.data.constants

const val PUBS_PATH = "publications/pubs"
private const val PUBS_IMAGES_PATH = "publications/images"

private const val SERVER_BASIC_DIRECTORY = "/root/studhunter"

const val APPROVED_PUBS_PATH = "$PUBS_PATH/approved"
const val NEW_PUBS_PATH = "$PUBS_PATH/new"
const val REJECTED_PUBS_PATH = "$PUBS_PATH/rejected"

const val APPROVED_PUB_IMAGES_PATH = "$PUBS_IMAGES_PATH/approved"
const val NEW_PUB_IMAGES_PATH = "$PUBS_IMAGES_PATH/new"
const val REJECTED_PUB_IMAGES_PATH = "$PUBS_IMAGES_PATH/rejected"

const val SERVER_NEW_PUB_IMAGES_PATH = "$SERVER_BASIC_DIRECTORY/$NEW_PUB_IMAGES_PATH"

const val USERS_DATA_PATH = "users_data"

const val HOST = "http://5.181.255.253"

const val ASSEMBLED_P_NAME = "assembled_publications.json"

// /root/studhunter/publications/images/new

object Constants {
    private const val PUBLICATIONS = "publications"
    private const val USERS = "users"
    const val PUB_IMAGES = "$PUBLICATIONS/images"
    const val USER_IMAGES = "$USERS/profile_images"

    val PRICE_TYPES = mapOf(0 to "р", 1 to "р/час", 2 to "Бесплатно", 3 to "Бартер", 4 to "В проект")

}