package com.example.servertimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.ZoneId
import java.time.ZonedDateTime
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 시간용 뷰 모델
 * **/
class TimeViewModel : ViewModel(){

    private val _utcTime = MutableStateFlow(ZonedDateTime.now(ZoneId.of("UTC"))) //
    val utcTime = _utcTime.asStateFlow()

    private val _kstTime = MutableStateFlow(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))
    val kstTime = _kstTime.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _utcTime.value = ZonedDateTime.now(ZoneId.of("UTC")) // 현재 시간 상태 UTC
                _kstTime.value = ZonedDateTime.now(ZoneId.of("Asia/Seoul")) // 현재 시간 상태 Asia/Seoul
                delay(1000) // 1초마다 갱신
            }
        }
    }
}