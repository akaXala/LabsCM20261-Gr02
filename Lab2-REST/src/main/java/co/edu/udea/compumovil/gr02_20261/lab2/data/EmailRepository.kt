package co.edu.udea.compumovil.gr02_20261.lab2.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Al ser un 'object', es un Singleton: solo existe una instancia en toda la app.
object EmailRepository {
    // Aquí guardamos la lista privada
    private val _emails = MutableStateFlow<List<Email>>(emptyList())

    // Esta es la lista pública que la interfaz va a observar (solo lectura)
    val emails: StateFlow<List<Email>> = _emails

    // Función para que el Worker actualice los datos
    fun updateEmails(newEmails: List<Email>) {
        _emails.value = newEmails
    }
}