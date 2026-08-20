package com.example.alarmboss.ui.ringing.tasks

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.alarmboss.data.ContentStore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Scans a real-world barcode/QR code with the camera and compares it to the target value
 * the user set in Settings (e.g. a code stuck to the bathroom mirror), forcing them to get
 * up and walk to a fixed location to dismiss the alarm.
 */
@Composable
fun BarcodeTaskScreen(onScanned: () -> Unit) {
    val context = LocalContext.current
    val store = remember { ContentStore(context) }
    var target by remember { mutableStateOf<String?>(null) }
    var lastSeen by remember { mutableStateOf("") }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        store.barcodeTarget.collect { target = it }
    }
    LaunchedEffect(Unit) { if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Scan your barcode to dismiss", style = MaterialTheme.typography.titleMedium)
        if (target.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "No target barcode set in Settings yet. Set one (e.g. scan a code you'll " +
                    "stick somewhere you must get up to reach), then this task will require it.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onScanned) { Text("Continue") }
            return@Column
        }
        Spacer(Modifier.height(12.dp))
        if (hasPermission) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                CameraPreviewWithBarcodeScan(
                    onBarcodeDetected = { value ->
                        lastSeen = value
                        if (value == target) onScanned()
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Last scanned: $lastSeen", style = MaterialTheme.typography.bodySmall)
        } else {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Camera permission is required for this task.")
            }
        }
    }
}

@Composable
private fun CameraPreviewWithBarcodeScan(onBarcodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanner = remember { BarcodeScanning.getClient() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull()?.rawValue?.let { onBarcodeDetected(it) }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                    )
                } catch (_: Exception) { }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}
