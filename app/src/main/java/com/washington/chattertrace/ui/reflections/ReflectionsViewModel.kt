package com.washington.chattertrace.ui.reflections

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReflectionsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is daily reflections fragment"
    }
    val text: LiveData<String> = _text
}