package com.todaymenu.app.data.remote.gemini

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini Nano 온디바이스 서비스 (ML Kit Prompt API)
 *
 * Galaxy Z Flip6/Fold6, Galaxy S24 이상에서 지원.
 * 미지원 기기에서는 isAvailable()이 false를 반환하며
 * AiEngineRouter가 GeminiFlashService로 자동 폴백합니다.
 *
 * ML Kit GenAI는 Alpha 단계이므로, 실제 연동은 ML Kit GenAI SDK가
 * 안정화된 후 구현합니다. 현재는 가용성 체크 + 폴백 구조만 준비합니다.
 */
@Singleton
class GeminiNanoService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Gemini Nano 온디바이스 모델 사용 가능 여부 확인
     * 현재는 false를 반환하여 항상 Flash로 폴백합니다.
     * ML Kit GenAI SDK가 안정화되면 실제 가용성 체크로 교체합니다.
     */
    suspend fun isAvailable(): Boolean {
        // TODO: ML Kit GenAI SDK 안정화 후 실제 가용성 체크 구현
        // val generativeModel = GenerativeModel.newBuilder()
        //     .setPromptType(PromptType.UNSPECIFIED)
        //     .build()
        // return generativeModel.checkStatus() == FeatureStatus.AVAILABLE
        return false
    }

    suspend fun parseIngredients(ocrText: String): Result<String> {
        // TODO: Nano 온디바이스 구현
        return Result.failure(UnsupportedOperationException("Nano 미지원"))
    }

    suspend fun estimateExpiry(name: String, storageType: String): Result<Int> {
        // TODO: Nano 온디바이스 구현
        return Result.failure(UnsupportedOperationException("Nano 미지원"))
    }

    suspend fun parseVoiceInput(text: String): Result<String> {
        // TODO: Nano 온디바이스 구현
        return Result.failure(UnsupportedOperationException("Nano 미지원"))
    }
}
