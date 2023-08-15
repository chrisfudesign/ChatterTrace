package com.washington.chattertrace.utils

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object ViewModleMain : ViewModel() {
    @JvmField
    var isShowWindow = MutableLiveData<Boolean>()

    var isShowSuspendWindow = MutableLiveData<Boolean>()

    var isVisible = MutableLiveData<Boolean>()

}