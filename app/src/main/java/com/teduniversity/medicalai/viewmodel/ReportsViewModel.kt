package com.teduniversity.medicalai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teduniversity.medicalai.model.Report
import com.teduniversity.medicalai.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportsViewModel : ViewModel() {
    private val repository = ReportRepository()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<Exception?>(null)
    val error: StateFlow<Exception?> = _error

    fun loadReports() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.getUserReports().collect { reports ->
                    _reports.value = reports
                }
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
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
} 