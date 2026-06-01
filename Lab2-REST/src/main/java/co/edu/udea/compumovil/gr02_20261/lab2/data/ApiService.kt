package co.edu.udea.compumovil.gr02_20261.lab2.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {
    @GET("emails")
    suspend fun getEmails(): List<Email>
}

object RetrofitClient {
    private const val BASE_URL = "https://6a1e0720bcc4f20d5ca5476c.mockapi.io/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}