package com.example.yandexcloud

import com.example.data.constants.*
import com.example.data.models.Publication

class YcPaths {
    fun newPubPath(category: String, id: String) =
        "$NEW_PUBS_PATH/${category}/${id}.json"

    fun rejectedPubPath(publication: Publication) =
        "$REJECTED_PUBS_PATH/${publication.category}/${publication.id}.json"

    fun approvedPubPath(publication: Publication) =
        "$APPROVED_PUBS_PATH/${publication.category}/${publication.id}.json"

    fun newPubImagePath(publicationId: String, imageName: String) =
        "$NEW_PUB_IMAGES_PATH/${publicationId}/$imageName.jpeg"

    fun rejectedPubImagePath(publicationId: String, imageName: String) =
        "$REJECTED_PUB_IMAGES_PATH/${publicationId}/$imageName.jpeg"

    fun approvedPubImagePath(publicationId: String, imageName: String) =
        "$APPROVED_PUB_IMAGES_PATH/${publicationId}/$imageName.jpeg"
}