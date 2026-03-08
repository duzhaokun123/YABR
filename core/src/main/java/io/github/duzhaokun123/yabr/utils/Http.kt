package io.github.duzhaokun123.yabr.utils

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

object Http {
    const val timeout = 10000

    fun get(urlString: String): InputStream {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = timeout
        connection.readTimeout = timeout
        connection.connect()
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw RuntimeException("HTTP GET request failed with response code: ${connection.responseCode}")
        }
        val inputStream = connection.inputStream
        return when (connection.contentEncoding?.lowercase()) {
            "br" -> RuntimeException("unsupported content encoding: br")
            "gzip" -> GZIPInputStream(inputStream)
            "deflate" -> InflaterInputStream(inputStream)
            else -> inputStream
        } as InputStream
    }
}