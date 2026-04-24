package com.masterflight.ping.ui.traceroute.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterflight.ping.data.network.TraceRepository
import com.masterflight.ping.data.network.TraceStep
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TraceViewModel(private val repository: TraceRepository) : ViewModel() {

    private val _steps = MutableStateFlow<List<TraceStep>>(emptyList())
    val steps: StateFlow<List<TraceStep>> = _steps.asStateFlow()

    private val _isTracing = MutableStateFlow(false)
    val isTracing: StateFlow<Boolean> = _isTracing.asStateFlow()

    private var traceJob: Job? = null

    fun startTrace(host: String) {
        stopTrace()
        _steps.value = emptyList()
        _isTracing.value = true
        
        traceJob = viewModelScope.launch {
            repository.startTrace(host).collect { step ->
                _steps.value = _steps.value + step
            }
            _isTracing.value = false
        }
    }

    fun stopTrace() {
        traceJob?.cancel()
        _isTracing.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopTrace()
    }
}
