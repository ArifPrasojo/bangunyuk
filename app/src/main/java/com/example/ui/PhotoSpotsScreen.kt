package com.example.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.PhotoSpotEntity
import com.example.ui.theme.GundamBlue
import com.example.ui.theme.GundamBlueSubtle
import com.example.ui.theme.GundamBorder
import com.example.ui.theme.GundamCanvasBg
import com.example.ui.theme.GundamCardBg
import com.example.ui.theme.GundamGreen
import com.example.ui.theme.GundamRed
import com.example.ui.theme.GundamTextPrimary
import com.example.ui.theme.GundamTextSecondary
import com.example.ui.theme.GundamWhite
import com.example.ui.theme.GundamYellow
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PhotoSpotsScreen(
    photoSpots: List<PhotoSpotEntity>,
    onSaveSpot: (PhotoSpotEntity) -> Unit,
    onDeleteSpot: (PhotoSpotEntity) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSpotTarget by remember { mutableStateOf<PhotoSpotEntity?>(null) }
    var currentCameraTempUri by remember { mutableStateOf<Uri?>(null) }

    // Dialog States
    var showAddSpotDialog by remember { mutableStateOf(false) }
    var spotToEdit by remember { mutableStateOf<PhotoSpotEntity?>(null) }
    var viewingFullPhotoSpot by remember { mutableStateOf<PhotoSpotEntity?>(null) }
    var spotToDelete by remember { mutableStateOf<PhotoSpotEntity?>(null) }

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    fun persistImage(sourceUri: Uri, spot: PhotoSpotEntity) {
        try {
            val dir = File(context.filesDir, "spot_photos").apply { mkdirs() }
            val destFile = File(dir, "spot_${spot.spotKey}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            val permanentUri = Uri.fromFile(destFile).toString()
            val updated = spot.copy(
                imageUri = permanentUri,
                updatedAt = System.currentTimeMillis()
            )
            onSaveSpot(updated)
        } catch (e: Exception) {
            Log.e("PhotoSpotsScreen", "Failed to save photo", e)
        }
    }

    // Camera Capture Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            currentCameraTempUri?.let { tempUri ->
                activeSpotTarget?.let { spot ->
                    persistImage(tempUri, spot)
                }
            }
        }
        activeSpotTarget = null
        currentCameraTempUri = null
    }

    // Gallery Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            activeSpotTarget?.let { spot ->
                persistImage(it, spot)
            }
        }
        activeSpotTarget = null
    }

    fun launchCamera(spot: PhotoSpotEntity) {
        activeSpotTarget = spot
        if (cameraPermissionState.status.isGranted) {
            try {
                val cacheDir = File(context.cacheDir, "camera_photos").apply { mkdirs() }
                val tempFile = File.createTempFile("spot_cam_${spot.spotKey}_", ".jpg", cacheDir)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
                currentCameraTempUri = uri
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                Log.e("PhotoSpotsScreen", "Error preparing camera URI", e)
            }
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    fun launchGallery(spot: PhotoSpotEntity) {
        activeSpotTarget = spot
        photoPickerLauncher.launch("image/*")
    }

    val registeredCount = photoSpots.count { !it.imageUri.isNullOrBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Spot Foto Bangun Tidur",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GundamBlue
                        )
                        Text(
                            text = "$registeredCount dari ${photoSpots.size} spot memiliki foto",
                            fontSize = 11.sp,
                            color = GundamTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_photo_spots_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = GundamBlue
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showAddSpotDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = GundamBlue),
                        modifier = Modifier.testTag("top_add_spot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Spot", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GundamCanvasBg)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSpotDialog = true },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah Spot Baru", fontWeight = FontWeight.Bold) },
                containerColor = GundamBlue,
                contentColor = GundamWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_custom_spot_fab")
            )
        },
        containerColor = GundamCanvasBg,
        modifier = modifier.statusBarsPadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Instruction Banner
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GundamBlueSubtle.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = GundamBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tambahkan spot foto di rumah Anda satu per satu dan beri nama sesuai keinginan. Foto referensi ini akan digunakan untuk mematikan alarm.",
                            fontSize = 12.sp,
                            color = GundamTextPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 16.dp)
                                .background(GundamBlue, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAFTAR SPOT FOTO (${photoSpots.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GundamTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Empty State if no spots exist
            if (photoSpots.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GundamCardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GundamBlueSubtle,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = GundamBlue,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum Ada Spot Foto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GundamTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tambahkan spot foto pertama Anda (misal: Wastafel, Toilet, Kulkas Dapur, Meja Kerja) untuk misi bangun tidur.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GundamTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddSpotDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GundamBlue, contentColor = GundamWhite),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tambah Spot Foto Sekarang", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // List of Photo Spots Cards
            items(photoSpots, key = { it.id }) { spot ->
                StructuredPhotoSpotCard(
                    spot = spot,
                    onLaunchCamera = { launchCamera(spot) },
                    onLaunchGallery = { launchGallery(spot) },
                    onViewFull = { viewingFullPhotoSpot = spot },
                    onEdit = { spotToEdit = spot },
                    onDelete = { spotToDelete = spot }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialog: Add Custom Spot One-by-One with Manual Naming
    if (showAddSpotDialog) {
        SpotEditorDialog(
            initialSpot = null,
            onDismiss = { showAddSpotDialog = false },
            onConfirm = { newSpot ->
                onSaveSpot(newSpot)
                showAddSpotDialog = false
            }
        )
    }

    // Dialog: Edit Existing Spot
    spotToEdit?.let { spot ->
        SpotEditorDialog(
            initialSpot = spot,
            onDismiss = { spotToEdit = null },
            onConfirm = { updatedSpot ->
                onSaveSpot(updatedSpot)
                spotToEdit = null
            }
        )
    }

    // Dialog: Full Photo View
    viewingFullPhotoSpot?.let { spot ->
        FullPhotoPreviewDialog(
            spot = spot,
            onDismiss = { viewingFullPhotoSpot = null },
            onRetake = {
                viewingFullPhotoSpot = null
                launchCamera(spot)
            }
        )
    }

    // Dialog: Confirm Delete
    spotToDelete?.let { spot ->
        AlertDialog(
            onDismissRequest = { spotToDelete = null },
            containerColor = GundamCardBg,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Hapus Spot \"${spot.spotName}\"?",
                    fontWeight = FontWeight.Bold,
                    color = GundamTextPrimary
                )
            },
            text = {
                Text(
                    text = "Spot ini dan foto referensinya akan dihapus dari daftar misi bangun tidur.",
                    color = GundamTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSpot(spot)
                        spotToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GundamRed)
                ) {
                    Text("Hapus Spot", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { spotToDelete = null }) {
                    Text("Batal", color = GundamTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun StructuredPhotoSpotCard(
    spot: PhotoSpotEntity,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onViewFull: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val spotIcon = getSpotIcon(spot.spotKey, spot.iconName)
    val hasImage = !spot.imageUri.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("spot_card_${spot.spotKey}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GundamCardBg),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (hasImage) GundamBlue.copy(alpha = 0.35f) else GundamBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Icon + Title & Description + Status Badge & Action Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (hasImage) GundamBlueSubtle else GundamBorder.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (hasImage) GundamBlue.copy(alpha = 0.3f) else GundamBorder
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = spotIcon,
                                contentDescription = spot.spotName,
                                tint = if (hasImage) GundamBlue else GundamTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = spot.spotName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GundamTextPrimary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = spot.spotDescription.ifBlank { "Spot foto target bangun tidur" },
                            style = MaterialTheme.typography.bodySmall,
                            color = GundamTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (hasImage) GundamGreen.copy(alpha = 0.1f) else GundamBorder.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (hasImage) GundamGreen.copy(alpha = 0.3f) else GundamBorder
                        )
                    ) {
                        Text(
                            text = if (hasImage) "✓ Terdaftar" else "Belum Ada Foto",
                            color = if (hasImage) GundamGreen else GundamTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ubah Nama",
                            tint = GundamTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body Content: If Registered -> 16:9 Image Preview with structured actions
            if (hasImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GundamBlueSubtle)
                        .border(1.dp, GundamBorder, RoundedCornerShape(12.dp))
                        .clickable { onViewFull() }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = spot.imageUri),
                        contentDescription = spot.spotName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Top Badge
                    Surface(
                        color = GundamCardBg.copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GundamGreen.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GundamGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Foto Terverifikasi",
                                color = GundamGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Overlay Preview Button
                    Surface(
                        color = GundamCardBg.copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
                        shape = RoundedCornerShape(topStart = 8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clickable { onViewFull() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Lihat",
                                tint = GundamBlue,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lihat Penuh",
                                color = GundamBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onLaunchCamera,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = GundamBlueSubtle,
                            contentColor = GundamBlue
                        )
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Foto Ulang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onLaunchGallery,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GundamTextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Galeri", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(38.dp)
                            .background(GundamRed.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, GundamRed.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = GundamRed,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            } else {
                // If NOT Registered -> Structured Empty Box with Dual Direct Action Buttons & Delete
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GundamBlueSubtle.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada foto referensi spot ini",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GundamTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onLaunchCamera,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("camera_btn_${spot.spotKey}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GundamBlue, contentColor = GundamWhite),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ambil Kamera", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onLaunchGallery,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("gallery_btn_${spot.spotKey}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GundamBlue),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pilih Galeri", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }

                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(GundamRed.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                    .border(1.dp, GundamRed.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Spot",
                                    tint = GundamRed,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpotEditorDialog(
    initialSpot: PhotoSpotEntity?,
    onDismiss: () -> Unit,
    onConfirm: (PhotoSpotEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialSpot?.spotName ?: "") }
    var description by remember { mutableStateOf(initialSpot?.spotDescription ?: "") }
    var selectedIcon by remember { mutableStateOf(initialSpot?.iconName ?: "DoorFront") }

    val isEditMode = initialSpot != null

    val quickNameSuggestions = listOf(
        "Toilet / Kamar Mandi" to "Wc",
        "Wastafel Cermin" to "Wash",
        "Dapur / Kulkas" to "Kitchen",
        "Dispenser Air Minum" to "LocalDrink",
        "Meja Belajar / Kerja" to "Lightbulb",
        "Pintu Depan Rumah" to "DoorFront",
        "Ruang Tamu" to "Tv",
        "Kasur / Kamar Tidur" to "Bed"
    )

    val iconOptions = listOf(
        "DoorFront" to ("Pintu" to Icons.Default.DoorFront),
        "Wc" to ("Toilet" to Icons.Default.Wc),
        "Wash" to ("Wastafel" to Icons.Default.Place),
        "Kitchen" to ("Dapur" to Icons.Default.Kitchen),
        "LocalDrink" to ("Dispenser" to Icons.Default.LocalDrink),
        "Lightbulb" to ("Meja/Lampu" to Icons.Default.Lightbulb),
        "Tv" to ("Ruang Tamu" to Icons.Default.Tv),
        "Bed" to ("Kamar" to Icons.Default.Bed)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GundamCardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = GundamBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Ubah Spot Foto" else "Tambah 1 Spot Foto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = GundamTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isEditMode)
                        "Perbarui nama dan keterangan lokasi spot ini."
                    else
                        "Beri nama spot foto secara manual (contoh: Toilet, Wastafel, Dispenser, Balkon, dll).",
                    fontSize = 12.sp,
                    color = GundamTextSecondary
                )

                // Input Nama Spot
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Spot Foto *") },
                    placeholder = { Text("Ketik nama spot...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("spot_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GundamBlue,
                        unfocusedBorderColor = GundamBorder
                    )
                )

                // Quick Suggestion Chips (when adding new spot)
                if (!isEditMode) {
                    Text("Pilihan Cepat (Opsional):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GundamTextSecondary)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        quickNameSuggestions.forEach { (suggestedName, defaultIcon) ->
                            FilterChip(
                                selected = name.equals(suggestedName, ignoreCase = true),
                                onClick = {
                                    name = suggestedName
                                    selectedIcon = defaultIcon
                                },
                                label = { Text(suggestedName, fontSize = 11.sp) },
                                shape = RoundedCornerShape(100.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GundamBlueSubtle,
                                    selectedLabelColor = GundamBlue
                                )
                            )
                        }
                    }
                }

                // Input Deskripsi Lokasi
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Lokasi (Opsional)") },
                    placeholder = { Text("Contoh: Foto cermin atau meja kerja") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("spot_desc_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GundamBlue,
                        unfocusedBorderColor = GundamBorder
                    )
                )

                // Pilihan Ikon
                Text("Pilih Ikon:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GundamTextPrimary)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    iconOptions.forEach { (iconKey, iconPair) ->
                        val isSelected = selectedIcon == iconKey
                        Surface(
                            onClick = { selectedIcon = iconKey },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) GundamBlue else GundamBlueSubtle,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GundamBlue else GundamBorder
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = iconPair.second,
                                    contentDescription = iconPair.first,
                                    tint = if (isSelected) GundamWhite else GundamBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val key = initialSpot?.spotKey ?: ("SPOT_" + System.currentTimeMillis().toString().takeLast(6))
                    val finalName = if (name.isNotBlank()) name.trim() else "Spot Foto"
                    val finalDesc = if (description.isNotBlank()) description.trim() else "Spot target bangun tidur"
                    val spotEntity = initialSpot?.copy(
                        spotName = finalName,
                        spotDescription = finalDesc,
                        iconName = selectedIcon,
                        updatedAt = System.currentTimeMillis()
                    ) ?: PhotoSpotEntity(
                        spotKey = key,
                        spotName = finalName,
                        spotDescription = finalDesc,
                        imageUri = null,
                        iconName = selectedIcon
                    )
                    onConfirm(spotEntity)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GundamBlue, contentColor = GundamWhite),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_spot_confirm_button")
            ) {
                Text(if (isEditMode) "Simpan Perubahan" else "Tambah Spot", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = GundamTextSecondary)
            }
        }
    )
}

@Composable
private fun FullPhotoPreviewDialog(
    spot: PhotoSpotEntity,
    onDismiss: () -> Unit,
    onRetake: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f)),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = spot.spotName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = spot.spotDescription,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Photo Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = spot.imageUri),
                        contentDescription = spot.spotName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                // Bottom Retake Button
                Button(
                    onClick = onRetake,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GundamBlue, contentColor = GundamWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Foto Ulang Spot Ini", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getSpotIcon(spotKey: String, iconName: String): ImageVector {
    return when (iconName) {
        "Wc" -> Icons.Default.Wc
        "Wash" -> Icons.Default.Place
        "Kitchen" -> Icons.Default.Kitchen
        "LocalDrink" -> Icons.Default.LocalDrink
        "Lightbulb" -> Icons.Default.Lightbulb
        "Tv" -> Icons.Default.Tv
        "Bed" -> Icons.Default.Bed
        "DoorFront" -> Icons.Default.DoorFront
        else -> when (spotKey) {
            "TOILET" -> Icons.Default.Wc
            "SINK" -> Icons.Default.Place
            "KITCHEN" -> Icons.Default.Kitchen
            "DOOR" -> Icons.Default.DoorFront
            else -> Icons.Default.Place
        }
    }
}
