package com.todaymenu.app.presentation.common.components

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun VoiceInputDialog(
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) {
            errorMessage = "마이크 권한이 필요합니다"
        }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    DisposableEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        onDispose {
            speechRecognizer.destroy()
        }
    }

    val listener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                errorMessage = null
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) {
                isListening = false
                errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "인식 결과가 없어요. 다시 말씀해 주세요."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성이 감지되지 않았어요."
                    else -> "음성 인식 오류가 발생했어요."
                }
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                recognizedText = matches?.firstOrNull() ?: ""
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                recognizedText = matches?.firstOrNull() ?: recognizedText
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening() {
        recognizedText = ""
        errorMessage = null
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer.setRecognitionListener(listener)
        speechRecognizer.startListening(intent)
    }

    // 펄스 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    AlertDialog(
        onDismissRequest = {
            speechRecognizer.stopListening()
            onDismiss()
        },
        title = { Text("음성으로 재료 추가", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 큰 마이크 아이콘
                IconButton(
                    onClick = {
                        if (isListening) speechRecognizer.stopListening()
                        else if (hasPermission) startListening()
                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .then(if (isListening) Modifier.scale(scale) else Modifier)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Outlined.Mic else Icons.Outlined.MicOff,
                        contentDescription = if (isListening) "듣고 있어요" else "마이크 시작",
                        modifier = Modifier.size(48.dp),
                        tint = if (isListening) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = if (isListening) "듣고 있어요..."
                    else "마이크를 눌러 말씀해 주세요",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 도움말
                Text(
                    text = "예: \"삼겹살 300그램\"\n예: \"양파 3개랑 대파 2단\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 인식 결과
                if (recognizedText.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = recognizedText,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // 에러 메시지
                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (recognizedText.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        if (hasPermission) startListening()
                    }) {
                        Icon(Icons.Outlined.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("계속 입력")
                    }
                    Button(onClick = {
                        speechRecognizer.stopListening()
                        onResult(recognizedText)
                    }) {
                        Text("추가")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                speechRecognizer.stopListening()
                onDismiss()
            }) {
                Text("취소")
            }
        }
    )

    // 권한 허용 후 자동 시작
    LaunchedEffect(hasPermission) {
        if (hasPermission && !isListening && recognizedText.isEmpty()) {
            startListening()
        }
    }
}
