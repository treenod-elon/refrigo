package com.todaymenu.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Pretendard 폰트 적용 방법:
 * 1. https://github.com/orioncactus/pretendard/releases 에서 다운로드
 * 2. Pretendard-Regular.ttf, Medium.ttf, SemiBold.ttf, Bold.ttf를
 *    app/src/main/res/font/ 폴더에 pretendard_regular.ttf 등으로 복사
 * 3. 아래 주석 해제하여 Pretendard FontFamily 활성화
 *
 * val Pretendard = FontFamily(
 *     Font(R.font.pretendard_regular, FontWeight.Normal),
 *     Font(R.font.pretendard_medium, FontWeight.Medium),
 *     Font(R.font.pretendard_semibold, FontWeight.SemiBold),
 *     Font(R.font.pretendard_bold, FontWeight.Bold)
 * )
 */

// 폰트 파일 추가 전까지 시스템 기본 폰트 사용
val Pretendard = FontFamily.Default

val Typography = Typography(
    // 화면 제목 — "내 냉장고", "메뉴 추천"
    titleLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    // 섹션 제목 — "육류", "오늘의 추천"
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    // 소제목
    titleSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // 본문, 재료명, 메뉴명
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.6.sp
    ),
    // 부가 정보 (수량, 날짜)
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    // 소본문
    bodySmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // 버튼 텍스트, 칩 텍스트
    labelLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // 중간 라벨
    labelMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // 캡션, 힌트 — 최소 허용 크기 (12sp 이하 절대 금지)
    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
)
