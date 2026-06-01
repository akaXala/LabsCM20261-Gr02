package co.edu.udea.compumovil.gr02_20261.lab2

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
import co.edu.udea.compumovil.gr02_20261.lab2.ui.theme.Labs20261Gr02Theme

import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import co.edu.udea.compumovil.gr02_20261.lab2.data.RetrofitClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            try {
                val emails = RetrofitClient.apiService.getEmails()
                emails.forEach { email ->
                    Log.d("RetrofitTest", "Éxito - Asunto: ${email.subject} | Remitente: ${email.sender}")
                }
            } catch (e: Exception) {
                Log.e("RetrofitTest", "Error consumiendo la API: ${e.message}")
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Labs20261Gr02Theme {
        Greeting("Android")
    }
}