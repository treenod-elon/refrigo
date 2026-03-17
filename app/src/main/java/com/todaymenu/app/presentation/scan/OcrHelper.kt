package com.todaymenu.app.presentation.scan

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object OcrHelper {
    suspend fun recognizeText(context: Context, imageUri: Uri): Result<String> {
        return suspendCancellableCoroutine { cont ->
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        val text = result.text
                        if (text.isBlank()) {
                            cont.resume(Result.failure(Exception("텍스트를 인식하지 못했어요.")))
                        } else {
                            cont.resume(Result.success(text))
                        }
                    }
                    .addOnFailureListener { e ->
                        cont.resume(Result.failure(e))
                    }
            } catch (e: Exception) {
                cont.resume(Result.failure(e))
            }
        }
    }
}
