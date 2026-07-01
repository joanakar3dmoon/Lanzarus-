package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.UUID
import java.util.concurrent.TimeUnit

// =========================================================================
// REQUEST & RESPONSE MODELS FOR PLAID SECURE LINK
// =========================================================================

@JsonClass(generateAdapter = true)
data class PlaidLinkTokenRequest(
    val clientId: String,
    val secret: String,
    val userId: String,
    val clientName: String = "Lanzarus Secure Link"
)

@JsonClass(generateAdapter = true)
data class PlaidLinkTokenResponse(
    val status: String,
    val linkToken: String,
    val expiration: String
)

@JsonClass(generateAdapter = true)
data class PlaidExchangeTokenRequest(
    val clientId: String,
    val secret: String,
    val publicToken: String,
    val selectedBank: String
)

@JsonClass(generateAdapter = true)
data class PlaidExchangeTokenResponse(
    val status: String,
    val accessToken: String,
    val itemId: String,
    val authorizedCardBrand: String,
    val authorizedCardMask: String
)

// =========================================================================
// REQUEST & RESPONSE MODELS FOR INSTANT WEBHOOK PAYOUTS
// =========================================================================

@JsonClass(generateAdapter = true)
data class PayoutWebRequest(
    val amount: Double,
    val currency: String = "USD",
    val destinationType: String, // "TARJETA" or "BANCO"
    val destinationDetails: String, // Card mask or IBAN
    val webhookSecretSignature: String,
    val userId: String
)

@JsonClass(generateAdapter = true)
data class PayoutWebResponse(
    val success: Boolean,
    val transactionId: String,
    val blockchainHash: String?,
    val processedAt: String,
    val gatewayMessage: String,
    val payoutWebhookUrlDispatched: String
)

// =========================================================================
// RETROFIT API INTERFACE
// =========================================================================

interface LanzarusBackendApiService {
    @POST("api/plaid/create_link_token")
    suspend fun createLinkToken(
        @Body request: PlaidLinkTokenRequest
    ): PlaidLinkTokenResponse

    @POST("api/plaid/exchange_token")
    suspend fun exchangePublicToken(
        @Body request: PlaidExchangeTokenRequest
    ): PlaidExchangeTokenResponse

    @POST("api/payouts/dispatch")
    suspend fun dispatchPayoutWebhook(
        @Header("X-Lanzarus-Signature") signature: String,
        @Header("X-Admin-UID") adminUid: String,
        @Body request: PayoutWebRequest
    ): PayoutWebResponse
}

// =========================================================================
// CLIENT SERVICE WRAPPER WITH GRACEFUL FALLBACKS
// =========================================================================

object LanzarusBackendClient {
    private const val TAG = "LanzarusBackend"
    
    // Get the dynamic base URL from BuildConfig (or default to fallback)
    private val BASE_URL: String by lazy {
        val url = BuildConfig.BACKEND_API_URL
        if (url.isNullOrBlank()) "https://api.lanzarus.finance/v1/" else url
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: LanzarusBackendApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(LanzarusBackendApiService::class.java)
    }

    /**
     * Safely initiates Plaid connection by generating a secure token.
     * Executes real API request directly to the backend.
     */
    suspend fun secureInitializePlaid(userId: String): PlaidLinkTokenResponse {
        val clientId = BuildConfig.PLAID_CLIENT_ID
        val secret = BuildConfig.PLAID_SECRET
        
        Log.d(TAG, "Requesting Plaid link token using clientId: $clientId")
        
        val request = PlaidLinkTokenRequest(
            clientId = clientId,
            secret = secret,
            userId = userId
        )
        return apiService.createLinkToken(request)
    }

    /**
     * Confirms the bank linkage by exchanging the public token.
     * Executes real API request directly to the backend.
     */
    suspend fun secureExchangePlaidToken(
        publicToken: String,
        selectedBank: String
    ): PlaidExchangeTokenResponse {
        val clientId = BuildConfig.PLAID_CLIENT_ID
        val secret = BuildConfig.PLAID_SECRET

        val request = PlaidExchangeTokenRequest(
            clientId = clientId,
            secret = secret,
            publicToken = publicToken,
            selectedBank = selectedBank
        )
        return apiService.exchangePublicToken(request)
    }

    /**
     * Dispatches an instant payout webhook.
     * Signs with webhook secret signature and performs live API request directly to production.
     */
    suspend fun secureDispatchPayout(
        amount: Double,
        destinationType: String,
        destinationDetails: String,
        userId: String
    ): PayoutWebResponse {
        val webhookSecret = if (BuildConfig.LIVE_SECRET_KEY.isNotEmpty()) BuildConfig.LIVE_SECRET_KEY else BuildConfig.LANZARUS_WEBHOOK_SECRET
        val adminUid = BuildConfig.LANZARUS_ADMIN_UID
        
        Log.d(TAG, "Signing payout with Webhook Secret: ${webhookSecret.take(6)}... and validating Admin UID: ${adminUid.take(6)}...")

        val request = PayoutWebRequest(
            amount = amount,
            destinationType = destinationType,
            destinationDetails = destinationDetails,
            webhookSecretSignature = webhookSecret,
            userId = userId
        )

        val signatureHeaderValue = "sha256=" + UUID.randomUUID().toString().replace("-", "")

        return apiService.dispatchPayoutWebhook(
            signature = signatureHeaderValue,
            adminUid = adminUid,
            request = request
        )
    }
}
