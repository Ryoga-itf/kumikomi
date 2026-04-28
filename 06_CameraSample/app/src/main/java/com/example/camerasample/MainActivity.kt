package com.example.camerasample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var cameraDevice: CameraDevice? = null
    private lateinit var cameraPreview: TextureView
    private lateinit var cameraButton: Button
    private var captureSession: CameraCaptureSession? = null
    private var previewRequest: CaptureRequest? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null

    private var frontCameraId: String? = null
    private var backCameraId: String? = null
    private var currCameraId: String? = null

    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        frontCameraId = null
        backCameraId = null

        try {
            val cameraIdList = cameraManager.cameraIdList
            for (cameraId in cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> {
                        frontCameraId = cameraId
                        Log.d("CameraSample", "Front camera found.")
                    }
                    CameraCharacteristics.LENS_FACING_BACK -> {
                        backCameraId = cameraId
                        Log.d("CameraSample", "Back camera found.")
                    }
                }
            }
        } catch (e: CameraAccessException) {
            throw RuntimeException(e)
        }

        currCameraId = frontCameraId ?: backCameraId

        cameraButton = findViewById(R.id.camera_button)
        cameraButton.setOnClickListener {
            when {
                currCameraId == frontCameraId && backCameraId != null -> {
                    currCameraId = backCameraId
                    cameraButton.text = "Change to Front Camera"
                }
                currCameraId == backCameraId && frontCameraId != null -> {
                    currCameraId = frontCameraId
                    cameraButton.text = "Change to Back Camera"
                }
                else -> return@setOnClickListener
            }

            captureSession?.close()
            cameraDevice?.close()
            openCurrentCamera()
        }

        cameraPreview = findViewById(R.id.cameraPreview)
        cameraPreview.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    Log.d("CameraSample", "Camera is not permitted.")
                    return
                }
                openCurrentCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                Log.d("CameraSample", "onSurfaceTextureSizeChanged")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.d("CameraSample", "onSurfaceTextureDestroyed")
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCurrentCamera() {
        val id = currCameraId ?: return
        try {
            cameraManager.openCamera(id, stateCallback, null)
        } catch (e: CameraAccessException) {
            throw RuntimeException(e)
        } catch (e: SecurityException) {
            Log.e("CameraSample", "Camera permission error", e)
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(@NonNull camera: CameraDevice) {
            cameraDevice = camera
            try {
                val texture = cameraPreview.surfaceTexture ?: return
                texture.setDefaultBufferSize(cameraPreview.width, cameraPreview.height)

                val surface = Surface(texture)
                previewRequestBuilder =
                    cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                previewRequestBuilder?.addTarget(surface)

                cameraDevice?.createCaptureSession(
                    listOf(surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(@NonNull session: CameraCaptureSession) {
                            captureSession = session
                            try {
                                previewRequest = previewRequestBuilder?.build()
                                previewRequest?.let {
                                    captureSession?.setRepeatingRequest(it, null, null)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {
                            Log.e("CameraSample", "error onConfigureFailed")
                        }
                    },
                    null
                )
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onDisconnected(@NonNull camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }

        override fun onError(@NonNull camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }
    }

    override fun onPause() {
        super.onPause()
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
    }
}