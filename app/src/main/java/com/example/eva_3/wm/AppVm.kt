package com.example.eva_3.wm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AppVm :ViewModel() {
    // guardamos las variables de estado stkeholder
    var onPermisoCamaraOk:() -> Unit= {}
    val pantallaActual = mutableStateOf(Pantalla.FORM)
    val latitud = mutableStateOf(0.0)
    val longitud = mutableStateOf(0.0)
    var permisoUbicacionOk:() -> Unit = {}
}
