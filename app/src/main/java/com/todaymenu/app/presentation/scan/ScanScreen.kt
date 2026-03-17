package com.todaymenu.app.presentation.scan

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.todaymenu.app.presentation.common.components.EmptyState
import com.todaymenu.app.presentation.scan.components.CameraPreviewSection
import com.todaymenu.app.presentation.scan.components.ScanResultContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasCameraPermission by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    // 갤러리 선택
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { processImage(scope, context, it, viewModel) }
    }

    // 초기 권한 요청
    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // 에러 snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 저장 완료 시 뒤로가기
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("재료가 냉장고에 추가되었어요!")
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState.phase) {
                            ScanPhase.CAMERA -> "영수증 스캔"
                            ScanPhase.PROCESSING -> "분석 중..."
                            ScanPhase.RESULT -> "인식 결과"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState.phase) {
                ScanPhase.CAMERA -> {
                    if (hasCameraPermission) {
                        CameraPreviewSection(
                            onPhotoCaptured = { uri ->
                                processImage(scope, context, uri, viewModel)
                            },
                            onGalleryClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    } else {
                        EmptyState(
                            icon = Icons.Outlined.CameraAlt,
                            title = "카메라 권한이 필요해요",
                            description = "영수증 스캔을 위해\n카메라 권한을 허용해 주세요."
                        ) {
                            Button(onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }) {
                                Text("권한 허용하기")
                            }
                        }
                    }
                }

                ScanPhase.PROCESSING -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = uiState.processingMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ScanPhase.RESULT -> {
                    if (uiState.isProcessing) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = uiState.processingMessage,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        ScanResultContent(
                            ingredients = uiState.scannedIngredients,
                            purchaseDate = uiState.purchaseDate,
                            onToggleSelection = { viewModel.toggleIngredientSelection(it) },
                            onUpdateIngredient = { index, ingredient ->
                                viewModel.updateIngredient(index, ingredient)
                            },
                            onRemoveIngredient = { viewModel.removeIngredient(it) },
                            onAddManual = { viewModel.addManualIngredient(it) },
                            onPurchaseDateChange = { viewModel.setPurchaseDate(it) },
                            onSave = { viewModel.saveSelectedIngredients() },
                            onRetake = { viewModel.retakePhoto() }
                        )
                    }
                }
            }
        }
    }
}

private fun processImage(
    scope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
    imageUri: Uri,
    viewModel: ScanViewModel
) {
    viewModel.setCapturedImageUri(imageUri)
    scope.launch {
        OcrHelper.recognizeText(context, imageUri)
            .onSuccess { ocrText ->
                viewModel.processOcrText(ocrText)
            }
            .onFailure {
                viewModel.processOcrText("") // 빈 텍스트로 시도 (에러 처리는 VM에서)
            }
    }
}
