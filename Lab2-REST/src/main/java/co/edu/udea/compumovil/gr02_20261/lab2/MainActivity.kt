package co.edu.udea.compumovil.gr02_20261.lab2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import co.edu.udea.compumovil.gr02_20261.lab2.data.Email
import co.edu.udea.compumovil.gr02_20261.lab2.data.EmailFetchWorker
import co.edu.udea.compumovil.gr02_20261.lab2.data.EmailRepository
import co.edu.udea.compumovil.gr02_20261.lab2.data.RetrofitClient
import co.edu.udea.compumovil.gr02_20261.lab2.ui.theme.Labs20261Gr02Theme
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración del Worker Periódico (cada 15 min)
        val periodicWorkRequest = PeriodicWorkRequestBuilder<EmailFetchWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "FetchEmailsTask",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

        // Sincronización inmediata vía WorkManager
        val immediateWorkRequest = OneTimeWorkRequestBuilder<EmailFetchWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(immediateWorkRequest)

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
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "inbox") {
        composable("inbox") {
            InboxScreen(
                onEmailClick = { emailId ->
                    navController.navigate("detail/$emailId")
                }
            )
        }
        composable(
            route = "detail/{emailId}",
            arguments = listOf(navArgument("emailId") { type = NavType.StringType })
        ) { backStackEntry ->
            val emailId = backStackEntry.arguments?.getString("emailId")
            DetailScreen(
                emailId = emailId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(onEmailClick: (String) -> Unit) {
    val emails by EmailRepository.emails.collectAsState()
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Función para refrescar datos
    val refreshEmails = {
        scope.launch {
            isLoading = true
            try {
                val fetchedEmails = RetrofitClient.apiService.getEmails()
                EmailRepository.updateEmails(fetchedEmails)
            } catch (e: Exception) {
                Log.e("InboxScreen", "Error fetching emails: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar automáticamente al iniciar si está vacío
    LaunchedEffect(Unit) {
        if (emails.isEmpty()) {
            refreshEmails()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.inbox_title)) },
                actions = {
                    // Botón de Refrescar
                    IconButton(onClick = { refreshEmails() }, enabled = !isLoading) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar"
                        )
                    }
                    // Botón de Idioma
                    IconButton(onClick = {
                        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)?.language
                        val newLocale = if (currentLocale == "es") "en" else "es"
                        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLocale)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = stringResource(R.string.switch_language)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (emails.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.waiting_emails))
                }
            } else if (isLoading && emails.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
}

@Composable
fun EmailCard(email: Email, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(emailId: String?, onBack: () -> Unit) {
    val email = emailId?.let { EmailRepository.getEmailById(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.message_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc)
                        )
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
                Text(
                    text = stringResource(R.string.sender_label, email.sender),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.date_label, email.date),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = email.body, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.email_not_found))
            }
        }
    }
}