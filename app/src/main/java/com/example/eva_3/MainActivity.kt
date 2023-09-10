package com.example.eva_3

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eva_3.wm.AppVm
import com.example.eva_3.wm.FormRegistroVm
import com.example.eva_3.wm.Pantalla
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date


class MainActivity : ComponentActivity() {
    // vinculacion  del viewmodel  al main activity
    val appVM:AppVm by viewModels()
    val formRegistroVM:FormRegistroVm by viewModels()

    // controlador de la camara
    lateinit var  cameraController: LifecycleCameraController

    // permisos
    var  lanzadorPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        //permisos camara
        if(it[android.Manifest.permission.CAMERA]?:false){
            // aca ejecuto lo que quiera hacer con la camara
            appVM.onPermisoCamaraOk()
        }
        // permisos ubicacion
        if (
            (it[android.Manifest.permission.ACCESS_FINE_LOCATION]?: false) or
            (it[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false)) {
            appVM.permisoUbicacionOk()

        }else{
            Log.v("Lanzamiento de permiso callback","Se denegaron los permisos, para ejecutar la aplicacion es necesario que le otorgue permisos")
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // instanciamos
        cameraController = LifecycleCameraController(this)
        // ejecutar metodo para vicular al ciclo de vida
        cameraController.bindToLifecycle(this)
        // que camara se  usara
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        setContent {
                AppUI(lanzadorPermisos, cameraController )
        }
    }
}


@Composable
fun AppUI(
    lanzadorPermisos:ActivityResultLauncher<Array<String>>,
    cameraController: LifecycleCameraController
){
    // llamamdo el vieModel
    val appVm:AppVm = viewModel()
    when(appVm.pantallaActual.value){
        Pantalla.FORM ->{ PantallaFormUI()}
        Pantalla.CAMARA ->{ PantallaCamaraUI(lanzadorPermisos,cameraController)}
        Pantalla.MAP ->{ PantallaMapUI()}
    }


}

//UI PANATALLAS
//FORM
@Composable
fun PantallaFormUI(){
    val contexto = LocalContext.current
    val appVm:AppVm= viewModel( )
    val formRegistroVM:FormRegistroVm = viewModel()
    Column(
        modifier = Modifier.fillMaxWidth()
    ){
        Button(
            onClick = {
                appVm.pantallaActual.value = Pantalla.CAMARA
            }) {
            Text(text = "Tomar Foto")

        }
        formRegistroVM.foto.value?.let {
            Image(
                painter = BitmapPainter(uri2imageBitmap(it, contexto)) ,
                contentDescription ="Imagen capturada de camara x" )


        }
        TextField(
            value = formRegistroVM.nombreLugar.value ,
            onValueChange = {it})
    }

}

//CAMARA
@Composable
fun PantallaCamaraUI(
    lanzadorPermisos: ActivityResultLauncher<Array<String>>,
    cameraController: LifecycleCameraController
){
    lanzadorPermisos.launch(arrayOf(android.Manifest.permission.CAMERA))
    val contexto = LocalContext.current
    val appVm:AppVm = viewModel( )
    val formRegistroVM:FormRegistroVm = viewModel()

    AndroidView(

        modifier = Modifier.fillMaxSize(),
        factory = { PreviewView(it).apply {
            controller = cameraController
        }}
    )
    Button(onClick = {
        capturarFotografia(
            cameraController,
            crearArchivoImagenPublica(contexto),
            contexto){
            formRegistroVM.foto.value = it
            appVm.pantallaActual.value = Pantalla.FORM

        }

    }) {
        Text(text = "Captura")

    }


}

//MAP
@Composable
fun PantallaMapUI(){}


// FUNCIONES
//Imagen  a  bitmap  para poder visualizar

fun uri2imageBitmap(uri: Uri, contexto: Context) =
    BitmapFactory.decodeStream(
        contexto.contentResolver.openInputStream(uri)
    ).asImageBitmap()

fun generarNombreSegunFechaIngreso():String = LocalDateTime
    .now().toString().replace(Regex("[T:.-]"),"").substring(0,14)


// crear  archivo fotos  publicas  ---  no logrado
fun crearArchivoImagenPublica(contexto: Context): File = File(
   contexto.getExternalFilesDir(Environment.DIRECTORY_DCIM),
    "${generarNombreSegunFechaIngreso()}.jpg"
)

fun capturarFotografia(
    cameraController: LifecycleCameraController,
    archivo: File,
    contexto: Context,
    onImagenGuardado:(uri:Uri) -> Unit
){
    val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()

    cameraController.takePicture(
        opciones,
        ContextCompat.getMainExecutor(contexto),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.let {
                    onImagenGuardado(it)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CapturarFotografia", exception.message?:"Error")
            }
        }
    )

}


