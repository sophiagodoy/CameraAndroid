package br.com.ibm.cameraandroid

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.ibm.cameraandroid.permissions.WithPermission
import br.com.ibm.cameraandroid.ui.theme.CameraAndroidTheme

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

