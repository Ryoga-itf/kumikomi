package com.example.androidled

import android.os.AsyncTask
import android.widget.TextView
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class HttpGetTask(
    private val uri: String,
    private val successMessage: String,
    private val textView: TextView
) : AsyncTask<Void, Void, HttpGetResult>() {

    override fun doInBackground(vararg params: Void?): HttpGetResult {
        return try {
            HttpGetResult(body = execGet())
        } catch (e: Exception) {
            e.printStackTrace()
            HttpGetResult(errorMessage = e.message ?: e.javaClass.simpleName)
        }
    }

    override fun onPostExecute(result: HttpGetResult) {
        if (result.errorMessage != null) {
            textView.text = "error: ${result.errorMessage}"
            return
        }

        val body = result.body.orEmpty().trim()
        textView.text = if (body.isEmpty()) {
            successMessage
        } else {
            "$successMessage\n$body"
        }
    }

    private fun execGet(): String {
        val connection = URL(uri).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.connect()

            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                reader.readText()
            }.orEmpty()

            if (statusCode !in 200..299) {
                val detail = body.trim().takeIf { it.isNotEmpty() }
                throw IOException(
                    if (detail == null) "HTTP $statusCode" else "HTTP $statusCode: $detail"
                )
            }

            return body
        } finally {
            connection.disconnect()
        }
    }
}

data class HttpGetResult(
    val body: String? = null,
    val errorMessage: String? = null
)
