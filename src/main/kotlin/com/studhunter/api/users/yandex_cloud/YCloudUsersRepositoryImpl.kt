package com.studhunter.api.users.yandex_cloud

import com.amazonaws.services.s3.AmazonS3
import com.studhunter.BUCKET_NAME
import com.studhunter.api.users.repository.YCloudUsersRepository
import java.io.File

class YCloudUsersRepositoryImpl(private val s3: AmazonS3) : YCloudUsersRepository {

    private val imagePathFor: (String) -> String = { "users/avatars/$it.jpeg" }
    override suspend fun insertUserAvatar(file: File, userID: String): Boolean {
        val result = s3.putObject(BUCKET_NAME, imagePathFor(userID), file)
        return result != null
    }

}