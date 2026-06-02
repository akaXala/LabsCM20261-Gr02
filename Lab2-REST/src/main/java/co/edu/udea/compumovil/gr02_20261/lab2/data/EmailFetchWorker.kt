package co.edu.udea.compumovil.gr02_20261.lab2.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class EmailFetchWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Hacemos la petición a MockAPI
            val emails = RetrofitClient.apiService.getEmails()

            // Por ahora, solo los imprimimos para confirmar que el Worker se ejecuta.
            emails.forEach { email ->
                Log.d("WorkerTest", "Worker en segundo plano descargó: ${email.subject}")
            }

            // Indicamos que el trabajo terminó con éxito
            Result.success()
        } catch (e: Exception) {
            Log.e("WorkerTest", "Error en el Worker: ${e.message}")
            // Si hay un error (ej. sin internet), le decimos a Android que reintente más tarde
            Result.retry()
        }
    }
}