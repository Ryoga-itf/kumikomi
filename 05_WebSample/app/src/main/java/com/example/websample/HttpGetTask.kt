package com.example.websample

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.widget.TextView
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpGetTask(
    private val parentActivity: Activity,
    private val textView: TextView
) : AsyncTask<Void, Void, String>() {

    private var dialog: ProgressDialog? = null
    private val uri = "https://www.yamagiwalab.jp/~yama/KPK/Hello.html"

    override fun onPreExecute() {
        dialog = ProgressDialog(parentActivity).apply {
            setMessage("")
            show()
        }
    }

    override fun doInBackground(vararg params: Void?): String {
        return execGet()
    }

    override fun onPostExecute(result: String) {
        dialog?.dismiss()
        textView.text = result
    }

    private fun execGet(): String {
        var http: HttpURLConnection? = null
        var input: InputStream? = null
        val src = StringBuilder()

        try {
            val url = URL(uri)
            http = url.openConnection() as HttpURLConnection
            http.requestMethod = "GET"
            http.connect()

            input = http.inputStream
            val buffer = ByteArray(1024)

            while (true) {
                val size = input.read(buffer)
                if (size <= 0) break
                src.append(String(buffer, 0, size))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                http?.disconnect()
                input?.close()
            } catch (_: Exception) {
            }
        }

        return src.toString()
    }
}