package com.studhunter.api.updates.repository

interface UpdateRepository {

    fun getUpdateFile(version: String): ByteArray?

    fun isUpdateFileExist(version: String): Boolean?

    fun getLastVersionFile(): Pair<ByteArray, String>?

}