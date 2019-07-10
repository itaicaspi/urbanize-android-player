package com.urbanize.urbanizeplayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private var lensFacing = CameraX.LensFacing.FRONT
    private val TAG = "MainActivity"
    private lateinit var auth: FirebaseAuth

    private lateinit var viewModel: MainViewModel

    private class LuminosityAnalyzer : ImageAnalysis.Analyzer {
        private var lastAnalyzedTimestamp = 0L

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()
            val data = ByteArray(remaining())
            get(data)
            return data
        }

        fun ImageProxy.toBitmap(): Bitmap {
            val yBuffer = planes[0].buffer // Y
            val uBuffer = planes[1].buffer // U
            val vBuffer = planes[2].buffer // V

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            //U and V are swapped
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
            val imageBytes = out.toByteArray()

//            return imageBytes
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }


        private val detector: FirebaseVisionFaceDetector by lazy {
            FirebaseVision.getInstance().getVisionFaceDetector()
        }

        var pendingTask: Task<out Any>? = null

        override fun analyze(image: ImageProxy, rotationDegrees: Int) {
//            val currentTimestamp = System.currentTimeMillis()
            // Throttle calls to the detector.
            if (pendingTask != null && !pendingTask!!.isComplete) {
                Log.d("MLQRcodeAnalyzer", "Throttle calls to the detector")
                return
            }
            //YUV_420 is normally the input type here
            var rotation = rotationDegrees % 360
            if (rotation < 0) {
                rotation += 360
            }
            val mediaImage = FirebaseVisionImage.fromMediaImage(image.image!!, when (rotation) {
                0 -> FirebaseVisionImageMetadata.ROTATION_0
                90 -> FirebaseVisionImageMetadata.ROTATION_90
                180 -> FirebaseVisionImageMetadata.ROTATION_180
                270 -> FirebaseVisionImageMetadata.ROTATION_270
                else -> {
                    Log.e("MLQRcodeAnalyzer", "unexpected rotation: $rotationDegrees")
                    FirebaseVisionImageMetadata.ROTATION_0
                }
            })

            pendingTask = detector.detectInImage(mediaImage)
                .addOnSuccessListener { faces ->
                    // Task completed successfully
                    for (face in faces) {
                        val bounds = face.boundingBox
                        val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                        val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

                        // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                        // nose available):
//                        val leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
//                        leftEar?.let {
//                            val leftEarPos = leftEar.position
//                        }
//
//                        // If contour detection was enabled:
//                        val leftEyeContour = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).points
//                        val upperLipBottomContour = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).points
//
//                        // If classification was enabled:
//                        if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
//                            val smileProb = face.smilingProbability
//                        }
//                        if (face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
//                            val rightEyeOpenProb = face.rightEyeOpenProbability
//                        }
//
//                        // If face tracking was enabled:
//                        if (face.trackingId != FirebaseVisionFace.INVALID_ID) {
//                            val id = face.trackingId
//                        }
                        Log.d("firebaseVision", bounds.toString())
                    }
                }
                .addOnFailureListener(
                    object : OnFailureListener {
                        override fun onFailure(e: Exception) {
                            // Task failed with an exception
                            // ...
                        }
                    })

//            if (currentTimestamp - lastAnalyzedTimestamp >=
//                TimeUnit.SECONDS.toMillis(1)) {
//                val buffer = image.planes[0].buffer
//                val data = buffer.toByteArray()
//                val pixels = data.map { it.toInt() and 0xFF }
//                val luma = pixels.average()
//                Log.d("CameraXApp", "Average luminosity: $luma")
//                lastAnalyzedTimestamp = currentTimestamp
//            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        // hide the status bar and action bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        // camera handling
//        textureView.post { startCamera() }

        viewModel.campaigns.observe(this, Observer {newCampaigns ->
            Log.d(TAG, newCampaigns.toString())
        })

        GlobalScope.launch {
            viewModel.fetchCampaigns()
        }

        startWebPlayer()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        findViewById<TextureView>(R.id.textureView).visibility = View.GONE

        // Write a message to the database
//        val database = FirebaseDatabase.getInstance()
//        val myRef = database.getReference("version_information")
//        // Read from the database
//        myRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                val value = dataSnapshot.value
//                Log.d(TAG, "Value is: $value")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//        })
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
//        updateUI(currentUser)
    }

    private fun startWebPlayer() {
        // get the webview and load the video html5 template
        val mainWebView: WebView = findViewById(R.id.webview)
        mainWebView.webChromeClient = WebChromeClient()
        mainWebView.settings.javaScriptEnabled = true
        mainWebView.settings.mediaPlaybackRequiresUserGesture = false
        mainWebView.settings.setAppCacheEnabled(true)
        mainWebView.settings.domStorageEnabled = true
        mainWebView.settings.databaseEnabled = true
        mainWebView.loadUrl("http://10.42.0.1:5000/dynamic_content")
//        mainWebView.loadUrl("file:///android_asset/video.html")
    }

    private fun startCamera() {
        // request permissions
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1240)
            return
        }

        // make sure the camera is available
        val cameraAvailable = checkCameraHardware(this)
        if (!cameraAvailable) {
            findViewById<TextureView>(R.id.textureView).visibility = View.GONE
        }

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

        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .apply {
                setLensFacing(lensFacing)
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(textureView.display.rotation)
//                setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
//                setImageQueueDepth(1)
            }
            .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig).apply {
            analyzer = LuminosityAnalyzer()
        }

//        imageAnalysis.setAnalyzer({ image: ImageProxy, rotationDegrees: Int ->
//            // insert your code here.
//        })

        CameraX.bindToLifecycle(this, preview, imageAnalysis)
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
