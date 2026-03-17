package com.todaymenu.app.data.backup

import android.content.Context
import android.net.Uri
import com.todaymenu.app.data.local.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    companion object {
        private const val DB_NAME = "todaymenu_db"
    }

    suspend fun exportToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // WAL 체크포인트 실행하여 모든 데이터를 메인 DB 파일에 반영
            database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")

            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("데이터베이스 파일을 찾을 수 없어요"))
            }

            context.contentResolver.openOutputStream(uri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("파일을 열 수 없어요"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)

            // DB 연결 닫기
            database.close()

            // WAL, SHM 파일 삭제
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()

            // 백업 파일 복원
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("백업 파일을 열 수 없어요"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "refrigo_backup_${dateFormat.format(Date())}.db"
    }
}
