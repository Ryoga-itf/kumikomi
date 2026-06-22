package com.example.androidled

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private lateinit var returnTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainView = findViewById<View>(R.id.main)
        val initialPaddingLeft = mainView.paddingLeft
        val initialPaddingTop = mainView.paddingTop
        val initialPaddingRight = mainView.paddingRight
        val initialPaddingBottom = mainView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            val systemBars: Insets =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                initialPaddingLeft + systemBars.left,
                initialPaddingTop + systemBars.top,
                initialPaddingRight + systemBars.right,
                initialPaddingBottom + systemBars.bottom
            )
            insets
        }

        returnTextView = findViewById(R.id.text_view_return)

        val toggleIds = intArrayOf(
            R.id.toggle_led0,
            R.id.toggle_led1,
            R.id.toggle_led2,
            R.id.toggle_led3,
            R.id.toggle_led4
        )

        toggleIds.forEachIndexed { ledNumber, viewId ->
            findViewById<ToggleButton>(viewId).apply {
                tag = ledNumber
                setOnCheckedChangeListener(this@MainActivity)
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val ledNumber = buttonView?.tag as? Int ?: return
        val stat = if (isChecked) 1 else 0
        val stateText = if (isChecked) "ON" else "OFF"
        val uri = "$LED_API_URL?num=$ledNumber&stat=$stat"

        Log.d(TAG, "GET $uri")
        returnTextView.text = "LED$ledNumber -> $stateText (Changing...)"

        HttpGetTask(
            uri = uri,
            successMessage = "LED$ledNumber -> $stateText (Done!)",
            textView = returnTextView
        ).execute()
    }

    companion object {
        private const val TAG = "WebSample"
        private const val LED_API_URL = "http://192.168.11.142/~pi/ledtest.php"
    }
}
