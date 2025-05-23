package com.studhunter.features.db

import com.studhunter.api.chat.tables.*
import com.studhunter.api.common.tables.Categories
import com.studhunter.api.common.tables.Districts
import com.studhunter.api.common.tables.PriceTypes
import com.studhunter.api.common.tables.Universities
import com.studhunter.api.favorites.tables.FavoritePublications
import com.studhunter.api.publications.tables.PublicationViews
import com.studhunter.api.publications.tables.Publications
import com.studhunter.api.reviews.tables.Reviews
import com.studhunter.api.users.tables.UserData
import com.studhunter.api.users.tables.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Database.createTables(): Database {
    transaction(this) {
        SchemaUtils.create(
            Chats,
//            OfferRequests,
//            OfferResponses,
            Tasks,
            UserChatMessages,
            FavoritePublications,
            Publications,
            PublicationViews,
            Reviews,
            UserData,
            Users,
            Categories,
            Districts,
            PriceTypes,
            Universities
        )
    }

    return this
}