package co.edu.udea.compumovil.gr02_20261.lab2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import co.edu.udea.compumovil.gr02_20261.lab2.data.Email
import co.edu.udea.compumovil.gr02_20261.lab2.data.EmailFetchWorker
import co.edu.udea.compumovil.gr02_20261.lab2.data.EmailRepository
import co.edu.udea.compumovil.gr02_20261.lab2.ui.theme.Labs20261Gr02Theme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configuración del Worker que ya tenías
        val workRequest = PeriodicWorkRequestBuilder<EmailFetchWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "FetchEmailsTask",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        // 2. Dibujar la Interfaz Visual (Esto faltaba)
        setContent {
            Labs20261Gr02Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReplyApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyApp() {
    // Conectamos la interfaz con la memoria reactiva del EmailRepository
    val emails by EmailRepository.emails.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bandeja de Entrada") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        // Si el Worker aún no descarga los datos, mostramos un mensaje de espera
        if (emails.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Esperando correos...")
            }
        } else {
            // Cuando los datos llegan, dibujamos la lista
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emails) { email ->
                    EmailCard(email)
                }
            }
        }
    }
}

@Composable
fun EmailCard(email: Email) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (email.isRead) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = email.sender, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = email.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = email.body, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}