package co.edu.udea.compumovil.gr02_20261.lab2.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object EmailRepository {
    private val _emails = MutableStateFlow<List<Email>>(emptyList())
    val emails: StateFlow<List<Email>> = _emails

    fun updateEmails(newEmails: List<Email>) {
        _emails.value = newEmails
    }

    fun getEmailById(id: String): Email? {
        return _emails.value.find { it.id == id }
    }
}