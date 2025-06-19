package com.teduniversity.medicalai.repository

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

class DownloadHelper(private val context: Context) {
    private var downloadReceiver: BroadcastReceiver? = null
    
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
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setAllowedOverRoaming(false)
            }
            
            val downloadId = downloadManager.enqueue(request)
            
            // Önceki receiver varsa unregister et
            downloadReceiver?.let { receiver ->
                try {
                    context.unregisterReceiver(receiver)
                } catch (e: Exception) {
                    // Already unregistered
                }
            }
            
            // Download tamamlandığında otomatik açma için BroadcastReceiver
            downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        checkDownloadStatusAndOpen(downloadManager, downloadId, cleanFileName)
                        
                        // Receiver'ı unregister et
                        try {
                            context.unregisterReceiver(this)
                            downloadReceiver = null
                        } catch (e: Exception) {
                            // Already unregistered
                        }
                    }
                }
            }
            
            // Receiver'ı register et
            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            try {
                context.registerReceiver(downloadReceiver, filter)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Download started but auto-open may not work", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkDownloadStatusAndOpen(downloadManager: DownloadManager, downloadId: Long, fileName: String) {
        try {
            val query = DownloadManager.Query()
            query.setFilterById(downloadId)
            val cursor: Cursor? = downloadManager.query(query)
            
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val statusIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val reasonIndex = c.getColumnIndex(DownloadManager.COLUMN_REASON)
                    
                    if (statusIndex >= 0) {
                        val status = c.getInt(statusIndex)
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                Toast.makeText(context, "Download completed successfully", Toast.LENGTH_SHORT).show()
                                // PDF'i aç
                                openPdfFile(fileName)
                            }
                            DownloadManager.STATUS_FAILED -> {
                                val reason = if (reasonIndex >= 0) c.getInt(reasonIndex) else -1
                                val errorMsg = getDownloadErrorMessage(reason)
                                Toast.makeText(context, "Download failed: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                Toast.makeText(context, "Download status: $status", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error checking download status: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getDownloadErrorMessage(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Device not found"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            DownloadManager.ERROR_FILE_ERROR -> "File error"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code"
            DownloadManager.ERROR_UNKNOWN -> "Unknown error"
            else -> "Download failed"
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
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                
                // PDF açabilecek uygulama var mı kontrol et
                val resolveInfo = context.packageManager.resolveActivity(intent, 0)
                if (resolveInfo != null) {
                    try {
                        context.startActivity(intent)
                        Toast.makeText(context, "Opening PDF...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Direct intent failed, try chooser
                        val chooserIntent = Intent.createChooser(intent, "Open PDF with")
                        chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        try {
                            context.startActivity(chooserIntent)
                        } catch (e2: Exception) {
                            Toast.makeText(context, "Could not open PDF. Please check Downloads folder.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    // No PDF app found, show message with link to Play Store
                    Toast.makeText(context, "No PDF viewer found. File saved to Downloads. Please install a PDF reader app.", Toast.LENGTH_LONG).show()
                    
                    // Try to open Play Store for PDF reader
                    try {
                        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pdf reader"))
                        playStoreIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(playStoreIntent)
                    } catch (e: Exception) {
                        // Play Store not available, do nothing
                    }
                }
            } else {
                Toast.makeText(context, "File not found: $cleanFileName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open PDF: ${e.message}", Toast.LENGTH_SHORT).show()
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
    
    fun cleanup() {
        downloadReceiver?.let { receiver ->
            try {
                context.unregisterReceiver(receiver)
                downloadReceiver = null
            } catch (e: Exception) {
                // Already unregistered
            }
        }
    }
} 