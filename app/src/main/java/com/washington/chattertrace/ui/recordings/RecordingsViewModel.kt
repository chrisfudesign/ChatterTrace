package com.washington.chattertrace.ui.recordings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is recordings fragment"
    }
    val text: LiveData<String> = _text
}