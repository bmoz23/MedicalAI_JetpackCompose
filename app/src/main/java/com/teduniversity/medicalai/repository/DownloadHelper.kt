package com.teduniversity.medicalai.repository

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

class DownloadHelper(private val context: Context) {
    
    fun downloadAndOpenPdf(downloadUrl: String, fileName: String) {
        try {
            // Dosya ismini temizle
            val cleanFileName = sanitizeFileName(fileName)
            
            // DownloadManager ile indirme
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(downloadUrl)
            
            val request = DownloadManager.Request(uri).apply {
                setTitle("Medical Report")
                setDescription("Downloading $cleanFileName")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, cleanFileName)
                setMimeType("application/pdf")
            }
            
            val downloadId = downloadManager.enqueue(request)
            
            // Download tamamlandığında otomatik açma için BroadcastReceiver kullanabiliriz
            // Ama şimdilik basit şekilde user manuel açacak
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun openPdfFile(fileName: String) {
        try {
            val cleanFileName = sanitizeFileName(fileName)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, cleanFileName)
            
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun sanitizeFileName(fileName: String): String {
        // Dosya ismi için güvenli karakter kontrolü
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .let { if (it.endsWith(".pdf")) it else "$it.pdf" }
    }
    
    fun isFileDownloaded(fileName: String): Boolean {
        return try {
            val cleanFileName = sanitizeFileName(fileName)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, cleanFileName)
            file.exists()
        } catch (e: Exception) {
            false
        }
    }
} 