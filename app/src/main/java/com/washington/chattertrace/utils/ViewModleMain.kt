package com.washington.chattertrace.utils

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController

object ViewModleMain : ViewModel() {
    @JvmField
    var isShowWindow = MutableLiveData<Boolean>()

    @JvmField
    var isShowSuspendWindow = MutableLiveData<Boolean>()

    var NavController = MutableLiveData<NavHostController>()

    var isVisible = MutableLiveData<Boolean>()

}