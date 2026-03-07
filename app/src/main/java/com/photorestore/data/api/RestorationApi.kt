package com.photorestore.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface RestorationApi {
    @Multipart
    @POST
    suspend fun restoreImage(@Url url: String, @Part image: MultipartBody.Part): Response<RestorationResponse>
    companion object { const val DEFAULT_BASE_URL = "https://api.deepai.org/" }
}

data class RestorationResponse(val output_url: String?, val error: String?, val status: String?)
