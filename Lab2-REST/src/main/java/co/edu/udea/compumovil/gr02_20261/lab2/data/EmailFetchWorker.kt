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

            EmailRepository.updateEmails(emails)

            Log.d("WorkerTest", "Worker guardó ${emails.size} correos en memoria.")
            Result.success()
        } catch (e: Exception) {
            Log.e("WorkerTest", "Error en el Worker: ${e.message}")
            Result.retry()
        }
    }
}