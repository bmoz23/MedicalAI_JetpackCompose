package com.teduniversity.medicalai.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teduniversity.medicalai.model.Report
import com.teduniversity.medicalai.repository.DownloadHelper
import com.teduniversity.medicalai.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportsViewModel : ViewModel() {
    private val repository = ReportRepository()
    private var downloadHelper: DownloadHelper? = null

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<Exception?>(null)
    val error: StateFlow<Exception?> = _error
    
    private val _downloadStatus = MutableStateFlow<String?>(null)
    val downloadStatus: StateFlow<String?> = _downloadStatus

    fun initializeDownloadHelper(context: Context) {
        downloadHelper = DownloadHelper(context)
    }

    fun loadReports() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.getUserReports().collect { reports ->
                    _reports.value = reports.sortedByDescending { it.lastModified }
                }
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadReport(report: Report) {
        viewModelScope.launch {
            try {
                _downloadStatus.value = "Downloading ${report.fileName}..."
                downloadHelper?.downloadAndOpenPdf(report.imageUrl, report.fileName)
                _downloadStatus.value = "Download started successfully"
                
                // Clear status after 3 seconds
                kotlinx.coroutines.delay(3000)
                _downloadStatus.value = null
            } catch (e: Exception) {
                _downloadStatus.value = "Download failed: ${e.message}"
                kotlinx.coroutines.delay(3000)
                _downloadStatus.value = null
            }
        }
    }

    fun addReport(report: Report) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.addReport(report)
                loadReports() // Reload reports after adding new one
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.deleteReport(reportId)
                loadReports() // Reload reports after deleting
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }
} 