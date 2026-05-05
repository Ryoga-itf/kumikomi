package com.example.touchsample

import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var touchTypeText: TextView
    private lateinit var touchPointText1: TextView
    private lateinit var touchPointText2: TextView
    private lateinit var touchLengthText: TextView

    private var touchMode = NONE
    private var dragStartX = 0.0f
    private var dragStartY = 0.0f
    private var pinchStartDistance = 0.0

    private var touchTypeString = ""
    private var touchPoint1String = ""
    private var touchPoint2String = ""
    private var touchLengthString = ""

    companion object {
        private const val NONE = 0
        private const val TOUCH = 1
        private const val DRAG = 2
        private const val PINCH = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        touchTypeText = findViewById(R.id.text_view_touch_type)
        touchPointText1 = findViewById(R.id.text_view_touch_point_1)
        touchPointText2 = findViewById(R.id.text_view_touch_point_2)
        touchLengthText = findViewById(R.id.text_view_touch_length)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount >= 2) {
                    pinchStartDistance = getPinchDistance(event)
                    if (pinchStartDistance > 50f) {
                        touchMode = PINCH
                        touchTypeString = "PINCH"
                        touchPoint1String = "x:${event.getX(0)}.y:${event.getY(0)}"
                        touchPoint2String = "x:${event.getX(1)}.y:${event.getY(1)}"
                        touchLengthString = "length:${getPinchDistance(event)}"
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchMode == PINCH && pinchStartDistance > 0) {
                    touchTypeString = "PINCH"
                    touchPoint1String = "x:${event.getX(0)}.y:${event.getY(0)}"
                    touchPoint2String = "x:${event.getX(1)}.y:${event.getY(1)}"
                    touchLengthString = "length:${getPinchDistance(event)}"
                }
            }

            MotionEvent.ACTION_UP -> {
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (touchMode == PINCH) {
                    touchTypeString = "PINCH"
                    touchPoint1String = "x:${event.getX(0)}.y:${event.getY(0)}"
                    touchPoint2String = "x:${event.getX(1)}.y:${event.getY(1)}"
                    touchLengthString = "length:${getPinchDistance(event)}"
                    touchMode = NONE
                }
            }
        }

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                if (touchMode == NONE && event.pointerCount == 1) {
                    touchMode = TOUCH
                    dragStartX = event.getX(0)
                    dragStartY = event.getY(0)
                    touchTypeString = "TOUCH"
                    touchPoint1String = "x:$dragStartX.y:$dragStartY"
                    touchPoint2String = "x:${event.getX(0)},y:${event.getY(0)}"
                    touchLengthString = "length:${getDragDistance(event)}"
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchMode == DRAG || touchMode == TOUCH) {
                    touchMode = DRAG
                    touchTypeString = "DRAG"
                    touchPoint1String = "x:$dragStartX.y:$dragStartY"
                    touchPoint2String = "x:${event.getX(0)},y:${event.getY(0)}"
                    touchLengthString = "length:${getDragDistance(event)}"
                }
            }

            MotionEvent.ACTION_UP -> {
                if (touchMode == TOUCH) {
                    touchTypeString = "TOUCH"
                } else if (touchMode == DRAG) {
                    touchTypeString = "DRAG"
                }
                touchPoint1String = "x:$dragStartX.y:$dragStartY"
                touchPoint2String = "x:${event.getX(0)},y:${event.getY(0)}"
                touchLengthString = "length:${getDragDistance(event)}"
                touchMode = NONE
            }
        }

        touchTypeText.text = "touch type:$touchTypeString"
        touchPointText1.text = touchPoint1String
        touchPointText2.text = touchPoint2String
        touchLengthText.text = touchLengthString

        return super.onTouchEvent(event)
    }

    private fun getDragDistance(event: MotionEvent): Double {
        val touchX0 = event.getX(0).toDouble()
        val touchY0 = event.getY(0).toDouble()
        val dragLengthX = touchX0 - dragStartX
        val dragLengthY = touchY0 - dragStartY
        return sqrt(dragLengthX * dragLengthX + dragLengthY * dragLengthY)
    }

    private fun getPinchDistance(event: MotionEvent): Double {
        val x = (event.getX(0) - event.getX(1)).toDouble()
        val y = (event.getY(0) - event.getY(1)).toDouble()
        return sqrt(x * x + y * y)
    }
}