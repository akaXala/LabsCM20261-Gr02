package co.edu.udea.compumovil.gr02_20261.lab2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

        // Configuración del Worker
        val workRequest = PeriodicWorkRequestBuilder<EmailFetchWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "FetchEmailsTask",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

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

@Composable
fun ReplyApp() {
    // El controlador de navegación
    val navController = rememberNavController()

    // NavHost define las rutas de la aplicación
    NavHost(navController = navController, startDestination = "inbox") {

        // Ruta 1: La lista principal
        composable("inbox") {
            InboxScreen(
                onEmailClick = { emailId ->
                    navController.navigate("detail/$emailId") // Navega pasando el ID
                }
            )
        }

        // Ruta 2: El detalle del correo
        composable(
            route = "detail/{emailId}",
            arguments = listOf(navArgument("emailId") { type = NavType.StringType })
        ) { backStackEntry ->
            val emailId = backStackEntry.arguments?.getString("emailId")
            DetailScreen(
                emailId = emailId,
                onBack = { navController.popBackStack() } // Vuelve atrás
            )
        }
    }
}

// --- FUNCIONALIDAD 1: BANDEJA DE ENTRADA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(onEmailClick: (String) -> Unit) {
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
        if (emails.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Esperando correos...")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emails) { email ->
                    EmailCard(email = email, onClick = { onEmailClick(email.id) })
                }
            }
        }
    }
}

@Composable
fun EmailCard(email: Email, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, // Ahora es clickeable
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

// --- FUNCIONALIDAD 2: LECTURA DEL CORREO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(emailId: String?, onBack: () -> Unit) {
    // Buscamos el correo específico
    val email = emailId?.let { EmailRepository.getEmailById(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mensaje") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (email != null) {
            Column(
                modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()
            ) {
                Text(text = email.subject, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "De: ${email.sender}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(text = "Fecha: ${email.date}", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = email.body, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Correo no encontrado")
            }
        }
    }
}