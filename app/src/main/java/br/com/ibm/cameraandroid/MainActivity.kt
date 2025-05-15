package br.com.ibm.cameraandroid

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import br.com.ibm.cameraandroid.permissions.WithPermission
import br.com.ibm.cameraandroid.ui.theme.CameraAndroidTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    innerPadding ->
                        WithPermission(
                            modifier = Modifier.padding(innerPadding),
                            /*
                            Se eu quiser fazer permissão de áudio, fazer assim:
                            permission = Manifest.permission.RECORD_AUDIO,
                            permissionActionLabel = "Permitir gravar audio..."
                            */
                            permission = Manifest.permission.CAMERA,
                            permissionActionLabel = "Permitir Camera..."
                            ) {
                            // Composable que será carregado após...
                            CameraAppTirarFoto()
                        }
                }
            }
        }
    }
}

@Composable
fun CameraAppTirarFoto() {
    Text("Camera aberta")
}


@Composable
fun TakePhotoScreen() {
    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_FRONT)
    }
    var zoomLevel by remember {
        mutableFloatStateOf(0.0f)
    }
    var imageCaptureUseCase by remember {
        mutableStateOf(ImageCapture.Builder().build())
    }

    val localContext = LocalContext.current

    Box{
        CameraPreview(
            lensFacing = lensFacing,
            zoomLevel = zoomLevel,
            ImageCaptureUseCase = imageCaptureUseCase
        )
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Row{
                Button(
                    onClick = { lensFacing = CameraSelector.LENS_FACING_FRONT}
                ){
                    Text(text = "Frontal")
                }
                Button(
                    onClick = {lensFacing = CameraSelector.LENS_FACING_BACK}
                ){
                    Text(text = "Traseira")
                }
            }
            Row {
                Button(
                    onClick = { zoomLevel =  1.0f}
                ) {
                    Text(text = "Frontal")
                }
                Button(
                    onClick = { zoomLevel = 1.0f }
                ) {
                    Text(text = "Traseiro")
                }
                Button(
                    onClick = {
                        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                            File(localContext.externalCacheDir, "image.jpg")
                        )

                        val callback = object: ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                Log.i("Camera", "Imagem salva no diretório dentro do app")
                            }

                            override fun onError(expection: ImageCaptureException) {
                                Log.e("Camera", "Imagem não foi salva" + expection.message)
                            }
                        }
                    }
                ) {
                    Text("Take photo")
                }
            }
        }
    }
}


@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    zoomLevel: Float,
    ImageCaptureUseCase: ImageCapture
) {
    val previewUseCase = remember {
        androidx.camera.core.Preview.Builder()
            .build() // o resultado do metodo build() vai gerar uma instancia, a Preview
    }

    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null) //conforme o decorrer do cod, esse valor null muda
    }

    var cameraControl by remember {
        mutableStateOf<CameraControl?>(null)
    }

    // Contexto do aplicativo
    // Contexto para nos neste caso em especifico serve para abrir outras linhas de execução além da MAIN theme
    val localContext = LocalContext.current //contexto do local da aplicação

    fun rebindCameraProvider() {
        cameraProvider?.let { cameraProvider ->
            // abrindo uma thread para tratar de acessar a cemera
            // e pedir para a camera especificamnete
            // que seu programa quer acessar a camera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                localContext as LifecycleOwner,
                cameraSelector,
                previewUseCase, ImageCaptureUseCase
            )
            cameraControl = cameraControl
        }
    }

    LaunchedEffect(Unit) {
        cameraProvider = ProcessCameraProvider
            .awaitInstance(localContext)
        rebindCameraProvider()
    }
    LaunchedEffect(lensFacing) {
        rebindCameraProvider()
    }
    LaunchedEffect(zoomLevel) {
        cameraControl?.setLinearZoom(zoomLevel)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            //criar uma thread em paralelo
            PreviewView(context).also {
                previewUseCase.surfaceProvider = it.surfaceProvider
                rebindCameraProvider()
            }
        }
    )
}
