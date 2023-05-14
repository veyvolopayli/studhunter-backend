package com.example.data.updateservice

interface UpdateRepository {

    fun getUpdateFile(version: String): ByteArray?

    fun isUpdateFileExists(version: String): Boolean?

    fun getLastVersionFile(): Pair<ByteArray, String>?

}