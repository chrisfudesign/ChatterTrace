package com.washington.chattertrace

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Home : NavigationItem("home", R.drawable.ic_record_black_24dp, "New Session")
    object Recordings : NavigationItem("recordings", R.drawable.ic_outline_folder_24, "All Recordings")
    object Reflections : NavigationItem("reflections", R.drawable.ic_reflection_black_24dp, "Daily Reflections")
}
