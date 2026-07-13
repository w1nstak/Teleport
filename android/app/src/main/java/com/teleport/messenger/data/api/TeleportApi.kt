package com.teleport.messenger.data.api

import com.teleport.messenger.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.http.*
import retrofit2.http.Part
import java.io.File
import java.util.concurrent.TimeUnit

interface TeleportApi {
    @POST("auth/register")
    suspend fun register(@Body body: AuthRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: AuthRequest): AuthResponse

    @POST("auth/login/username")
    suspend fun loginByUsername(@Body body: UsernameLoginRequest): AuthResponse

    @POST("auth/register/web")
    suspend fun registerWeb(@Body body: WebRegisterRequest): AuthResponse

    @POST("auth/qr")
    suspend fun qrLogin(@Body body: QrAuthRequest): AuthResponse

    @POST("auth/recover")
    suspend fun recover(@Body body: AuthRequest): AuthResponse

    @POST("auth/sms/send")
    suspend fun sendSms(@Body body: SmsSendRequest): SmsSendResponse

    @POST("auth/sms/verify")
    suspend fun verifySms(@Body body: SmsVerifyRequest): SmsVerifyResponse

    @POST("devices/fcm")
    suspend fun registerFcm(@Header("Authorization") token: String, @Body body: FcmTokenRequest)

    @Multipart
    @POST("media/upload")
    suspend fun uploadMedia(@Header("Authorization") token: String, @Part file: MultipartBody.Part): MediaUploadResponse

    @POST("messages/send")
    suspend fun sendMessage(@Header("Authorization") token: String, @Body body: SendMessageRequest): MessageDto

    @PATCH("messages/{id}")
    suspend fun editMessage(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: EditMessageRequest,
    ): MessageDto

    @POST("messages/{id}/forward")
    suspend fun forwardMessage(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: ForwardMessageRequest,
    ): MessageDto

    @GET("messages/sync")
    suspend fun syncMessages(@Header("Authorization") token: String, @Query("since") since: Long): SyncResponse

    @GET("users/search")
    suspend fun searchUsers(@Header("Authorization") token: String, @Query("q") query: String): List<UserDto>

    @GET("users/me")
    suspend fun getMe(@Header("Authorization") token: String): UserDto

    @PATCH("users/me")
    suspend fun updateMe(@Header("Authorization") token: String, @Body body: UpdateProfileRequest): UserDto

    @GET("users/{id}")
    suspend fun getUser(@Header("Authorization") token: String, @Path("id") id: String): UserDto

    @POST("chats/open")
    suspend fun openChat(@Header("Authorization") token: String, @Body body: OpenChatRequest): OpenChatResponse

    @GET("chats")
    suspend fun listChats(@Header("Authorization") token: String): List<ChatListItemDto>

    @GET("users/username/{username}/available")
    suspend fun checkUsername(@Path("username") username: String, @Query("exclude") exclude: String? = null): UsernameCheckResponse

    @GET("admin/check")
    suspend fun adminCheck(@Header("Authorization") token: String): AdminCheckDto

    @GET("admin/stats")
    suspend fun adminStats(@Header("Authorization") token: String): AdminStatsDto
}

object ApiClient {
    val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: TeleportApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(TeleportApi::class.java)

    fun wsUrl(token: String): String {
        val base = BuildConfig.API_BASE_URL.removeSuffix("/")
        val wsBase = base.replace("http://", "ws://").replace("https://", "wss://")
        return "$wsBase/ws?token=$token"
    }

    suspend fun uploadFile(token: String, file: File, mime: String): String {
        val part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody(mime.toMediaType()),
        )
        val path = api.uploadMedia("Bearer $token", part).url
        val base = BuildConfig.API_BASE_URL.removeSuffix("/")
        return if (path.startsWith("http")) path else "$base$path"
    }
}
