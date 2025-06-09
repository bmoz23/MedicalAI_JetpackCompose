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
            }
            
            val downloadId = downloadManager.enqueue(request)
            
            // Download tamamlandığında otomatik açma için BroadcastReceiver
            downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        // Download tamamlandı, dosya durumunu kontrol et
                        val query = DownloadManager.Query()
                        query.setFilterById(downloadId)
                        val cursor: Cursor = downloadManager.query(query)
                        
                        if (cursor.moveToFirst()) {
                            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            if (columnIndex >= 0) {
                                when (cursor.getInt(columnIndex)) {
                                    DownloadManager.STATUS_SUCCESSFUL -> {
                                        // İndirme başarılı, PDF'i aç
                                        openPdfFile(cleanFileName)
                                        Toast.makeText(context, "Opening PDF...", Toast.LENGTH_SHORT).show()
                                    }
                                    DownloadManager.STATUS_FAILED -> {
                                        Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        cursor.close()
                        
                        // Receiver'ı unregister et
                        try {
                            context.unregisterReceiver(this)
                        } catch (e: Exception) {
                            // Already unregistered
                        }
                    }
                }
            }
            
            // Receiver'ı register et
            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            context.registerReceiver(downloadReceiver, filter)
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
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
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "No PDF viewer app found. Please install a PDF reader.", Toast.LENGTH_LONG).show()
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
} 