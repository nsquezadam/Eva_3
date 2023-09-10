package com.example.eva_3.wm

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class FormRegistroVm: ViewModel() {
    val nombreLugar = mutableStateOf("")
    val foto = mutableStateOf<Uri?>(null)

}