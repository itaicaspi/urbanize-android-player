package com.urbanize.urbanizeplayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.security.Permissions


class MainActivity : AppCompatActivity() {
    private var lensFacing = CameraX.LensFacing.FRONT
    private val TAG = "MainActivity"


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // hide the status bar and action bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()


        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1240)
            return
        }

        // camera handling
        textureView.post { startCamera() }

        val cameraAvailable = checkCameraHardware(this)
        if (!cameraAvailable) {
            findViewById<TextureView>(R.id.textureView).visibility = View.GONE
        }

        // get the webview and load the video html5 template
        val mainWebView: WebView = findViewById(R.id.webview)
        mainWebView.webChromeClient = WebChromeClient()
        mainWebView.settings.javaScriptEnabled = true
        mainWebView.settings.mediaPlaybackRequiresUserGesture = false
        mainWebView.loadUrl("file:///android_asset/video.html")
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startCamera() {
        val metrics = DisplayMetrics().also { textureView.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetResolution(screenSize)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(windowManager.defaultDisplay.rotation)
            setTargetRotation(textureView.display.rotation)
        }.build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            textureView.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setLensFacing(lensFacing)
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(textureView.display.rotation)
                setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
            }.build()

        // Build the image capture use case and attach button click listener
        val imageCapture = ImageCapture(imageCaptureConfig)

        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = textureView.width / 2f
        val centerY = textureView.height / 2f

        val rotationDegrees = when (textureView.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        textureView.setTransform(matrix)
    }

    /** Check if this device has a camera */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun checkCameraHardware(context: Context): Boolean {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    Toast.makeText(context, "front facing camera available", Toast.LENGTH_LONG).show()
                    return true
                }
            }
        }

        // no camera on this device
        Toast.makeText(context, "front facing camera not available", Toast.LENGTH_LONG).show()
        return false
    }


}
